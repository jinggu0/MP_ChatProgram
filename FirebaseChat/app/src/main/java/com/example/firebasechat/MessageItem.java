package com.example.firebasechat;

public class MessageItem {
    public String message;
    public String nick;
    public String uid;

    public MessageItem() {
    }

    public MessageItem(String message, String nick, String uid) {
        this.message = message;
        this.nick = nick;
        this.uid = uid;
    }
}