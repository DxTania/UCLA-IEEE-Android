package navigation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.ucla_ieee.app.R;

public class NavigationDrawerListAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] labels;
    private final ListView mDrawerListView;

    public NavigationDrawerListAdapter(Context context, String[] labels, ListView drawerListView) {
        super(context, R.layout.snippet_navigation, labels);
        this.context = context;
        this.labels = labels;
        this.mDrawerListView = drawerListView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.snippet_navigation, parent, false);
            viewHolder.summary = (TextView) convertView.findViewById(R.id.summaryText);
            viewHolder.type = (ImageView) convertView.findViewById(R.id.type);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String label = labels[position];
        viewHolder.summary.setText(label);

        NavigationDrawerFragment.Navigation pos = NavigationDrawerFragment.Navigation.values()[position];
        switch(pos) {
            case CALENDAR:
                viewHolder.type.setImageDrawable(context.getResources().getDrawable(R.drawable.calendar));
                break;
            case ANNOUNCEMENTS:
                viewHolder.type.setImageDrawable(context.getResources().getDrawable(R.drawable.bullhorn));
                break;
            case MEMBERSHIP:
                viewHolder.type.setImageDrawable(context.getResources().getDrawable(R.drawable.member));
                break;
            case CHECK_IN:
                viewHolder.type.setImageDrawable(context.getResources().getDrawable(R.drawable.checkin));
                break;
            case HELP:
                viewHolder.type.setImageDrawable(context.getResources().getDrawable(R.drawable.help));
                break;
            case POINTS_REWARDS:
                viewHolder.type.setImageDrawable(context.getResources().getDrawable(R.drawable.points));
                break;
            case FRONT_PAGE:
                viewHolder.type.setImageDrawable(context.getResources().getDrawable(R.drawable.star));
                break;
            case LOGOUT:
                viewHolder.type.setImageDrawable(context.getResources().getDrawable(R.drawable.logout));
                break;
        }

        if (mDrawerListView.isItemChecked(position)) {
            convertView.setBackgroundColor(context.getResources().getColor(R.color.ucla_blue));
            viewHolder.summary.setTextColor(context.getResources().getColor(R.color.caldroid_white));
        } else {
            convertView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
            viewHolder.summary.setTextColor(context.getResources().getColor(android.R.color.black));
        }

        return convertView;
    }

    static class ViewHolder {
        TextView summary;
        ImageView type;
    }
} 