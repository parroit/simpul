package simpul.eventloop;

import simpul.Interfaces;
import simpul.core.EventEmitter;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The main event loop.
 * It execute Runnable interface in sequence, in a single thread ExecutorService.
 * <p>
 * Each Runnable interface is called a ticket.
 * <p>
 * The event loop run while there are more ticket scheduled to execute,
 * or while there are pending operation in the BackgroundLoop.
 * When there are no tickets and no background operations pending, the ExecutorService
 * is shutdown, and event loop terminate
 */
public class EventLoop implements Interfaces.EventEmitter {

    private final ExecutorService service;
    private final AtomicLong eventsInLoop;
    private final Interfaces.EventEmitter events;

    public <T> void runInBackground(Callable<T> backgroundTask, Interfaces.Callback<T> cb) {
        background.runInBackground(backgroundTask, cb);
    }


    public void addBackgroundOperation() {
        background.addPendingOperation();
    }

    public void removeBackgroundOperation() {
        background.removePendingOperation();
    }

    private final BackgroundLoop background;


    public EventLoop() {
        service = Executors.newSingleThreadExecutor();
        events = new EventEmitter();
        eventsInLoop = new AtomicLong();
        background = new BackgroundLoop(this);
    }


    /**
     * Wait for event loop termination for a number of milliseconds, then return.
     * This method is mainly useful for testing purpose, the normal use of EventLoop
     * does not need it, because the program is kept running while the event loop is running.
     *
     * @param milliseconds number of second to wait for termination
     * @return true if the event loop is terminated, false if the timeout elapsed
     * while the event loop is still running.
     */
    public boolean await(long milliseconds) {
        try {
            service.awaitTermination(milliseconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return service.isShutdown();
    }


    /**
     * Run the specified ticket in the event loop.
     * The ticket run after all others schedule tickets have run.
     *
     * @param ticket The ticket to run
     */
    public void runTicket(final Runnable ticket) {
        eventsInLoop.incrementAndGet();
        service.execute(() -> {
            try {
                ticket.run();

            } catch (Throwable t) {
                runTicket(() -> emit("uncaughtException", t));


            } finally {

                long inLoop = eventsInLoop.decrementAndGet();


                if (inLoop == 0 && background.pendingOperations() == 0) {
                    service.shutdown();
                }
            }


        });
    }


    @Override
    public <T> void on(String event, Interfaces.EventCallback<T> cb) {
        events.on(event, cb);
    }

    @Override
    public <T> void emit(String event, T data) {
        events.emit(event, data);
    }

    @Override
    public <T> void removeListener(String event, Interfaces.EventCallback<T> cb) {
        events.removeListener(event, cb);
    }

    @Override
    public void removeAllListeners(String event) {
        events.removeAllListeners(event);
    }

    @Override
    public <T> void once(String event, Interfaces.EventCallback<T> cb) {
        events.once(event, cb);
    }
}