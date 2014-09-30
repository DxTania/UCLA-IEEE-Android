package com.ucla_ieee.app.calendar;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Event class for calendar events
 */
public class Event implements Parcelable {
    public static final Creator<Event> CREATOR = new Creator<Event>() {
        public Event createFromParcel(Parcel source) {
            Event mEvent = new Event();
            mEvent.setSummary(source.readString());
            mEvent.setLocation(source.readString());
            mEvent.setCreatorEmail(source.readString());
            mEvent.setCreatorName(source.readString());
            mEvent.setAllDay(source.readInt() != 0);
            mEvent.setStartDate(new Date(source.readLong()));
            mEvent.setEndDate(new Date(source.readLong()));
            return mEvent;
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[0];
        }
    };
    private Date startDate;
    private Date endDate;
    private String summary;
    private String location;
    private String creatorEmail;
    private String creatorName;
    private String id;
    private boolean allDay;

    public Event() {
        startDate = null;
        endDate = null;
        summary = null;
        location = null;
        creatorEmail = null;
        creatorName = null;
        allDay = false;
        id = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getAllDay() {
        return allDay;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    /**
     * @return Date in MMM dd format
     */
    public String getDateString() {
        SimpleDateFormat format = new SimpleDateFormat("MMM dd");
        return format.format(getStartDate());
    }

    /**
     * @return "Time at Location" or "All Day at Location"
     */
    public String getLocationTime() {
        String time = "";
        String loc = getLocation() == null ? "" : " at " + getLocation();

        if (getStartDate() != null && !getAllDay()) {
            SimpleDateFormat format = new SimpleDateFormat("hh:mma");
            time = format.format(getStartDate());
        } else if (getAllDay()) {
            time = "All Day";
        }

        return time + loc;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(summary);
        dest.writeString(location);
        dest.writeString(creatorEmail);
        dest.writeString(creatorName);
        dest.writeInt(allDay ? 1 : 0);
        dest.writeLong(startDate.getTime());
        dest.writeLong(endDate.getTime());
    }
}
