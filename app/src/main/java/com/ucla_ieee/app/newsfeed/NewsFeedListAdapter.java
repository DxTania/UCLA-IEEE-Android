package com.ucla_ieee.app.newsfeed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.ucla_ieee.app.R;

import java.util.*;

public class NewsFeedListAdapter extends ArrayAdapter<News> {
    private final Context context;
    private final List<News> news;

    public NewsFeedListAdapter(Context context, List<News> news) {
        super(context, R.layout.snippet_news, news);
        this.context = context;
        this.news = news;
        sort();
    }

    public void sort() {
        Collections.sort(news, new Comparator<News>() {
            @Override
            public int compare(News lhs, News rhs) {
                Calendar date = new GregorianCalendar();
                date.set(Calendar.HOUR_OF_DAY, 0);
                date.set(Calendar.MINUTE, 0);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.MILLISECOND, 0);
                long msToday = date.getTimeInMillis();
                long leftDistance = Math.abs(lhs.getRealDate().getTime() - msToday);
                long rightDistance = Math.abs(rhs.getRealDate().getTime() - msToday);
                return leftDistance > rightDistance ? 1 : -1; // sort by closest to today
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
            convertView = inflater.inflate(R.layout.snippet_news, parent, false);
            viewHolder.locationTime = (TextView) convertView.findViewById(R.id.locationText);
            viewHolder.dateText = (TextView) convertView.findViewById(R.id.dateText);
            viewHolder.summary = (TextView) convertView.findViewById(R.id.summaryText);
            viewHolder.type = (ImageView) convertView.findViewById(R.id.type);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        News newsSnippet = news.get(position);
        viewHolder.locationTime.setText(newsSnippet.getLocationTime());
        viewHolder.dateText.setText(newsSnippet.getDateText());
        viewHolder.summary.setText(newsSnippet.getContent());

        if (newsSnippet.getType().equals("calendar")) {
            viewHolder.type.setImageResource(R.drawable.calendar);
        } else {
            viewHolder.type.setImageResource(R.drawable.bullhorn);
        }

        // TODO: Have unread markers for these too

        return convertView;
    }

    static class ViewHolder {
        TextView summary;
        TextView locationTime;
        TextView dateText;
        ImageView type;
    }
} 