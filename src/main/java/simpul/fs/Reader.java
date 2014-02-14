package simpul.fs;


import simpul.Interfaces;
import simpul.eventloop.EventLoop;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiFunction;

public class Reader {

    private final EventLoop eventLoop;


    public Reader(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    private class ReadCompletionHandler<ResultType> implements CompletionHandler<Integer, ByteBuffer>{
        private final Interfaces.Callback<ResultType> cb;
        private final BiFunction<ByteBuffer,Long, ResultType> resultTransformer;
        private final long fileSize;

        private ReadCompletionHandler(Interfaces.Callback<ResultType> cb, BiFunction<ByteBuffer, Long, ResultType> resultTransformer, long fileSize) {
            this.cb = cb;
            this.resultTransformer = resultTransformer;
            this.fileSize = fileSize;
        }

        @Override
        public void completed(Integer result, ByteBuffer buffer) {

            eventLoop.runTicket(()-> cb.invoke(null,resultTransformer.apply(buffer, fileSize)));
            eventLoop.removeBackgroundOperation();
        }

        @Override
        public void failed(Throwable exc, ByteBuffer buffer) {
            eventLoop.runTicket(()-> cb.invoke(exc,null));
            eventLoop.removeBackgroundOperation();
        }
    }

    private <T> Interfaces.Callback<Long> makeReader(Path file,Interfaces.Callback<T> cb,BiFunction<ByteBuffer,Long,T> resultTransformer ) {

        return (err, fileSize) -> {
            if (fileSize > Integer.MAX_VALUE) {
                String message = "File size " + fileSize + " exceeds maximum allowed value. Use fs.createReadStream to read big files.";
                eventLoop.runTicket(() -> cb.invoke(new IOException(message),null));
                eventLoop.removeBackgroundOperation();
                return;
            }

            if (err != null) {
                eventLoop.runTicket(()-> cb.invoke(err,null));
                eventLoop.removeBackgroundOperation();
                return;
            }

            CompletionHandler<Integer, ByteBuffer> completion = new ReadCompletionHandler<>(cb,resultTransformer,fileSize);


            try {
                eventLoop.addBackgroundOperation();
                AsynchronousFileChannel channel = AsynchronousFileChannel.open(file);


                ByteBuffer buffer = ByteBuffer.allocate(fileSize.intValue());

                channel.read(buffer, 0, buffer, completion );

            } catch (IOException e) {
                eventLoop.runTicket(()-> cb.invoke(e,null));
                eventLoop.removeBackgroundOperation();

            }
        };
    }

    public void readFile(String path,String encoding, Interfaces.Callback<String> cb){
        Path file = Paths.get(path);

        eventLoop.runInBackground(() -> Files.size(file), makeReader(file,cb,(buffer,fileSize)->{
            Charset charset = Charset.forName(encoding);
            return new String(buffer.array(),0,fileSize.intValue(), charset);
        }));

    }


    public void readFile(String path, Interfaces.Callback<ByteBuffer> cb){
        Path file = Paths.get(path);

        eventLoop.runInBackground(() -> Files.size(file), makeReader(file,cb,(buffer,fileSize)-> buffer));
    }

}
