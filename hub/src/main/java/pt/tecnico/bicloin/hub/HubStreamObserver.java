package pt.tecnico.bicloin.hub;

import io.grpc.stub.StreamObserver;

/**
 * HubStreamObserver - Sends the responses and exceptions to responseCollector
 */
public class HubStreamObserver<R> implements StreamObserver<R> {
    HubResponseCollector<R> responseCollector;

    public HubStreamObserver(HubResponseCollector<R> responseCollector) {
        this.responseCollector = responseCollector;
    }

    @Override
    public void onNext(R r) {
        responseCollector.setResponse(r);
    }

    @Override
    public void onError(Throwable throwable) {
        responseCollector.setException(throwable);
        responseCollector.countDown();
    }

    @Override
    public void onCompleted() {
        responseCollector.countDown();
    }

}