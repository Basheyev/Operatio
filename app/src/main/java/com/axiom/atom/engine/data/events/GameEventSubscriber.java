package com.axiom.atom.engine.data.events;

public interface GameEventSubscriber {

    boolean onGameEvent(GameEvent event);

}
