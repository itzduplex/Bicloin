package pt.tecnico.rec;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.rec.grpc.Rec.CtrlPingRequest;
import pt.tecnico.rec.grpc.Rec.CtrlPingResponse;
import pt.tecnico.rec.grpc.Rec.WriteRequest;
import pt.tecnico.rec.grpc.Rec.WriteResponse;
import pt.tecnico.rec.grpc.Rec.ReadRequest;
import pt.tecnico.rec.grpc.Rec.ReadResponse;
import pt.tecnico.rec.grpc.RecordServiceGrpc;
import pt.tecnico.rec.grpc.RecordServiceGrpc.RecordServiceStub;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.CountDownLatch;

public class RecFrontend {
    final ManagedChannel channel;
    final RecordServiceStub stub;
    
    
    public RecFrontend(String zooHost, String zooPort, String path) throws ZKNamingException {
        ZKNaming zkNaming = new ZKNaming(zooHost,zooPort);
        
        ZKRecord record = zkNaming.lookup(path);
        String target = record.getURI();
                
		this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
	    this.stub = RecordServiceGrpc.newStub(channel);
    }


    public void shutdown() {
        this.channel.shutdownNow();
    }

    public CtrlPingResponse ctrlPing(CtrlPingRequest request) throws InterruptedException, StatusRuntimeException {
        CountDownLatch latch = new CountDownLatch(1);
        RecResponseCollector<CtrlPingResponse> responseCollector = new RecResponseCollector<>(latch);
        stub.ctrlPing(request, new RecStreamObserver<CtrlPingResponse>(responseCollector));
        latch.await();
        if (responseCollector.getException() != null)
            throw (StatusRuntimeException) responseCollector.getException();
        return responseCollector.getResponse();
    }

    public WriteResponse write(WriteRequest request) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        RecResponseCollector<WriteResponse> responseCollector = new RecResponseCollector<>(latch);
        stub.write(request, new RecStreamObserver<WriteResponse>(responseCollector));
        latch.await();
        return responseCollector.getResponse();

    }
    
    public ReadResponse read(ReadRequest request) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        RecResponseCollector<ReadResponse> responseCollector = new RecResponseCollector<>(latch);
        stub.read(request, new RecStreamObserver<ReadResponse>(responseCollector));
        latch.await();
        return responseCollector.getResponse();
    }
}