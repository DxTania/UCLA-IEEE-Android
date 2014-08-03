package com.ucla_ieee.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ucla_ieee.app.signin.SessionManager;

public class MainPageFragment extends Fragment {

    private SessionManager sessionManager;
    private View rootView;
    private TextView mPointsView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.startAsyncCall(null);

        mPointsView = (TextView) rootView.findViewById(R.id.numPoints);
        sessionManager = new SessionManager(getActivity());
        int points = sessionManager.getPoints();
        mPointsView.setText(String.valueOf(points) + ".");

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
