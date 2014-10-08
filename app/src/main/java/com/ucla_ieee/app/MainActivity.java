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
import com.ucla_ieee.app.content.AnnouncementsFragment;
import com.ucla_ieee.app.content.HelpAboutFragment;
import com.ucla_ieee.app.content.PointsRewardsFragment;
import com.ucla_ieee.app.newsfeed.FrontPageFragment;
import com.ucla_ieee.app.scan.IntentIntegrator;
import com.ucla_ieee.app.scan.IntentResult;
import com.ucla_ieee.app.signin.LoginActivity;
import com.ucla_ieee.app.user.MembershipFragment;
import com.ucla_ieee.app.user.SessionManager;

public class MainActivity extends FragmentActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    public static final String CAL_TAG = "calendar";
    public static final String PROFILE_TAG = "profile";
    public static final String MAIN_TAG = "main";
    public static final String ANNOUNCEMENTS_TAG = "announcements";
    public static final String POINTS_REWARDS_TAG = "points_rewards";
    public static final String HELP_TAG = "help";

    public String currentTag;
    public boolean loading;

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
    private AsyncTaskManager mTaskManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = "UCLA IEEE";
        mSessionManager = new SessionManager(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mTaskManager = new AsyncTaskManager(this);

        // Get new data
        mTaskManager.startUserAsyncCall(true);
        mTaskManager.startCalendarAsyncCall(null);
        mTaskManager.startAnnouncementsAsyncCall(null);
        mTaskManager.startAttendedEventsAsyncCall();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                mDrawerLayout,
                this);

        mNavigationDrawerFragment.selectItem(0);
        mNavigationDrawerFragment.switchFragments(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null) {
            String contents = result.getContents();
            if (contents != null) {
                getTaskManager().startCheckInAsyncCall(contents);
            }
        }
    }

    public void updateUI() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        loading = false;
        if (currentTag.equals(MAIN_TAG)) {
            FrontPageFragment frontPage = (FrontPageFragment) fragmentManager.findFragmentByTag(MAIN_TAG);
            if (frontPage != null) {
                frontPage.updateNews();
                frontPage.updatePoints();
                frontPage.showProgress(false);
            }
        } else if (currentTag.equals(PROFILE_TAG)) {
            MembershipFragment membershipFragment = (MembershipFragment) fragmentManager.findFragmentByTag(PROFILE_TAG);
            if (membershipFragment != null) {
                membershipFragment.update();
                membershipFragment.updateAttendedEvents();
            }
        } else if (currentTag.equals(ANNOUNCEMENTS_TAG)) {
            AnnouncementsFragment announcementsFragment = (AnnouncementsFragment) fragmentManager.findFragmentByTag
                    (ANNOUNCEMENTS_TAG);
            if (announcementsFragment != null) {
                announcementsFragment.showProgress(false);
                announcementsFragment.updateAnnouncements();
            }
        }
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
     */
    public void doFragment(String tag) {
        currentTag = tag;
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment == null) { // Fragments are only created once
            if (tag.equals(CAL_TAG)) {
                fragment = new CalendarFragment();
            } else if (tag.equals(PROFILE_TAG)) {
                fragment = new MembershipFragment();
            } else if (tag.equals(ANNOUNCEMENTS_TAG)) {
                fragment = new AnnouncementsFragment();
            } else if (tag.equals(POINTS_REWARDS_TAG)) {
                fragment = new PointsRewardsFragment();
            } else if (tag.equals(HELP_TAG)) {
                fragment = new HelpAboutFragment();
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
            NavigationDrawerFragment.Navigation selected = NavigationDrawerFragment.Navigation.values()[mPosition];
            switch (selected) {
                case FRONT_PAGE:
                    getMenuInflater().inflate(R.menu.main_settings, menu);
                    return false;
                case ANNOUNCEMENTS:
                    getMenuInflater().inflate(R.menu.refresh_settings, menu);
                    return false;
                case MEMBERSHIP:
                    getMenuInflater().inflate(R.menu.edit_member, menu);
                    return false;
                case CALENDAR:
                    getMenuInflater().inflate(R.menu.refresh_settings, menu);
                    return false;
                case POINTS_REWARDS:
                    getMenuInflater().inflate(R.menu.main_settings, menu);
                    return false;
                case HELP:
                    getMenuInflater().inflate(R.menu.main_settings, menu);
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

    public AsyncTaskManager getTaskManager() {
        return mTaskManager;
    }
}
