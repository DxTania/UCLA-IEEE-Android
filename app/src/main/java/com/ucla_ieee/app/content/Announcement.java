package com.ucla_ieee.app.content;

/**
 * Class that represents a single announcement
 */
public class Announcement {
    private String content;
    private String date;
    private int uid;

    public Announcement(String content, String date, int uid) {
        this.content = content;
        this.date = date;
        this.uid = uid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getUid() {
        return uid;
    }
}
