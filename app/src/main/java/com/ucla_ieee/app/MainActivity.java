package com.ucla_ieee.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.ucla_ieee.app.calendar.CalendarActivity;
import com.ucla_ieee.app.scan.IntentIntegrator;
import com.ucla_ieee.app.scan.IntentResult;
import com.ucla_ieee.app.signin.LoginActivity;
import com.ucla_ieee.app.signin.ProfileActivity;
import com.ucla_ieee.app.signin.SessionManager;

public class MainActivity extends Activity {
    private SessionManager mSessionManager;
    private LinearLayout mAnnouncementsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        mSessionManager = new SessionManager(this);

        mAnnouncementsView = (LinearLayout) findViewById(R.id.announcements);
        mAnnouncementsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Allow clicking on announcements to see previous announcements
            }
        });

        // CALENDAR
        LinearLayout calendar = (LinearLayout) findViewById(R.id.calendarButton);
        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent calendarIntent = new Intent(MainActivity.this, CalendarActivity.class);
                startActivity(calendarIntent);
            }
        });

        // MEMBERSHIP
        LinearLayout myMembership = (LinearLayout) findViewById(R.id.myMembership);
        myMembership.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent membershipIntent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(membershipIntent);
            }
        });

        // LOGOUT
        LinearLayout logout = (LinearLayout) findViewById(R.id.achievementsButton);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSessionManager.logoutUser();
                actOnLoginStatus();
            }
        });

        // SETTINGS
        LinearLayout settings = (LinearLayout) findViewById(R.id.settingsButton);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });
    }

    public void actOnLoginStatus() {
        if (!mSessionManager.isLoggedIn()) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        actOnLoginStatus();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
//            testText.setText(scanResult.getContents());
            Toast.makeText(this, "Thanks for checking in!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
