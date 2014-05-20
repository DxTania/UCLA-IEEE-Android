package com.ucla_ieee.app.calendar;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.ucla_ieee.app.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EventListAdapter extends ArrayAdapter<Event> {
    private final Context context;
    private final List<Event> events;

    static class ViewHolder {
        TextView summary;
        TextView location;
        ImageView checkIn;
    }

    public EventListAdapter(Context context, List<Event> events) {
        super(context, R.layout.event_snippet, events);
        this.context = context;
        this.events = events;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.event_snippet, parent, false);
            viewHolder.summary = (TextView) convertView.findViewById(R.id.summaryText);
            viewHolder.location = (TextView) convertView.findViewById(R.id.locationText);
            viewHolder.checkIn = (ImageView) convertView.findViewById(R.id.eventCheckIn);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Event event = events.get(position);

        String time = "";
        String loc = event.getLocation() == null? "" : " at " + event.getLocation();

        if (event.getStartDate() != null && !event.getAllDay()) {
            SimpleDateFormat format = new SimpleDateFormat("hh:mma");
            time = format.format(event.getStartDate());
        } else if (event.getAllDay()) {
            time = "All Day";
        }

        // TODO: Update this as time goes on
        Date now = new Date();
        if (event.getStartDate() != null && event.getEndDate() != null) {
            if (event.getStartDate().compareTo(now) <= 0 && event.getEndDate().compareTo(now) > 0) {
                viewHolder.checkIn.setVisibility(View.VISIBLE);
            } else {
                viewHolder.checkIn.setVisibility(View.INVISIBLE);
            }
        } else if (event.getStartDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            if (sdf.format(event.getStartDate()).equals(sdf.format(now))) {
                viewHolder.checkIn.setVisibility(View.VISIBLE);
            } else {
                viewHolder.checkIn.setVisibility(View.INVISIBLE);
            }
        }
        viewHolder.checkIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                ((CalendarActivity) context).startActivityForResult(intent, 0);
            }
        });

        viewHolder.summary.setText(event.getSummary());
        viewHolder.location.setText(time + loc + " ");
        return convertView;
    }
} 