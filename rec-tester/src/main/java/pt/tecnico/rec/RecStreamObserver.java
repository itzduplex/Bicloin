package pt.tecnico.rec;

import io.grpc.stub.StreamObserver;

/**
 * RecStreamObserver - Sends the responses and exceptions to responseCollector
 */
public class RecStreamObserver<R> implements StreamObserver<R> {
    RecResponseCollector<R> responseCollector;

    public RecStreamObserver(RecResponseCollector<R> responseCollector) {
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
