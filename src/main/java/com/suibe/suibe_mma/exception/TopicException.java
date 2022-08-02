package com.suibe.suibe_mma.exception;

public class TopicException extends RuntimeException{
    public TopicException(String message) {
        super(message);
    }

    public TopicException(String message, Exception e) {
        super(message, e);
    }
}
