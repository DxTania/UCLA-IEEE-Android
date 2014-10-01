package com.ucla_ieee.app.content;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.ucla_ieee.app.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AnnouncementsListAdapter extends ArrayAdapter<Announcement> {
    private final Context context;
    private final List<Announcement> announcements;

    public AnnouncementsListAdapter(Context context, List<Announcement> announcements) {
        super(context, R.layout.snippet_event, announcements);
        this.context = context;
        this.announcements = announcements;
        sort();
    }

    public void sort() {
        Collections.sort(announcements, new Comparator<Announcement>() {
            @Override
            public int compare(Announcement lhs, Announcement rhs) {
                return rhs.getDate().compareTo(lhs.getDate());
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
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.snippet_announcement, parent, false);
            viewHolder.content = (TextView) convertView.findViewById(R.id.contentText);
            viewHolder.date = (TextView) convertView.findViewById(R.id.datePosted);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Announcement announcement = announcements.get(position);

        viewHolder.content.setText(announcement.getContent());
        viewHolder.date.setText(announcement.getDateString());

        if (announcement.getUnread()) {
            convertView.setBackgroundResource(R.drawable.unread_border);
        } else {
            convertView.setBackgroundColor(context.getResources().getColor(R.color.caldroid_transparent));
        }

        return convertView;
    }

    static class ViewHolder {
        TextView content;
        TextView date;
    }
} 