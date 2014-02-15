package simpul;


public final class Interfaces {
    private Interfaces(){}


    public static interface Callback<T> {
        void invoke(Throwable err,T data);
    }


    public static interface EventCallback<T> {
        void invoke(T data);
    }

    public static interface EventEmitter {
        <T>void on  (String event,EventCallback<T> cb);
        <T>void once  (String event,EventCallback<T> cb);
        <T>void emit  (String event,T data);
        <T>void removeListener(String event,EventCallback<T> cb);
        void removeAllListeners(String event);


    }
}
