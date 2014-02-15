package simpul.core;

import simpul.Interfaces;

import java.util.ArrayList;
import java.util.List;

public class ReadableStream<TData> extends EventEmitter implements Interfaces.ReadableStream {
    private final ArrayList<TData> buffer = new ArrayList<>();
    private final ArrayList<RuntimeException> errors = new ArrayList<>();

    @Override
    public <T> void on(String event, Interfaces.EventCallback<T> cb) {
        super.on(event, cb);
        drain(event, "data", buffer, false);
        drain(event, "error", errors, false);

    }

    private <T> void drain(String event, String drainedEvent, ArrayList<T> drainedBuffer, boolean one) {
        if (drainedEvent.equals(event) && drainedBuffer.size() > 0) {
            //noinspection unchecked
            for (T chunk : (List<T>) drainedBuffer.clone()) {
                emit(drainedEvent, chunk);
                drainedBuffer.remove(chunk);
                if (one) {
                    return;
                }
            }


        }
    }

    @Override
    public <T> void once(String event, Interfaces.EventCallback<T> cb) {
        super.once(event, cb);
        drain(event, "data", buffer, true);
        drain(event, "error", errors, true);
    }

    @Override
    public <T> void emit(String event, T data) {
        if (listeners(event) > 0) {
            super.emit(event, data);
        } else {
            if ("data".equals(event)){
                //noinspection unchecked
                buffer.add((TData) data);
            } else if ("error".equals(event)){
                //noinspection unchecked
                errors.add((RuntimeException) data);
            }

        }
    }


}
