package pt.tecnico.bicloin.hub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc.HubServiceStub;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class HubFrontend {
    private final ManagedChannel channel;
    private final HubServiceStub stub;
    private ZKNaming zkNaming;

    public HubFrontend(String zooHost, String zooPort, String path, boolean noURIFlag) throws ZKNamingException {
        this.zkNaming = new ZKNaming(zooHost, zooPort);
        String target;

        if (noURIFlag) {
            ZKRecord record = zkNaming.lookup(path);
            target = record.getURI();
        } else {
            target = path;
        }

        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        this.stub = HubServiceGrpc.newStub(channel);
    }

    public ZKNaming getZkNaming() {
        return this.zkNaming;
    }

    public void shutdown() {
        this.channel.shutdownNow();
    }

    public CtrlPingResponse ctrlPing(CtrlPingRequest request) throws InterruptedException, StatusRuntimeException {
        CountDownLatch latch = new CountDownLatch(1);
        HubResponseCollector<CtrlPingResponse> responseCollector = new HubResponseCollector<>(latch);
        stub.ctrlPing(request, new HubStreamObserver<>(responseCollector));
        latch.await();
        if (responseCollector.getException() != null)
            throw (StatusRuntimeException) responseCollector.getException();

        return responseCollector.getResponse();
    }

    public SysStatusResponse sysStatus(SysStatusRequest request) throws InterruptedException, StatusRuntimeException {
        CountDownLatch latch = new CountDownLatch(1);
        HubResponseCollector<SysStatusResponse> responseCollector = new HubResponseCollector<>(latch);
        stub.sysStatus(request, new HubStreamObserver<>(responseCollector));
        latch.await();
        if (responseCollector.getException() != null)
            throw (StatusRuntimeException) responseCollector.getException();
        return responseCollector.getResponse();
    }

    public BalanceResponse balance(BalanceRequest request) throws InterruptedException, StatusRuntimeException {
        CountDownLatch latch = new CountDownLatch(1);
        HubResponseCollector<BalanceResponse> responseCollector = new HubResponseCollector<>(latch);
        stub.balance(request, new HubStreamObserver<>(responseCollector));
        latch.await();
        if (responseCollector.getException() != null)
            throw (StatusRuntimeException) responseCollector.getException();
        return responseCollector.getResponse();
    }

    public TopUpResponse topUp(TopUpRequest request) throws InterruptedException, StatusRuntimeException {
        CountDownLatch latch = new CountDownLatch(1);
        HubResponseCollector<TopUpResponse> responseCollector = new HubResponseCollector<>(latch);
        stub.topUp(request, new HubStreamObserver<>(responseCollector));
        latch.await();
        if (responseCollector.getException() != null)
            throw (StatusRuntimeException) responseCollector.getException();
        return responseCollector.getResponse();
    }

    public BikeUpResponse bikeUp(BikeUpRequest request) throws InterruptedException, StatusRuntimeException {
        CountDownLatch latch = new CountDownLatch(1);
        HubResponseCollector<BikeUpResponse> responseCollector = new HubResponseCollector<>(latch);
        stub.bikeUp(request, new HubStreamObserver<>(responseCollector));
        latch.await();
        if (responseCollector.getException() != null)
            throw (StatusRuntimeException) responseCollector.getException();
        return responseCollector.getResponse();
    }

    public BikeDownResponse bikeDown(BikeDownRequest request) throws InterruptedException, StatusRuntimeException {
        CountDownLatch latch = new CountDownLatch(1);
        HubResponseCollector<BikeDownResponse> responseCollector = new HubResponseCollector<>(latch);
        stub.bikeDown(request, new HubStreamObserver<>(responseCollector));
        latch.await();
        if (responseCollector.getException() != null)
            throw (StatusRuntimeException) responseCollector.getException();
        return responseCollector.getResponse();
    }

    public InfoStationResponse infoStation(InfoStationRequest request) throws InterruptedException, StatusRuntimeException {
        CountDownLatch latch = new CountDownLatch(1);
        HubResponseCollector<InfoStationResponse> responseCollector = new HubResponseCollector<>(latch);
        stub.infoStation(request, new HubStreamObserver<>(responseCollector));
        latch.await();
        if (responseCollector.getException() != null)
            throw (StatusRuntimeException) responseCollector.getException();
        return responseCollector.getResponse();
    }

    public LocateStationResponse locateStation(LocateStationRequest request) throws InterruptedException, StatusRuntimeException {
        CountDownLatch latch = new CountDownLatch(1);
        HubResponseCollector<LocateStationResponse> responseCollector = new HubResponseCollector<>(latch);
        stub.locateStation(request, new HubStreamObserver<>(responseCollector));
        latch.await();
        if (responseCollector.getException() != null)
            throw (StatusRuntimeException) responseCollector.getException();
        return responseCollector.getResponse();
    }

}
