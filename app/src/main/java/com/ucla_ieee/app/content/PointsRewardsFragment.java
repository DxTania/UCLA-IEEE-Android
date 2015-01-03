package com.ucla_ieee.app.content;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ucla_ieee.app.R;
import com.ucla_ieee.app.user.SessionManager;


public class PointsRewardsFragment extends Fragment {
    private SessionManager mSessionManager;
    private TextView mRewardsView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_points_rewards, container, false);
        mSessionManager = new SessionManager(this.getActivity());
        mRewardsView = (TextView) rootView.findViewById(R.id.rewards_list);
        updateRewards();
        return rootView;
    }

    public void updateRewards() {
        JsonArray rewards = mSessionManager.getJsonArray(SessionManager.Keys.REWARDS);
        StringBuilder bulletList = new StringBuilder();
        if (rewards != null) {
            for (int i = 0; i < rewards.size(); i++) {
                JsonObject reward = rewards.get(i).getAsJsonObject();
                bulletList.append("• ").append(reward.get("content").getAsString());
                bulletList.append(" (").append(reward.get("price").getAsString()).append(" points)");
                bulletList.append("\n");
            }
        }
        mRewardsView.setText(bulletList.toString());
    }
}
