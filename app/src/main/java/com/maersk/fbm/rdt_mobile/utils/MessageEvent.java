package com.maersk.fbm.rdt_mobile.utils;


public class MessageEvent<T> {
    public final String type;
    public final String message;

    public MessageEvent(String type, String message) {
        this.type = type;
        this.message = message;
    }
}
