package com.ucla_ieee.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.ucla_ieee.app.scan.IntentIntegrator;
import com.ucla_ieee.app.scan.IntentResult;
import com.ucla_ieee.app.signin.LoginActivity;
import com.ucla_ieee.app.signin.SessionManager;

public class MainActivity extends Activity {

    private TextView testText;
    public SessionManager mSessionManager;
    private static final int SCAN = 0;
    private static final int LOGIN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSessionManager = new SessionManager(this);
        testText = (TextView) findViewById(R.id.testText);

        // CALENDAR
        Button calendar = (Button) findViewById(R.id.calendar);
        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent calendarIntent = new Intent(MainActivity.this, CalendarActivity.class);
                startActivity(calendarIntent);
            }
        });

        // CHECK-IN
        Button checkIn = (Button) findViewById(R.id.eventCheckIn);
        checkIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator barcodeScan = new IntentIntegrator(MainActivity.this);
                barcodeScan.initiateScan();
            }
        });

        // LOGOUT
        Button logout = (Button) findViewById(R.id.button4);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSessionManager.logoutUser();
                actOnLoginStatus();
            }
        });
    }

    public void actOnLoginStatus() {
        if (!mSessionManager.isLoggedIn()) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(loginIntent, LOGIN);
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
            testText.setText(scanResult.getContents());
        } else if (requestCode == LOGIN) {
            actOnLoginStatus();
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
