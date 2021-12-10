package pt.tecnico.bicloin.hub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.concurrent.CountDownLatch;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.grpc.Hub;
import pt.tecnico.bicloin.hub.grpc.Hub.BalanceRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.BalanceResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeDownRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeDownResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeUpRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeUpResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.InfoStationRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.InfoStationResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.LocateStationRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.LocateStationResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.SysStatusResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.TopUpRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.TopUpResponse;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc.HubServiceStub;
import pt.tecnico.rec.grpc.Rec;
import pt.tecnico.rec.grpc.Rec.ReadRequest;
import pt.tecnico.rec.grpc.Rec.ReadResponse;
import pt.tecnico.rec.grpc.Rec.Tag;
import pt.tecnico.rec.grpc.Rec.WriteRequest;
import pt.tecnico.rec.grpc.Rec.WriteResponse;
import pt.tecnico.rec.grpc.RecordServiceGrpc;
import pt.tecnico.rec.grpc.RecordServiceGrpc.RecordServiceStub;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import static pt.tecnico.bicloin.hub.HubError.*;


/**
 * HubBackend
 */
public class HubBackend {
    private final String path;
    private final ZKNaming zkNaming;
    private HashMap<String, HubUserEntry> users; // Map<Key: userID, Value: UserInfo>
    private HashMap<String, HubStationEntry> stations; // Map<Key: stationID, Value: StationInfo>
    private ArrayList<RecordServiceStub> stubs = new ArrayList<>();
    private ArrayList<ManagedChannel> channels = new ArrayList<>();
    private ArrayList<String> paths = new ArrayList<>();
    private ArrayList<String> uris = new ArrayList<>();
    private final int combinedWeight;
    private final int cid;

    public HubBackend(String path, ZKNaming zkNaming, String users, String stations, int cid, boolean initRec, int combinedWeight) throws ZKNamingException, HubException, InterruptedException {
        this.path = path;
        this.zkNaming = zkNaming;
        this.cid = cid;

        String recs = "/grpc/bicloin/rec";
        Collection<ZKRecord> records = zkNaming.listRecords(recs);

        this.combinedWeight = combinedWeight;

        // creating arrays of paths, channels and stubs found in the zookeeper
        for (ZKRecord zkrecord : records) {
            this.paths.add(zkrecord.getPath());
            String target = zkrecord.getURI();
            this.uris.add(target);
            ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            this.stubs.add(RecordServiceGrpc.newStub(channel));
            this.channels.add(channel);
        }

        // parsing the files given in the starting arguments
        HubParsing hubParser = new HubParsing();
        this.users = hubParser.parseUsers(users, initRec, this);
        this.stations = hubParser.parseStations(stations, initRec, this);
    }


    private void searchForNewRec(int i) {
        try {
            // lookup in zookeeper to see if the path is still registered or not
            ZKRecord zkRecord = zkNaming.lookup(paths.get(i));
            // shutdown the old channel and set up a new one if a record server with the same path but a different port exists
            channels.get(i).shutdown();
            ManagedChannel newChannel = ManagedChannelBuilder.forTarget(zkRecord.getURI()).usePlaintext().build();
            // add the new channel and the new stub to the structures
            channels.set(i, newChannel);
            stubs.set(i, RecordServiceGrpc.newStub(newChannel));
        } catch (ZKNamingException ignored) {
        }
    }

    // function used to calculate haversine distance between coordinates. cred: github.com/jasonwinn
    public double haversine(double lat1, double lon1, double lat2, double lon2) {
        // distance between latitudes and longitudes
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLon / 2), 2) *
                        Math.cos(lat1) *
                        Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
    }

    public void sysStatusHub(List<String> outPaths, List<Boolean> outBools) throws ZKNamingException, InterruptedException {
        String hub = "/grpc/bicloin/hub";
        ArrayList<ZKRecord> hubs = new ArrayList<>(zkNaming.listRecords(hub));

        ArrayList<HubResponseCollector<Hub.CtrlPingResponse>> hubResponseCollectors = new ArrayList<>();

        ArrayList<ManagedChannel> hubChannels = new ArrayList<>();

        CountDownLatch hubLatch = new CountDownLatch(hubs.size());

        String tempPath;

        for (int i = 0; i < hubs.size(); i++) {
            tempPath = hubs.get(i).getPath();

            // if record_iter is this hub instance, there's no need to ping.
            if (tempPath.equals(this.path)) {
                outPaths.add(tempPath);
                outBools.add(true);
                hubLatch.countDown();
                continue;
            }

            ZKRecord zkrecord = zkNaming.lookup(tempPath);
            String target = zkrecord.getURI();

            ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            hubChannels.add(i, channel);

            HubServiceStub stub = HubServiceGrpc.newStub(channel);


            hubResponseCollectors.add(new HubResponseCollector<>(hubLatch));
            stub.withDeadlineAfter(2000, TimeUnit.MILLISECONDS).ctrlPing(Hub.CtrlPingRequest.newBuilder().setInput("Friend").build(),
                    new HubStreamObserver<>(hubResponseCollectors.get(i)));

        }

        hubLatch.await();  // wait for all stub requests to get a response

        for (int i = 0; i < hubChannels.size(); i++) {
            try {
                if (hubResponseCollectors.get(i).getException() != null) {
                    throw (StatusRuntimeException) hubResponseCollectors.get(i).getException();
                }

                Hub.CtrlPingResponse response = hubResponseCollectors.get(i).getResponse();
                outBools.add(response.getOutput().equals("Hello Friend!"));
            } catch (Exception e) {
                outBools.add(false);
            }

            tempPath = hubs.get(i).getPath();
            outPaths.add(tempPath);
            hubChannels.get(i).shutdownNow();
        }

    }

    public void sysStatusRec(List<String> outPaths, List<Boolean> outBools) throws ZKNamingException, InterruptedException {
        String rec = "/grpc/bicloin/rec";
        ArrayList<ZKRecord> recs = new ArrayList<>(zkNaming.listRecords(rec));

        // Array of hubResponseCollectors, one for each stub to write its response to
        ArrayList<HubResponseCollector<Rec.CtrlPingResponse>> responseCollectors = new ArrayList<>();

        ArrayList<ManagedChannel> recChannels = new ArrayList<>();

        // To count how many responses are yet to arrive.
        CountDownLatch recLatch = new CountDownLatch(recs.size());

        String tempPath;

        int recSize = recs.size();

        for (int i = 0; i < recSize; i++) {
            tempPath = recs.get(i).getPath();

            ZKRecord zkrecord = zkNaming.lookup(tempPath);
            String target = zkrecord.getURI();

            ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            recChannels.add(i, channel);
            RecordServiceStub stub = RecordServiceGrpc.newStub(recChannels.get(i));

            responseCollectors.add(new HubResponseCollector<>(recLatch));
            stub.withDeadlineAfter(2000, TimeUnit.MILLISECONDS).ctrlPing(Rec.CtrlPingRequest.newBuilder().setInput("Friend").build(),
                    new HubStreamObserver<>(responseCollectors.get(i)));
        }

        recLatch.await(); // wait for all stub requests to get a response

        // read all responses/exceptions
        for (int i = 0; i < recSize; i++) {
            try {
                if (responseCollectors.get(i).getException() != null)
                    throw (StatusRuntimeException) responseCollectors.get(i).getException();

                Rec.CtrlPingResponse response = responseCollectors.get(i).getResponse();
                outBools.add(response.getOutput().equals("Hello Friend!"));
            } catch (Exception e) {
                outBools.add(false);
            }

            tempPath = recs.get(i).getPath();
            outPaths.add(tempPath);
            recChannels.get(i).shutdownNow();
        }
    }

    public SysStatusResponse sysStatus() throws ZKNamingException, InterruptedException {
        ArrayList<String> outPaths = new ArrayList<>();
        ArrayList<Boolean> outBools = new ArrayList<>();

        sysStatusHub(outPaths, outBools);
        sysStatusRec(outPaths, outBools);

        return SysStatusResponse.newBuilder().addAllServerName(outPaths).addAllIsUp(outBools).build();
    }


    public BalanceResponse balance(BalanceRequest request) throws InterruptedException {
        ReadRequest readRequest = ReadRequest.newBuilder().setKey(request.getUser()).setTable(0).setColumn(0).build();
        int balance;

        // lock the structure for the requested user
        synchronized (users.get(request.getUser())) {
            balance = readWithWriteback(readRequest)[0];
        }

        return BalanceResponse.newBuilder().setBalance(balance).build();
    }

    public TopUpResponse topUp(TopUpRequest request) throws HubException, InterruptedException {
        if (!Objects.equals(users.get(request.getUser()).getPhone(), request.getPhone()))
            throw new HubException(WRONG_PHONE_NUMBER);

        ReadRequest readRequest = ReadRequest.newBuilder().setKey(request.getUser()).setTable(0).setColumn(0).build();

        int newBalance;

        synchronized (users.get(request.getUser())) {
            int[] readResult = readOP(readRequest);

            // getting the balance and incrementing it by the given amount
            newBalance = readResult[0] + request.getAmount() * 10;

            Tag tag = Tag.newBuilder().setSeq(readResult[1] + 1).setCid(cid).build();

            writeBack(WriteRequest.newBuilder().setKey(request.getUser()).setTable(0).setColumn(0).setValue(newBalance).setTag(tag).build());
        }


        return TopUpResponse.newBuilder().setBalance(newBalance).build();
    }


    public BikeUpResponse bikeUp(BikeUpRequest request) throws HubException, InterruptedException {
        if (!stations.containsKey(request.getStation())) {
            throw new HubException(STATION_DOESNT_EXIST);
        }

        HubStationEntry station = stations.get(request.getStation());

        if (haversine(request.getLatitude(), request.getLongitude(), station.getLatitude(), station.getLongitude()) >= 0.2) {
            throw new HubException(USER_OUT_OF_RANGE);
        }

        ReadRequest hasBicyclesRequest = ReadRequest.newBuilder().setKey(request.getUser()).setTable(0).setColumn(1).build();
        ReadRequest balanceRequest = ReadRequest.newBuilder().setKey(request.getUser()).setTable(0).setColumn(0).build();
        ReadRequest numBicyclesRequest = ReadRequest.newBuilder().setKey(request.getStation()).setTable(1).setColumn(1).build();
        ReadRequest pickupsRequest = ReadRequest.newBuilder().setKey(request.getStation()).setTable(1).setColumn(3).build();

        synchronized (users.get(request.getUser())) {
            synchronized (stations.get(request.getStation())) {
                // requesting the needed fields from the record service servers
                int[] newBalanceResult = readOP(balanceRequest);
                int[] pickupsResult = readOP(pickupsRequest);
                int[] hasBikeResult = readOP(hasBicyclesRequest);
                int[] oldNumBicyclesResult = readOP(numBicyclesRequest);


                if (oldNumBicyclesResult[0] < 1) {
                    throw new HubException(NO_BIKES_AVAILABLE);
                }

                if (newBalanceResult[0] - 10 < 0) {
                    throw new HubException(INSUFFICIENT_BALANCE);
                }

                if (hasBikeResult[0] == 1) {
                    throw new HubException(USER_HAS_BIKE);
                }

                Tag tag1 = Tag.newBuilder().setSeq(oldNumBicyclesResult[1] + 1).setCid(cid).build();
                Tag tag2 = Tag.newBuilder().setSeq(hasBikeResult[1] + 1).setCid(cid).build();
                Tag tag3 = Tag.newBuilder().setSeq(newBalanceResult[1] + 1).setCid(cid).build();
                Tag tag4 = Tag.newBuilder().setSeq(pickupsResult[1] + 1).setCid(cid).build();

                writeBack(WriteRequest.newBuilder().setKey(request.getStation()).setTable(1).setColumn(1).setValue(oldNumBicyclesResult[0] - 1).setTag(tag1).build()); // changing numBicycles
                writeBack(WriteRequest.newBuilder().setKey(request.getUser()).setTable(0).setColumn(1).setValue(1).setTag(tag2).build());  // hasBike = 1
                writeBack(WriteRequest.newBuilder().setKey(request.getUser()).setTable(0).setColumn(0).setValue(newBalanceResult[0] - 10).setTag(tag3).build());  // changing balance
                writeBack(WriteRequest.newBuilder().setKey(request.getStation()).setTable(1).setColumn(3).setValue(pickupsResult[0] + 1).setTag(tag4).build());  // changing numPickups
            }
        }

        return BikeUpResponse.getDefaultInstance();
    }

    public BikeDownResponse bikeDown(BikeDownRequest request) throws HubException, InterruptedException {
        if (!stations.containsKey(request.getStation())) {
            throw new HubException(STATION_DOESNT_EXIST);
        }

        HubStationEntry station = stations.get(request.getStation());

        if (haversine(request.getLatitude(), request.getLongitude(), station.getLatitude(), station.getLongitude()) >= 0.2) {
            throw new HubException(USER_OUT_OF_RANGE);
        }

        ReadRequest hasBicyclesRequest = ReadRequest.newBuilder().setKey(request.getUser()).setTable(0).setColumn(1).build();
        ReadRequest numBicyclesRequest = ReadRequest.newBuilder().setKey(request.getStation()).setTable(1).setColumn(1).build();
        ReadRequest prizeRequest = ReadRequest.newBuilder().setKey(request.getStation()).setTable(1).setColumn(2).build();
        ReadRequest numDocksRequest = ReadRequest.newBuilder().setKey(request.getStation()).setTable(1).setColumn(0).build();
        ReadRequest balanceRequest = ReadRequest.newBuilder().setKey(request.getUser()).setTable(0).setColumn(0).build();
        ReadRequest dropsRequest = ReadRequest.newBuilder().setKey(request.getStation()).setTable(1).setColumn(4).build();


        synchronized (users.get(request.getUser())) {
            synchronized (stations.get(request.getStation())) {
                int[] hasBikeResult = readOP(hasBicyclesRequest);
                int[] dropsResult = readOP(dropsRequest);
                int[] oldNumBicyclesResult = readOP(numBicyclesRequest);
                int[] numDocksResult = readOP(numDocksRequest);

                if (oldNumBicyclesResult[0] + 1 > numDocksResult[0]) {
                    throw new HubException(NO_DOCKS);
                }

                if (hasBikeResult[0] == 0) {
                    throw new HubException(USER_HAS_NO_BIKE);
                }

                int prize = readOP(prizeRequest)[0];
                int[] newBalanceResult = readOP(balanceRequest);

                Tag tag1 = Tag.newBuilder().setSeq(oldNumBicyclesResult[1] + 1).setCid(cid).build();
                Tag tag2 = Tag.newBuilder().setSeq(hasBikeResult[1] + 1).setCid(cid).build();
                Tag tag3 = Tag.newBuilder().setSeq(newBalanceResult[1] + 1).setCid(cid).build();
                Tag tag4 = Tag.newBuilder().setSeq(dropsResult[1] + 1).setCid(cid).build();

                writeBack(WriteRequest.newBuilder().setKey(request.getStation()).setTable(1).setColumn(1).setValue(oldNumBicyclesResult[0] + 1).setTag(tag1).build()); // changing numBicycles
                writeBack(WriteRequest.newBuilder().setKey(request.getUser()).setTable(0).setColumn(1).setValue(0).setTag(tag2).build());  // hasBike = 0
                writeBack(WriteRequest.newBuilder().setKey(request.getUser()).setTable(0).setColumn(0).setValue(newBalanceResult[0] + prize).setTag(tag3).build());  // changing balance
                writeBack(WriteRequest.newBuilder().setKey(request.getStation()).setTable(1).setColumn(4).setValue(dropsResult[0] + 1).setTag(tag4).build());  // changing numDrops
            }
        }

        return BikeDownResponse.getDefaultInstance();
    }

    public InfoStationResponse infoStation(InfoStationRequest request) throws HubException, InterruptedException {
        String station = request.getStation();
        int numDocks;
        int numBicycles;
        int prize;
        int numPickups;
        int numDrops;
        if (!stations.containsKey(station)) {
            throw new HubException(STATION_DOESNT_EXIST);
        }

        HubStationEntry stationEntry = stations.get(station);

        ReadRequest numDocksRequest = ReadRequest.newBuilder().setKey(station).setTable(1).setColumn(0).build();
        ReadRequest numBicyclesRequest = ReadRequest.newBuilder().setKey(station).setTable(1).setColumn(1).build();
        ReadRequest prizeRequest = ReadRequest.newBuilder().setKey(station).setTable(1).setColumn(2).build();
        ReadRequest pickupsRequest = ReadRequest.newBuilder().setKey(station).setTable(1).setColumn(3).build();
        ReadRequest dropsRequest = ReadRequest.newBuilder().setKey(station).setTable(1).setColumn(4).build();

        synchronized (stations.get(request.getStation())) {
            // read all needed data and update the recs if necessary
            numDocks = readWithWriteback(numDocksRequest)[0];
            numBicycles = readWithWriteback(numBicyclesRequest)[0];
            prize = readWithWriteback(prizeRequest)[0];
            numPickups = readWithWriteback(pickupsRequest)[0];
            numDrops = readWithWriteback(dropsRequest)[0];
        }

        return InfoStationResponse.newBuilder().setStationName(stationEntry.getStationName())
                .setLatitude(stationEntry.getLatitude()).setLongitude(stationEntry.getLongitude()).setNumDocks(numDocks)
                .setPrize(prize).setNumBicycles(numBicycles).setNumPickups(numPickups).setNumDrops(numDrops).build();
    }

    public LocateStationResponse locateStation(LocateStationRequest request) throws HubException {
        double lat = request.getLatitude();
        double longt = request.getLongitude();
        int numStations = request.getK();
        if (numStations <= 0) {
            throw new HubException(INVALID_K_GIVEN);
        }

        // Maps station abbrev to haversine distance
        Map<String, Double> stationDistances = stations.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, station -> haversine(station.getValue().getLatitude(), station.getValue().getLongitude(), lat, longt)));

        // Sorts map by distance and return list of station abbrevs
        List<String> stationInfo = stationDistances.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Map.Entry::getKey).limit(numStations)
                .collect(Collectors.toList());

        return LocateStationResponse.newBuilder().addAllStations(stationInfo).build();
    }

    public void writeBack(WriteRequest request) throws InterruptedException {
        int size = stubs.size();

        // To count how many responses are yet to arrive.
        CountDownLatch latch = new CountDownLatch(size);

        // Array of hubResponseCollectors, one for each stub to write its response to
        ArrayList<HubResponseCollector<WriteResponse>> responseCollectors = new ArrayList<>();
        for (RecordServiceStub stub : stubs) {
            HubResponseCollector<WriteResponse> responseCollector = new HubResponseCollector<>(latch);
            responseCollectors.add(responseCollector);
            // Finally, make the call using the stub with timeout of 2 seconds
            stub.withDeadlineAfter(2000, TimeUnit.MILLISECONDS).write(request, new HubStreamObserver<>(responseCollector));
        }

        latch.await(); // wait for all stub requests to get a response

        // read all responses/exceptions
        for (int i = 0; i < size; i++) {
            try {
                // if the response is an exception then it must be caught (server might be down or might've changed URI)
                if (responseCollectors.get(i).getException() != null)
                    throw (StatusRuntimeException) responseCollectors.get(i).getException();
            } catch (StatusRuntimeException sre) {
                searchForNewRec(i);
            }
        }
    }

    public int[] readOP(ReadRequest request) throws InterruptedException {
        int maxSeq = -1;
        int maxCid = -1;
        int maxValue = -1;

        int quorumCombinedWeight = 0;
        int size = stubs.size();

        // To count how many responses are yet to arrive.
        CountDownLatch latch = new CountDownLatch(size);

        ArrayList<HubResponseCollector<ReadResponse>> responseCollectors = new ArrayList<>();
        for (RecordServiceStub stub : stubs) {
            HubResponseCollector<ReadResponse> responseCollector = new HubResponseCollector<>(latch);
            responseCollectors.add(responseCollector);
            stub.withDeadlineAfter(2000, TimeUnit.MILLISECONDS).read(request, new HubStreamObserver<>(responseCollector));
        }

        latch.await();  // wait for all stub requests to get a response
        size = responseCollectors.size();
        
        // read all responses/exceptions
        for (int i = 0; i < size; i++) {
            try {
                // if the response is an exception then it must be caught (server might be down or might've changed URI)
                if (responseCollectors.get(i).getException() != null) {
                    throw (StatusRuntimeException) responseCollectors.get(i).getException();
                }

                ReadResponse response = responseCollectors.get(i).getResponse();

                quorumCombinedWeight += response.getTag().getWeight();

                int seq = response.getTag().getSeq();
                int cid = response.getTag().getCid();
                int value = response.getValue();
                
                // if the response is more recent, change the value to the more recent value
                if (seq > maxSeq) {
                    maxSeq = seq;
                    maxValue = value;
                }

                if (seq == maxSeq && cid >= maxCid) {
                    maxValue = value;
                }
                
                // if a whole quorum has been contacted, break the loop
                if (quorumCombinedWeight > combinedWeight / 2) {
                    break;
                }
            } catch (StatusRuntimeException e) {
                System.out.println("Caught exception " + e.getClass() + " when trying to contact rec " + (i + 1) + " at " + uris.get(i));
                searchForNewRec(i);
            }
        }

        return new int[]{maxValue, maxSeq};
    }

    public int[] readWithWriteback(ReadRequest request) throws InterruptedException {
        int[] readResult = readOP(request);
        int maxValue = readResult[0];
        int maxSeq = readResult[1];
        Tag writebackTag = Tag.newBuilder().setSeq(maxSeq).setCid(cid).build();
        WriteRequest writeRequest = WriteRequest.newBuilder().setKey(request.getKey()).setTable(request.getTable())
                .setColumn(request.getColumn()).setValue(maxValue).setTag(writebackTag).build();
        writeBack(writeRequest);

        return new int[]{maxValue, maxSeq};
    }
}
