package simpul.core;

import simpul.eventloop.EventLoop;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class Timers{
    private final EventLoop eventLoop;

    private long nextId;
    private final Timer systemTimer;
    private final ConcurrentHashMap<Long,Task> scheduled;

    Timers(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
        systemTimer = new Timer("system-timer", true);
        scheduled = new ConcurrentHashMap<>();
        nextId = 0;
    }


    private class Task extends TimerTask{
        private final Runnable runnable;
        private boolean interval;
        private long id;

        public Task(Runnable runnable, boolean interval, long id){
            eventLoop.addBackgroundOperation();
            this.runnable = runnable;
            this.interval = interval;
            this.id = id;
        }

        public void run(){
            if (!interval) {
                scheduled.remove(id);
                eventLoop.removeBackgroundOperation();
            }

            eventLoop.runTicket(runnable);
        }
    }

    /**
     * Schedule a callback to be execute on event loop after delay milliseconds
     * @param cb The callback to schedule
     * @param delay delay in milliseconds
     * @return an id that could be used to cancel schedule
     */
    public long setTimeout(Runnable cb, long delay) {
        Task task = new Task(cb, false, nextId++);
        systemTimer.schedule(task, delay);

        scheduled.put(task.id, task);
        return task.id;
    }

    /**
     * Schedule a callback to be execute on event loop each `interval` milliseconds
     * @param cb The callback to schedule
     * @param interval interval in milliseconds
     * @return an id that could be used to cancel schedule
     */
    public long setInterval(Runnable cb, long interval) {
        Task task = new Task(cb, true, nextId++);
        systemTimer.schedule(task, interval, interval);
        scheduled.put(task.id, task);
        return task.id;
    }

    /**
     * Cancel an interval schedule specified by id
     * @param id The id of the interval
     */
    public void clearInterval(long id) {
        clearTimer(id);
    }


    /**
     * Cancel a timeout schedule specified by id
     * @param id The id of the timeout
     */
    public void clearTimeout(long id) {
        clearTimer(id);
    }

    private void clearTimer(long id) {
        Task task = scheduled.get(id);
        if (task != null) {
            task.cancel();
            scheduled.remove(id);
            eventLoop.removeBackgroundOperation();
        }
    }


}