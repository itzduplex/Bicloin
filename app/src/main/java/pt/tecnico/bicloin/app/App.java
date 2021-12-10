package pt.tecnico.bicloin.app;

import java.util.ArrayList;
import java.util.HashMap;

import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.HubFrontend;
import pt.tecnico.bicloin.hub.grpc.Hub.BalanceRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.BalanceResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeDownRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeUpRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.CtrlPingRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.CtrlPingResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.InfoStationRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.InfoStationResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.LocateStationRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.LocateStationResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.SysStatusRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SysStatusResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.TopUpRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.TopUpResponse;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class App {
    private final HubFrontend frontend;
    private HashMap<String, ArrayList<Double>> tags = new HashMap<>();
    private double lat;
    private double lon;


    public App(String zooHost, String zooPort, String uri, double lat, double lon) throws ZKNamingException {
        this.frontend = new HubFrontend(zooHost, zooPort, uri, false);
        this.lat = lat;
        this.lon = lon;
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

    public void balance(String nameId) throws InterruptedException  {
        try {
            BalanceResponse balance = frontend.balance(BalanceRequest.newBuilder().setUser(nameId).build());
            System.out.println(nameId + " " + balance.getBalance() + " BIC");
        } catch (StatusRuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    public void topUp(String nameId, String amount, String phone) throws InterruptedException  {
        try {
            int money = Integer.parseInt(amount);
            TopUpResponse afterTopUp = frontend.topUp(TopUpRequest.newBuilder().setUser(nameId).setAmount(money).setPhone(phone).build());
            System.out.println(nameId + " " + afterTopUp.getBalance() + " BIC");
        } catch (StatusRuntimeException sre) {
            System.out.println(sre.getMessage());
        } catch (NumberFormatException nfe) {
            System.out.println("Invalid arguments: not numbers");
        }
    }

    public void tag(String lat, String lon, String tag) {
        try {
            Double lat1 = Double.parseDouble(lat); 
            Double long1 = Double.parseDouble(lon);
            ArrayList<Double> doubles = new ArrayList<>();
            doubles.add(lat1);
            doubles.add(long1);
            tags.put(tag, doubles);
            System.out.println("OK");
        } catch (NumberFormatException e) {
            System.out.println("Invalid arguments: not numbers");
        }
    }

    public void at(String nameId) {
        System.out.println(nameId + " at https://www.google.com/maps/place/" + lat + "," + lon);
    }

    public void move(String nameId, String[] splitLine) {
        if (splitLine.length == 2) {
            if (!tags.containsKey(splitLine[1])) {
                System.out.println("Tag doesn't exist");
                return;
            }

            this.lat = tags.get(splitLine[1]).get(0);
            this.lon = tags.get(splitLine[1]).get(1);

        } else if (splitLine.length == 3) {
            try {
                this.lat = Double.parseDouble(splitLine[1]);
                this.lon = Double.parseDouble(splitLine[2]);
            } catch (NumberFormatException nfe) {
                System.out.println("Invalid arguments: not numbers");
                return;
            }
        }

        at(nameId);
    }

    public void scan(String k) throws InterruptedException {
        try {
            int cycles = Integer.parseInt(k);
            LocateStationRequest request = LocateStationRequest.newBuilder().setLongitude(lon).setLatitude(lat).setK(cycles).build();
            LocateStationResponse response = frontend.locateStation(request);
            for (int i = 0; i < cycles; i++) {
                InfoStationRequest stationRequest = InfoStationRequest.newBuilder().setStation(response.getStations(i)).build();
                InfoStationResponse stationResponse = frontend.infoStation(stationRequest);
                System.out.println(response.getStations(i) + ", lat " + stationResponse.getLatitude() + ", " + stationResponse.getLongitude() + " long, " + stationResponse.getNumDocks() +
                        " docas, " + stationResponse.getPrize() + " BIC prémio, " + stationResponse.getNumBicycles() + " bicicletas, a " +
                        (haversine(lat, lon, stationResponse.getLatitude(), stationResponse.getLongitude()) * 1000) + " metros");
            }

        } catch (NumberFormatException nfe) {
            System.out.println("Invalid arguments: not numbers");
        } catch (StatusRuntimeException sre) {
            System.out.println(sre.getMessage());
        }
    }

    public void info(String stationID) throws InterruptedException {
        try {
            InfoStationRequest stationRequest = InfoStationRequest.newBuilder().setStation(stationID).build();
            InfoStationResponse stationResponse = frontend.infoStation(stationRequest);
            System.out.println(stationResponse.getStationName() + ", lat " + stationResponse.getLatitude() + ", " + stationResponse.getLongitude() + " long, " +
                    stationResponse.getNumDocks() + " docas, " + stationResponse.getPrize() + " BIC prémio, " + stationResponse.getNumBicycles() + " bicicletas, " +
                    stationResponse.getNumPickups() + " levantamentos, " + stationResponse.getNumDrops() + " devoluções, https://www.google.com/maps/place/" +
                    this.lat + "," + this.lon);
        } catch (StatusRuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    public void bikeUp(String nameID, String stationID) throws InterruptedException {
        try {
            frontend.bikeUp(BikeUpRequest.newBuilder().setUser(nameID).setLatitude(lat).setLongitude(lon).setStation(stationID).build());
            System.out.println("OK");
        } catch (StatusRuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    public void bikeDown(String nameID, String stationID) throws InterruptedException {
        try {
            frontend.bikeDown(BikeDownRequest.newBuilder().setUser(nameID).setLatitude(lat).setLongitude(lon).setStation(stationID).build());
            System.out.println("OK");
        } catch (StatusRuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    public void shutdown() {
        frontend.shutdown();
    }

    public void sysStatus() throws InterruptedException {
        try {
            SysStatusResponse status = frontend.sysStatus(SysStatusRequest.getDefaultInstance());
            for (int i = 0; i < status.getIsUpList().size(); i++) {
                System.out.println("name: " + status.getServerName(i) + " isUp: " + status.getIsUp(i));
            }
        } catch (StatusRuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    public void ping() throws InterruptedException {
        try {
            CtrlPingResponse pong = frontend.ctrlPing(CtrlPingRequest.newBuilder().setInput("Friend").build());
            System.out.println(pong.getOutput());
        } catch (StatusRuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    public void help() {
        System.out.println("exit: The command is used to terminate the execution of the app");
    }
}