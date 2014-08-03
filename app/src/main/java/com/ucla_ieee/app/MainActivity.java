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
import com.ucla_ieee.app.calendar.CalendarActivity;
import com.ucla_ieee.app.calendar.CalendarTask;
import com.ucla_ieee.app.signin.LoginActivity;
import com.ucla_ieee.app.signin.ProfileActivity;
import com.ucla_ieee.app.signin.SessionManager;

public class MainActivity extends FragmentActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    public static final String CAL_TAG = "calendar";
    public static final String PROFILE_TAG = "profile";
    public static final String MAIN_TAG = "main";

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
    private CalendarActivity mCalendarActivity;

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
        mNavigationDrawerFragment.switchFragments(0);
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

    public void doFragment(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment == null) {
            if (tag.equals(CAL_TAG)) {
                fragment = new CalendarActivity();
            } else if (tag.equals(PROFILE_TAG)) {
                fragment = new ProfileActivity();
            } else {
                fragment = new MainPage();
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
                    getMenuInflater().inflate(R.menu.main_activity2, menu);
                    break;
                case 1:
                    getMenuInflater().inflate(R.menu.calendar, menu);
                    break;
                default:
                    getMenuInflater().inflate(R.menu.main_activity2, menu);
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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        mDrawerLayout.openDrawer(mNavigationDrawerFragment.getDrawerFragmentView());
    }

    public void setFragmentTitle(String title) {
        mTitle = title;
    }

    // Calendar Functions
    public void startAsyncCall(CalendarActivity activity) {
        if (mCalendarActivity == null) {
            mCalendarActivity = activity;
        }
        if (mCalendarTask == null) {
            mCalendarTask = new CalendarTask(this);
            mCalendarTask.execute((Void) null);
        }
    }

    public void setCalendar(CalendarActivity activity) {
        mCalendarActivity = activity;
    }

    public CalendarActivity getCalendar() {
        return mCalendarActivity;
    }
    public void stopCalendarTask() {
        if (mCalendarTask != null) {
            mCalendarTask.cancel(true);
        }
    }

    public void setCalendarTaskNull() {
        mCalendarTask = null;
    }
}
