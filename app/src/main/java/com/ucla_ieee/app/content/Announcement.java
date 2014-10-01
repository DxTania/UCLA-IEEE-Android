package com.ucla_ieee.app.content;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents a single announcement
 */
public class Announcement {
    private String content;
    private Date date;
    private int id;
    private boolean unread;

    public Announcement(Boolean unread, String content, String date, int id) {
        this.unread = unread;
        this.content = content;
        try {
            this.date = (new SimpleDateFormat("yyyy-MM-dd")).parse(date);
        } catch (ParseException e) {
            this.date = new Date();
        }
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public String getDateString() {
        SimpleDateFormat format = new SimpleDateFormat("MMM dd");
        return format.format(date);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public boolean getUnread() {
        return unread;
    }
}
