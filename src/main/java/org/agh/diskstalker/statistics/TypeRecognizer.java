package org.agh.diskstalker.statistics;

import io.reactivex.rxjava3.subjects.PublishSubject;
import lombok.extern.slf4j.Slf4j;
import org.agh.diskstalker.model.interfaces.IObservedFolder;
import org.agh.diskstalker.model.tree.NodeData;
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
    private final HashMap<IObservedFolder, PublishSubject<TypeRecognizedEvent>> eventStreams = new HashMap<>();

    private TypeRecognizer(){

    }

    public static TypeRecognizer getInstance(){ //TODO: refactor this, spring Service
        if(INSTANCE == null){
            log.info("Started TypeRecognizer");
            INSTANCE = new TypeRecognizer();
        }

        return INSTANCE;
    }


    public void recognize(IObservedFolder folder, NodeData nodeData){
        if(nodeData.isFile()){
            executor.submit(recognizeCallable(folder, nodeData));
        }
    }

    private Runnable recognizeCallable(IObservedFolder folder, NodeData nodeData){
        return () -> {
            var nodePath = nodeData.getPath();
            var outputEvent = new TypeRecognizedEvent();
            var eventStream = eventStreams.get(folder);

            if(eventStream == null){
                log.error("This folder is not registered!");
                throw new IllegalStateException("This folder is not registered!");
            }

            try {
                var type = typeRecognizer.detect(nodePath.toUri().toURL());
                outputEvent.setType(type);
            } catch (IOException exception) {
                log.warn("Cannot recognize file type " + nodePath);
                outputEvent.setType(TypeRecognizedEvent.UNKNOWN_TYPE);
            }

            eventStream.onNext(outputEvent);
        };
    }

    public PublishSubject<TypeRecognizedEvent> register(IObservedFolder folder){
        var subject = PublishSubject.<TypeRecognizedEvent>create();
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
