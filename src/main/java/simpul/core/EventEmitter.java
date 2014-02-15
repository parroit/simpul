package simpul.core;

import simpul.Interfaces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventEmitter implements Interfaces.EventEmitter{
    private class RegisteredCallback{
        private final Interfaces.EventCallback cb;
        private final boolean once;

        private RegisteredCallback(Interfaces.EventCallback cb, boolean once) {
            this.cb = cb;
            this.once = once;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RegisteredCallback that = (RegisteredCallback) o;

            return cb.equals(that.cb);

        }

        @Override
        public int hashCode() {
            return cb.hashCode();
        }
    }


    private final Map<String,List<RegisteredCallback>> listeners;

    public EventEmitter() {
        listeners = new HashMap<>();
    }

    @Override
    public <T> void on(String event, Interfaces.EventCallback<T> cb) {
        addListener(event, cb, false);
    }

    private <T> void addListener(String event, Interfaces.EventCallback<T> cb, boolean once) {
        List<RegisteredCallback> callbacks = listeners.get(event);
        if (callbacks == null) {
            callbacks = new ArrayList<>();
            listeners.put(event,callbacks);
        }
        callbacks.add(new RegisteredCallback(cb,once));
    }

    @Override
    public <T> void once(String event, Interfaces.EventCallback<T> cb) {
        addListener(event, cb, true);
    }

    @Override
    public <T> void emit(String event, T data)  {
        List<RegisteredCallback> callbacks = listeners.get(event);
        if (callbacks == null) {
            return;
        }

        for (RegisteredCallback registeredCallback: callbacks) {
            if (registeredCallback.once) {
                //noinspection unchecked
                removeListener(event,registeredCallback.cb);
            }

            //noinspection unchecked
            registeredCallback.cb.invoke(data);


        }
    }

    @Override
    public <T> void removeListener(String event, Interfaces.EventCallback<T> cb) {
        List<RegisteredCallback> callbacks = listeners.get(event);
        if (callbacks == null) {
            return;
        }

        callbacks.remove(new RegisteredCallback(cb,false));
    }

    @Override
    public void removeAllListeners(String event) {
        listeners.remove(event);
    }

    @Override
    public int listeners(String event) {
        List<RegisteredCallback> callbacks = listeners.get(event);
        if (callbacks == null) {
            return 0;
        }

        return callbacks.size();

    }
}
