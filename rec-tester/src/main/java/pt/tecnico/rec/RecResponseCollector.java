package pt.tecnico.rec;

import java.util.concurrent.CountDownLatch;


/**
 * RecResponseCollector - Collects the stub responses / exceptions
 */
public class RecResponseCollector<T> {
    private T response;
    private Throwable exception;
    private final CountDownLatch latch;

    public RecResponseCollector(CountDownLatch latch) {
        this.latch = latch;
    }

    public void countDown() {
        latch.countDown();
    }

    public T getResponse() {
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }
}
