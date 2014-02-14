package simpul.eventloop;


import java.util.concurrent.atomic.AtomicLong;

public enum BackgroundLoop {
    INSTANCE;

    private final AtomicLong pendingCallbacks;

    public long pendingOperations() {
        return pendingCallbacks.get();
    }

    BackgroundLoop() {

        pendingCallbacks = new AtomicLong();
    }
}
