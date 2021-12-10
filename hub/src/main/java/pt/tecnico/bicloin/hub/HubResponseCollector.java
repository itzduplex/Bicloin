package pt.tecnico.bicloin.hub;

import java.util.concurrent.CountDownLatch;

/**
 * HubResponseCollector - Collects the stub responses / exceptions
 */
public class HubResponseCollector<T> {
    T response;
    CountDownLatch latch;
    Throwable exception;

    public HubResponseCollector(CountDownLatch latch) {
        this.latch = latch;
    }

    public T getResponse() {
        return this.response;
    }

    public void countDown() {
        latch.countDown();
    }

    public void setResponse(T response) {
        this.response = response;
    }


    public Throwable getException() {
        return this.exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
        }

}