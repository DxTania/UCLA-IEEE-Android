package com.ucla_ieee.app;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.ucla_ieee.app.calendar.CalendarFragment;
import com.ucla_ieee.app.calendar.CalendarTask;
import com.ucla_ieee.app.content.AnnouncementsFragment;
import com.ucla_ieee.app.content.AnnouncementsTask;
import com.ucla_ieee.app.newsfeed.FrontPageFragment;
import com.ucla_ieee.app.scan.CheckInScanTask;
import com.ucla_ieee.app.signin.LoginActivity;
import com.ucla_ieee.app.user.MembershipFragment;
import com.ucla_ieee.app.user.SessionManager;
import com.ucla_ieee.app.user.UpdateTask;

public class MainActivity extends FragmentActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    public static final String CAL_TAG = "calendar";
    public static final String PROFILE_TAG = "profile";
    public static final String MAIN_TAG = "main";
    public static final String ANNOUNCEMENTS_TAG = "announcements";

    public String currentTag;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private SessionManager mSessionManager;
    private DrawerLayout mDrawerLayout;
    private int mPosition;
    private CalendarTask mCalendarTask;
    private AnnouncementsTask mAnnouncementsAsyncTask;
    private CheckInScanTask mCheckInScanTask;
    private UpdateTask mUpdateTask;
    private CalendarFragment mCalendarFragment;
    private AnnouncementsFragment mAnnouncementsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = "UCLA IEEE";
        mSessionManager = new SessionManager(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                mDrawerLayout,
                this);

        mNavigationDrawerFragment.selectItem(0);
        mNavigationDrawerFragment.switchFragments(0, false);

        startUserAsyncCall();
        startCalendarAsyncCall(null);
        startAnnouncementsAsyncCall(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        actOnLoginStatus();
    }

    public void actOnLoginStatus() {
        if (!mSessionManager.isLoggedIn()) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        mPosition = position;
        invalidateOptionsMenu();
    }

    /**
     * Switches out current fragment
     * @param tag of fragment to switch to
     * @param renew will refresh fragment even if tag is same as current fragment
     */
    public void doFragment(String tag, boolean renew) {
        currentTag = tag;
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment == null || renew) {
            if (tag.equals(CAL_TAG)) {
                fragment = new CalendarFragment();
            } else if (tag.equals(PROFILE_TAG)) {
                fragment = new MembershipFragment();
            } else if (tag.equals(ANNOUNCEMENTS_TAG)) {
                fragment = new AnnouncementsFragment();
            } else {
                fragment = new FrontPageFragment();
            }
        }
        if (!fragment.isVisible()) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment, tag)
                    .addToBackStack(null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            switch (mPosition) {
                case 0:
                    getMenuInflater().inflate(R.menu.main_settings, menu);
                    return false;
                case 1:
                    getMenuInflater().inflate(R.menu.calendar, menu);
                    return false;
                case 2:
                    getMenuInflater().inflate(R.menu.edit_member, menu);
                    return false;
                default:
                    getMenuInflater().inflate(R.menu.main_settings, menu);
            }
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

    public void setFragmentTitle(String title) {
        mTitle = title;
        restoreActionBar();
    }

    // Calendar Functions
    public void startCalendarAsyncCall(CalendarFragment activity) {
        if (mCalendarFragment == null) {
            mCalendarFragment = activity;
        }
        if (mCalendarTask == null) {
            mCalendarTask = new CalendarTask(this);
            mCalendarTask.execute((Void) null);
        }
    }

    public void startUserAsyncCall() {
        if (mUpdateTask == null) {
            mUpdateTask = new UpdateTask(this);
            mUpdateTask.execute((Void) null);
        }
    }

    public void startAnnouncementsAsyncCall(AnnouncementsFragment activity) {
        if (mAnnouncementsFragment == null) {
            mAnnouncementsFragment = activity;
        }
        if (mAnnouncementsAsyncTask == null) {
            mAnnouncementsAsyncTask = new AnnouncementsTask(this);
            mAnnouncementsAsyncTask.execute((Void) null);
        }
    }

    public void startCheckInAsyncCall(String qrCode) {
        if (mCheckInScanTask == null) {
            mCheckInScanTask = new CheckInScanTask(this, qrCode);
            mCheckInScanTask.execute((Void) null);
        }
    }

    public CalendarFragment getCalendar() {
        return mCalendarFragment;
    }

    public void setCalendar(CalendarFragment activity) {
        mCalendarFragment = activity;
    }

    public AnnouncementsFragment getAnnouncementsActivity() {
        return mAnnouncementsFragment;
    }

    public void stopAsyncTasks() {
        if (mCalendarTask != null) {
            mCalendarTask.cancel(true);
        }
        if (mUpdateTask != null) {
            mUpdateTask.cancel(true);
        }

        if (mAnnouncementsAsyncTask != null) {
            mAnnouncementsAsyncTask.cancel(true);
        }

        if (mCheckInScanTask != null) {
            mCheckInScanTask.cancel(true);
        }
    }

    public void finishAnnouncementsTask() {
        mAnnouncementsAsyncTask = null;
        if (currentTag.equals(MAIN_TAG)) {
            doFragment(currentTag, true);
        }
    }

    public void finishCalendarTask() {
        mCalendarTask = null;
        if (currentTag.equals(MAIN_TAG)) {
            doFragment(currentTag, true);
        }
    }

    public void finishUpdateUserTask() {
        mUpdateTask = null;
        if (currentTag.equals(MAIN_TAG) || currentTag.equals(PROFILE_TAG)) {
            doFragment(currentTag, true);
        }
    }

    public void finishCheckInTask() {
        mCheckInScanTask = null;
        if (currentTag.equals(MAIN_TAG) || currentTag.equals(PROFILE_TAG)) {
            doFragment(currentTag, true);
        }
    }
}
