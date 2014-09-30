package com.ucla_ieee.app.newsfeed;

import java.util.Date;

/**
 * Holder for either and event or announcement in the form of news feed
 */
public class News {
    private String content;
    private String type;
    private String dateText;
    private String locationTime;
    private Date realDate;

    public News(String content, String dateText, String locationTime, String type, Date realDate) {
        this.content = content;
        this.dateText = dateText;
        this.locationTime = locationTime;
        this.type = type;
        this.realDate = realDate;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public String getDateText() {
        return dateText;
    }

    public String getLocationTime() {
        return locationTime;
    }

    public Date getRealDate() {
        return realDate;
    }
}
