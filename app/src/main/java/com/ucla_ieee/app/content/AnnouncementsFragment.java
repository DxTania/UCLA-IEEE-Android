package com.ucla_ieee.app.content;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ucla_ieee.app.MainActivity;
import com.ucla_ieee.app.R;
import com.ucla_ieee.app.user.SessionManager;

import java.util.ArrayList;
import java.util.List;


public class AnnouncementsFragment extends Fragment {
    private AnnouncementsListAdapter mListAdapter;
    private SessionManager mSessionManager;
    private MainActivity mainActivity;
    private View mProgressView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_announcements, container, false);

        ListView announcements = (ListView) rootView.findViewById(R.id.announcementsList);
        mSessionManager = new SessionManager(this.getActivity());

        mProgressView = rootView.findViewById(R.id.refresh_process);

        mainActivity = (MainActivity) getActivity();
        mListAdapter = new AnnouncementsListAdapter(this.getActivity(), new ArrayList<Announcement>());
        updateAnnouncements();
        announcements.setAdapter(mListAdapter);

        return rootView;
    }

    public void updateAnnouncements() {
        JsonArray announcements = mSessionManager.getJsonArray(SessionManager.Keys.ANNOUNCEMENTS);
        if (announcements != null) {
            List<Announcement> announcementList = new ArrayList<Announcement>();
            for (int i = 0; i < announcements.size(); i++) {
                JsonObject announcement = announcements.get(i).getAsJsonObject();
                announcementList.add(new Announcement(announcement.get("unread").getAsBoolean(),
                        announcement.get("content").getAsString(),
                        announcement.get("datePosted").getAsString(), announcement.get("id").getAsInt()));
            }
            mListAdapter.clear();
            mListAdapter.addAll(announcementList);
            mListAdapter.notifyDataSetChanged();

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            showProgress(true);
            mainActivity.getTaskManager().startAnnouncementsAsyncCall(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
