package org.agh.diskstalker.statistics;

import io.reactivex.rxjava3.subjects.PublishSubject;
import lombok.extern.slf4j.Slf4j;
import org.agh.diskstalker.model.interfaces.IObservedFolder;
import org.apache.tika.Tika;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
public class TypeRecognizer {
    private static final int THREADS_NUM = 2;
    private static TypeRecognizer INSTANCE;

    private final Tika typeRecognizer = new Tika();
    private final ExecutorService executor = Executors.newFixedThreadPool(THREADS_NUM);
    private final HashMap<IObservedFolder, PublishSubject<AbstractRecognizeTypeMessage>> eventStreams = new HashMap<>();

    private TypeRecognizer(){

    }

    public static TypeRecognizer getInstance(){ //TODO: refactor this, spring Service
        if(INSTANCE == null){
            log.info("Started TypeRecognizer");
            INSTANCE = new TypeRecognizer();
        }

        return INSTANCE;
    }

    public void recognize(AbstractRecognizeTypeMessage message){
        executor.submit(recognizeCallable(message));
    }

    private Runnable recognizeCallable(AbstractRecognizeTypeMessage message){
        var nodeData = message.getNodeData();
        var folder = message.getFolder();
        if(nodeData.isRemoved() || nodeData.isDirectory()){
            return () -> {};
        }

        return () -> {
            var nodePath = nodeData.getPath();
            var eventStream = eventStreams.get(folder);

            if(eventStream == null){
                return;
            }

            try {
                var type = typeRecognizer.detect(nodePath.toUri().toURL());
                message.setType(type);
            } catch (IOException exception) {
                message.setType(AbstractRecognizeTypeMessage.UNKNOWN_TYPE);
            }

            nodeData.setType(message.getType());
            eventStream.onNext(message);
        };
    }

    public PublishSubject<AbstractRecognizeTypeMessage> register(IObservedFolder folder){
        var subject = PublishSubject.<AbstractRecognizeTypeMessage>create();
        eventStreams.putIfAbsent(folder, subject);
        return subject;
    }

    public void unregister(IObservedFolder folder){
        var e = eventStreams.remove(folder);
        if(e != null){
            e.onComplete();
        }
    }

    public void stop(){
        log.info("Stopping TypeRecognizer");
        executor.shutdownNow();
    }
}
