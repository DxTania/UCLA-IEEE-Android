package com.ucla_ieee.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class NewsFeedListAdapter extends ArrayAdapter<News> {
    private final Context context;
    private final List<News> news;

    public NewsFeedListAdapter(Context context, List<News> news) {
        super(context, R.layout.snippet_news, news);
        this.context = context;
        this.news = news;
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
            convertView = inflater.inflate(R.layout.snippet_news, parent, false);
            viewHolder.location = (TextView) convertView.findViewById(R.id.locationText);
            viewHolder.summary = (TextView) convertView.findViewById(R.id.summaryText);
            viewHolder.type = (ImageView) convertView.findViewById(R.id.type);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        News newsSnippet = news.get(position);
        viewHolder.location.setText(newsSnippet.getDate());
        viewHolder.summary.setText(newsSnippet.getContent());

        if (newsSnippet.getType().equals("calendar")) {
            viewHolder.type.setImageResource(R.drawable.calendar);
        } else {
            viewHolder.type.setImageResource(R.drawable.bullhorn);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView summary;
        TextView location;
        ImageView type;
    }
} 