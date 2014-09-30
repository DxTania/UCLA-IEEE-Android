package com.ucla_ieee.app;

/**
 * Created by rawrtan on 9/29/14.
 */
public class News {
    private String content;
    private String type;
    private String date;

    public News(String content, String date, String type) {
        this.content = content;
        this.date = date;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
