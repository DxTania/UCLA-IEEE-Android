package com.ucla_ieee.app.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.gson.JsonArray;
import com.ucla_ieee.app.R;
import com.ucla_ieee.app.user.SessionManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EventListAdapter extends ArrayAdapter<Event> {
    private final Context mContext;
    private final List<Event> mEvents;
    private final SessionManager mSessionManager;

    public EventListAdapter(Context mContext, List<Event> events) {
        super(mContext, R.layout.snippet_event, events);
        this.mContext = mContext;
        this.mEvents = events;
        this.mSessionManager = new SessionManager(mContext);
        sort();
    }

    public void sort() {
        Collections.sort(mEvents, new Comparator<Event>() {
            @Override
            public int compare(Event lhs, Event rhs) {
                return lhs.getStartDate().compareTo(rhs.getStartDate());
            }
        });
    }

    @Override
    public void notifyDataSetChanged() {
        sort();
        super.notifyDataSetChanged();
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.snippet_event, parent, false);
            viewHolder.summary = (TextView) convertView.findViewById(R.id.summaryText);
            viewHolder.location = (TextView) convertView.findViewById(R.id.locationText);
            viewHolder.checkIn = (ImageView) convertView.findViewById(R.id.eventCheckIn);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Event event = mEvents.get(position);
        JsonArray attendedEvents = mSessionManager.getAttendedEvents();
        if (attendedEvents != null) {
            for (int i = 0; i < attendedEvents.size(); i++) {
                if (attendedEvents.get(i).getAsJsonObject().get("event_id").getAsString().equals(event.getId())) {
                    viewHolder.checkIn.setVisibility(View.VISIBLE);
                    break;
                }
            }
        }
        viewHolder.summary.setText(event.getSummary());
        viewHolder.location.setText(event.getLocationTime());

        return convertView;
    }

    static class ViewHolder {
        TextView summary;
        TextView location;
        ImageView checkIn;
    }
} 