package com.ucla_ieee.app.content;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.ucla_ieee.app.R;

public class YearSpinner extends ArrayAdapter<String> {

    public YearSpinner(Context context) {
        super(context, android.R.layout.simple_spinner_item, context.getResources().getStringArray(R.array.years));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = super.getView(position, convertView, parent);
        if (position == getCount()) {
            ((TextView) v.findViewById(android.R.id.text1)).setText("");
            ((TextView) v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
        }

        return v;
    }

    @Override
    public int getCount() {
        return super.getCount() - 1; // you dont display last item. It is used as hint.
    }

}
