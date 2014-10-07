package com.ucla_ieee.app.content;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ucla_ieee.app.MainActivity;
import com.ucla_ieee.app.R;
import com.ucla_ieee.app.user.SessionManager;


public class PointsRewardsFragment extends Fragment {
    private MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_points_rewards, container, false);

        SessionManager sessionManager = new SessionManager(this.getActivity());

        mainActivity = (MainActivity) getActivity();

        return rootView;
    }
}
