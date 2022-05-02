package com.basheyev.atom.engine.data.events;

import com.basheyev.atom.engine.data.structures.Channel;

import java.util.ArrayList;

/**
 * Диспетчер сообщений
 */
public class GameEventQueue {

    private Channel<GameEvent> events;
    private ArrayList<TopicSubscriber> subscribers;

    private static class TopicSubscriber {
        int topic;
        GameEventSubscriber eventSubscriber;
    }

    public GameEventQueue() {
        this(256);
    }

    public GameEventQueue(int maxQueueSize) {
        events = new Channel<>(maxQueueSize);
        subscribers = new ArrayList<>();
    }

    public boolean push(GameEvent event) {
        return events.push(event);
    }

    public void addSubscriber(GameEventSubscriber subscriber) {
        addSubscriber(subscriber, 0);
    }

    public void addSubscriber(GameEventSubscriber subscriber, int topic) {
        if (subscriber==null) return;
        TopicSubscriber topicSubscriber = new TopicSubscriber();
        topicSubscriber.topic = topic;
        topicSubscriber.eventSubscriber = subscriber;
        subscribers.add(topicSubscriber);
    }

    public void removeSubscriber(GameEventSubscriber subscriber) {
        for (TopicSubscriber topicSubscriber: subscribers) {
            if (topicSubscriber.eventSubscriber==subscriber) {
                subscribers.remove(topicSubscriber);
            }
        }
    }

    public void processEvent() {
        GameEvent event = events.poll();
        if (event == null) return;
        //Log.d("GAME EVENT", "topic = " + event.getTopic() + " payload=" + event.getPayload());
        for (int i=0; i<subscribers.size(); i++) {
            TopicSubscriber topicSubscriber = subscribers.get(i);
            int subscriberTopic = topicSubscriber.topic;
            if (subscriberTopic==GameEvent.ALL_TOPICS || subscriberTopic==event.getTopic()) {
                topicSubscriber.eventSubscriber.onGameEvent(event);
            }
        }
    }

    public int size() {
        return events.size();
    }

}
