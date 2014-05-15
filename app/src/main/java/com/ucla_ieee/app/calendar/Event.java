package com.ucla_ieee.app.calendar;

import java.util.Date;

/**
 * Event class for calendar events
 */
public class Event {
    private Date mStartDate;
    private Date mEndDate;
    private String mSummary;
    private String mLocation;

    private String creatorEmail;
    private String creatorName;

    public Event() {
        mStartDate = null;
        mEndDate = null;
        mSummary = null;
        mLocation = null;
        creatorEmail = null;
        creatorName = null;
    }

    public Date getmStartDate() {
        return mStartDate;
    }

    public void setmStartDate(Date mStartDate) {
        this.mStartDate = mStartDate;
    }

    public Date getmEndDate() {
        return mEndDate;
    }

    public void setmEndDate(Date mEndDate) {
        this.mEndDate = mEndDate;
    }

    public String getmSummary() {
        return mSummary;
    }

    public void setmSummary(String mSummary) {
        this.mSummary = mSummary;
    }

    public String getmLocation() {
        return mLocation;
    }

    public void setmLocation(String mLocation) {
        this.mLocation = mLocation;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }
}
