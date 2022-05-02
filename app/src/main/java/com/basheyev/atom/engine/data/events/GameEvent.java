package com.basheyev.atom.engine.data.events;


/**
 * Игровое событие для уведомлений
 */
public class GameEvent {

    public static final int ALL_TOPICS = 0;

    private final int topic;
    private final Object payload;

    public GameEvent(int topic, Object payload) {
        this.topic = topic;
        this.payload = payload;
    }

    public int getTopic() {
        return topic;
    }

    public Object getPayload() {
        return payload;
    }
}
