package bgu.spl.mics.application.messages;


import bgu.spl.mics.Broadcast;


public class TerminatedBroadcast implements Broadcast {
    private final String senderName;
    public TerminatedBroadcast(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderName() {
        return senderName;
    }
}
