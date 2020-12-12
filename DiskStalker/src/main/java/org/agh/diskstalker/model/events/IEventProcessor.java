package org.agh.diskstalker.model.events;

public interface IEventProcessor {
    void processEvent(EventObject eventObject);
}
