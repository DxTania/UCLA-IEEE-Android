package com.ucla_ieee.app.calendar;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.*;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Helper class to convert json event objects to event objects
 */
public class EventCreator {

    public static ArrayList<Event> createEvents(JsonArray events) {
        ArrayList<Event> allEvents = new ArrayList<Event>();
        for (JsonElement jEvent : events) {
            Event event = new Event();
            JsonObject eventObj = jEvent.getAsJsonObject();

            JsonPrimitive status = eventObj.getAsJsonPrimitive("status");
            if (status != null && status.getAsString().equals("cancelled")) {
                continue;
            }

            JsonObject startObj = eventObj.getAsJsonObject("start");
            if (startObj == null) {
                startObj = eventObj.getAsJsonObject("originalStartTime");
            }
            if (startObj != null) {
                event.setStartDate(getDate(startObj));

                JsonPrimitive allDay = startObj.getAsJsonPrimitive("date");
                if (allDay != null) {
                    event.setAllDay(true);
                }
            }

            JsonObject endObj = eventObj.getAsJsonObject("end");
            if (endObj != null) {
                event.setEndDate(getDate(endObj));
            }

            JsonPrimitive summary = eventObj.getAsJsonPrimitive("summary");
            if (summary != null) {
                event.setSummary(summary.getAsString());
            }

            JsonPrimitive location = eventObj.getAsJsonPrimitive("location");
            if (location != null) {
                event.setLocation(location.getAsString());
            }

            JsonObject creator = eventObj.getAsJsonObject("creator");
            if (creator != null) {
                event.setCreatorEmail(getCreatorEmail(creator));
                event.setCreatorName(getCreatorName(creator));
            }

            allEvents.add(event);
        }
        return allEvents;
    }

    private static Date getDate(JsonObject d) {
        Date calDate = null;
        try {
            JsonPrimitive date = d.getAsJsonPrimitive("date");
            JsonPrimitive dateTime = d.getAsJsonPrimitive("dateTime");

            if (dateTime != null) {
                String dateString = dateTime.getAsString();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                calDate = format.parse(dateString);
            } else if (date != null) {
                String dateString = date.getAsString();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                calDate = format.parse(dateString);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calDate;
    }

    private static String getCreatorEmail(JsonObject c) {
        JsonPrimitive email = c.getAsJsonPrimitive("email");
        if (email != null) {
            return email.getAsString();
        }
        return null;
    }

    private static String getCreatorName(JsonObject c) {
        JsonPrimitive name = c.getAsJsonPrimitive("displayName");
        if (name != null) {
            return name.getAsString();
        }
        return null;
    }
}
