package com.ucla_ieee.app.calendar;

import java.util.Comparator;

/**
 * Created by rawrtan on 5/15/14.
 */
public class DateComp implements Comparator<Event> {

    @Override
    public int compare(Event lhs, Event rhs) {
        return lhs.getStartDate().compareTo(rhs.getStartDate());
    }
}
