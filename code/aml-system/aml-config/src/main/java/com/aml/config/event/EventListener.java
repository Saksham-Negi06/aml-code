package com.aml.config.event;

public interface EventListener<T> {
    void onEvent(T event);
}
