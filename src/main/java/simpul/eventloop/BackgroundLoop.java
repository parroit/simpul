package simpul.eventloop;


import simpul.Interfaces;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class BackgroundLoop {
    private final EventLoop eventLoop;

    private static final int BACKGROUND_THREADS = 10;

    private final ExecutorService backgroundLoop = Executors.newFixedThreadPool(BACKGROUND_THREADS);
    private final AtomicLong pendingCallbacks;

    public <T> void runInBackground(Callable<T> backgroundTask, Interfaces.Callback<T> cb){
        addPendingOperation();
        backgroundLoop.submit(()->{


            try {
                T result = backgroundTask.call();
                eventLoop.runTicket(() -> cb.invoke(null, result));
            } catch (Exception e) {
                eventLoop.runTicket(() -> cb.invoke(e,null));
            } finally {
                removePendingOperation();
            }
        });
    }

    public void addPendingOperation() {
        pendingCallbacks.incrementAndGet();
    }
    public void removePendingOperation() {
        pendingCallbacks.decrementAndGet();
    }

    public long pendingOperations() {
        return pendingCallbacks.get();
    }

    BackgroundLoop(EventLoop eventLoop) {
        this.eventLoop = eventLoop;

        pendingCallbacks = new AtomicLong();
    }
}
