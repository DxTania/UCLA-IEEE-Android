package com.ucla_ieee.app.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.ucla_ieee.app.R;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.List;

public class EventListAdapter extends ArrayAdapter<Event> {
    private final Context context;
    private final List<Event> values;

    static class ViewHolder {
        TextView summary;
        TextView location;
    }

    public EventListAdapter(Context context, List<Event> values) {
        super(context, R.layout.event_snippet, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.event_snippet, parent, false);
            viewHolder.summary = (TextView) convertView.findViewById(R.id.summaryText);
            viewHolder.location= (TextView) convertView.findViewById(R.id.locationText);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Event event = values.get(position);

        String time = "";
        String loc = event.getLocation() == null? "" : " at " + event.getLocation();

        if (event.getStartDate() != null && !event.getAllDay()) {
            SimpleDateFormat format = new SimpleDateFormat("hh:mma");
            time = format.format(event.getStartDate());
        } else if (event.getAllDay()) {
            time = "All Day";
        }

        viewHolder.summary.setText(event.getSummary());
        viewHolder.location.setText(time + loc + " ");
        return convertView;
    }
} 