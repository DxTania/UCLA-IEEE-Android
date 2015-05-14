package com.ucla_ieee.app.content;

import com.google.gson.JsonObject;

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

    public Announcement(JsonObject jsonObject) {
        this.unread = jsonObject.get("unread").getAsBoolean();
        this.content = jsonObject.get("content").getAsString();
        try {
            this.date = (new SimpleDateFormat("yyyy-MM-dd"))
                    .parse(jsonObject.get("datePosted").getAsString());
        } catch (ParseException e) {
            this.date = new Date();
        }
        this.id = jsonObject.get("id").getAsInt();
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
