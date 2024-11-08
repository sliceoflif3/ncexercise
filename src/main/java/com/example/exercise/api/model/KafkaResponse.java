package com.example.exercise.api.model;

import java.util.List;

public class KafkaResponse {
    private List<String> batchOfEvents;
    private int numberOfMessagesLeft;
    private boolean hasMoreMessages;

    public KafkaResponse(List<String> batchOfEvents, int numberOfMessagesLeft, boolean hasMoreMessages) {
        this.batchOfEvents = batchOfEvents;
        this.numberOfMessagesLeft = numberOfMessagesLeft;
        this.hasMoreMessages = hasMoreMessages;
    }

    public List<String> getBatchOfEvents() {
        return batchOfEvents;
    }

    public void setBatchOfEvents(List<String> batchOfEvents) {
        this.batchOfEvents = batchOfEvents;
    }

    public int getNumberOfMessagesLeft() {
        return numberOfMessagesLeft;
    }

    public void setNumberOfMessagesLeft(int numberOfMessagesLeft) {
        this.numberOfMessagesLeft = numberOfMessagesLeft;
    }

    public boolean isHasMoreMessages() {
        return hasMoreMessages;
    }

    public void setHasMoreMessages(boolean hasMoreMessages) {
        this.hasMoreMessages = hasMoreMessages;
    }
}
