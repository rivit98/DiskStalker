package model.events;

public interface IEventProcessor {
    void processEvent(EventObject eventObject);
}
