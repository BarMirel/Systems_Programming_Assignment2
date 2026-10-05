package bgu.spl.mics.application.messages;


import bgu.spl.mics.Broadcast;


public class CrashedBroadcast implements Broadcast {
    private final String senderName;
    private final String description;
    public CrashedBroadcast(String senderName, String description) {
        this.senderName = senderName;
        this.description = description;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getDescription() {
        return description;
    }
}