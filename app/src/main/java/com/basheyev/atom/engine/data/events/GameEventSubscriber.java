package com.basheyev.atom.engine.data.events;

public interface GameEventSubscriber {

    boolean onGameEvent(GameEvent event);

}
