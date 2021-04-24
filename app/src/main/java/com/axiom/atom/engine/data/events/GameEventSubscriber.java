package com.axiom.atom.engine.data.events;

public interface GameEventSubscriber {

    boolean onEvent(GameEvent event);

}
