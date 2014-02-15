package simpul.fs;


import simpul.Interfaces;
import simpul.core.ReadableStream;
import simpul.eventloop.EventLoop;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Reader {

    private final EventLoop eventLoop;


    public Reader(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    private class ReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer>{
        private final Interfaces.Callback<ByteBuffer> cb;
        private final AsynchronousFileChannel channel;
        private final boolean autoClose;

        private ReadCompletionHandler(Interfaces.Callback<ByteBuffer> cb, AsynchronousFileChannel channel, boolean autoClose) {
            this.cb = cb;

            this.channel = channel;
            this.autoClose = autoClose;
        }

        @Override
        public void completed(Integer result, ByteBuffer buffer) {
            if (autoClose && channel.isOpen()) {
                try {
                    channel.close();
                } catch (IOException ignored) {

                }
            }

            eventLoop.runTicket(()-> cb.invoke(null,buffer));
            eventLoop.removeBackgroundOperation();
        }

        @Override
        public void failed(Throwable exc, ByteBuffer buffer) {
            if (channel.isOpen()) {
                try {
                    channel.close();
                } catch (IOException ignored) {

                }
            }

            eventLoop.runTicket(()-> cb.invoke(exc,null));
            eventLoop.removeBackgroundOperation();
        }
    }

    private Interfaces.Callback<Long> makeReader(AsynchronousFileChannel channel,boolean autoClose,long position,Interfaces.Callback<ByteBuffer> cb ) {

        return (err, fileSize) -> {
            if (fileSize > Integer.MAX_VALUE) {
                String message = "File size " + fileSize + " exceeds maximum allowed value. Use fs.createReadStream to read big files.";
                eventLoop.runTicket(() -> cb.invoke(new IOException(message),null));
                return;
            }

            if (err != null) {
                eventLoop.runTicket(()-> cb.invoke(err,null));
                return;
            }

            eventLoop.addBackgroundOperation();

            ByteBuffer buffer = ByteBuffer.allocate(fileSize.intValue());


            CompletionHandler<Integer, ByteBuffer> completion = new ReadCompletionHandler(cb,channel,autoClose);
            channel.read(buffer, position, buffer, completion );


        };
    }

    public void readFile(String path,String encoding, Interfaces.Callback<String> cb){
        Path file = Paths.get(path);
        AsynchronousFileChannel channel = openFileChannel(cb, file);

        if (channel == null) {
            return;
        }
        eventLoop.runInBackground(() -> Files.size(file), makeReader(channel,true,0,(err,buffer)->{
            Charset charset = Charset.forName(encoding);
            cb.invoke(null, new String(buffer.array(), 0, buffer.position(), charset));
        }));

    }

    private <T>AsynchronousFileChannel openFileChannel(Interfaces.Callback<T> cb, Path file) {
        AsynchronousFileChannel channel = null;
        try {
            channel = AsynchronousFileChannel.open(file);

        } catch (IOException e) {

            eventLoop.runTicket(()-> cb.invoke(new RuntimeException(e),null));
        }

        return channel;
    }


    public void readFile(String path, Interfaces.Callback<ByteBuffer> cb){
        Path file = Paths.get(path);
        AsynchronousFileChannel channel = openFileChannel(cb, file);

        if (channel == null) {
            return;
        }

        eventLoop.runInBackground(() -> Files.size(file), makeReader(channel,true,0,cb));
    }

    public Interfaces.ReadableStream createReadStream(String path,String encoding) {
        Interfaces.ReadableStream stream = new ReadableStream();
        Path file = Paths.get(path);

        AsynchronousFileChannel channel = openFileChannel((err,data)-> {
            if (err != null) {
                stream.emit("error",err);
            }
        }, file);


        if (channel == null) {
            return stream;
        }

        readChunk(encoding, stream, channel, 0L);

        return stream;
    }

    private void readChunk(String encoding, Interfaces.ReadableStream stream, AsynchronousFileChannel channel, long position) {
        Interfaces.Callback<Long> reader = makeReader(channel, false, position, (err, buffer) -> {
            Charset charset = Charset.forName(encoding);
            int bytesRead = buffer.position();
            if (bytesRead > 0 ){
                String chunk = new String(buffer.array(), 0, bytesRead, charset);
                stream.emit("data",chunk);
                readChunk(encoding, stream, channel, position+ bytesRead);
            } else {
                stream.emit("end",null);
            }

        });

        reader.invoke(null,2L);
    }

}
