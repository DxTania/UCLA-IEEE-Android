package com.ucla_ieee.app.calendar;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class to convert json event objects to event objects
 */
public class EventManager {

    public static ArrayList<Event> createEvents(JsonArray events, List<Event> cancelled) {
        ArrayList<Event> allEvents = new ArrayList<Event>();
        for (JsonElement jEvent : events) {
            Event event = new Event();
            JsonObject eventObj = jEvent.getAsJsonObject();

            JsonPrimitive id = eventObj.getAsJsonPrimitive("id");
            if (id != null) {
                event.setId(id.getAsString());
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

            JsonPrimitive status = eventObj.getAsJsonPrimitive("status");
            if (status != null && status.getAsString().equals("cancelled")) {
                cancelled.add(event);
                continue;
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

    // Returns a json array string including all new items and previous items
    // that do not match any ids in the new items
    public static String reviseJson(JsonArray newItems, JsonArray prevItems) {
        JsonArray items = new JsonArray();
        for (JsonElement pItem : prevItems) {
            boolean match = false;
            for (JsonElement item : newItems) {
                String newId = item.getAsJsonObject().getAsJsonPrimitive("id").getAsString();
                String prevId = pItem.getAsJsonObject().getAsJsonPrimitive("id").getAsString();
                if (newId.equals(prevId)) {
                    // Item is stale
                    match = true;
                    break;
                }
            }
            if (!match) {
                items.add(pItem);
            }
        }
        for (JsonElement nItem : newItems) {
            if (!nItem.getAsJsonObject().getAsJsonPrimitive("status")
                    .getAsString().equals("cancelled")) {
                items.add(nItem);
            }
        }
        return items.toString();
    }

    // Removes events from prevEvents with matching id in newEvents
    public static void removeStaleEvents(List<Event> newEvents, List<Event> prevEvents, boolean cancelled) {
        for (Iterator<Event> it = prevEvents.iterator(); it.hasNext(); ) {
            Event event = it.next();
            for (Event nEvent : newEvents) {
                if (event.getId().equals(nEvent.getId())) {
                    if (cancelled) {
                        nEvent.setStartDate(event.getStartDate()); // what??
                    }
                    it.remove();
                }
            }
        }
    }
}
