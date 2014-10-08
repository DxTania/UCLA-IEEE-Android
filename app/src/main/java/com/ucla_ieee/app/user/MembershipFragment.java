package com.ucla_ieee.app.user;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ucla_ieee.app.MainActivity;
import com.ucla_ieee.app.R;
import com.ucla_ieee.app.calendar.Event;
import com.ucla_ieee.app.content.YearSpinner;
import com.ucla_ieee.app.newsfeed.News;
import com.ucla_ieee.app.newsfeed.NewsFeedListAdapter;
import com.ucla_ieee.app.signin.LoginActivity;
import org.apache.http.message.BasicNameValuePair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MembershipFragment extends Fragment {
    private MembershipEditTask mAuthTask = null;

    private EditText mEmail, mName, mMemberId, mMajor;
    private TextView mEmailText, mNameText, mMemberIdText, mPasswordText, mMajorText, mYearText;
    private TextView mPointsText, mNoEvents;
    private Spinner mYearSpinner;
    private ImageButton mPasswordEditPencil;
    private ListView mAttendedEventsView;
    private View mHeader;
    private AlertDialog mAlertDialog;

    private NewsFeedListAdapter mEventListAdapter;
    private List<News> mAttendedEvents;
    private SessionManager mSessionManager;

    private Boolean mAlertReady = false;
    private String mCachedPassword = "";
    private List<String> mYears;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        mAttendedEvents = new ArrayList<News>();
        mSessionManager = new SessionManager(getActivity());
        mAttendedEventsView = (ListView) rootView.findViewById(R.id.attendedEventList);
        mHeader = View.inflate(getActivity(), R.layout.snippet_profile, null);

        mYears = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.years)));
        mYearSpinner = (Spinner) mHeader.findViewById(R.id.memberYear);
        ArrayAdapter<String> adapter = new YearSpinner(getActivity());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mYearSpinner.setAdapter(adapter);

        setUpViews();
        setUpImageButtons();
        setUpSaveChanges();
        setUpAttendedEventsView();

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        mAlertDialog.dismiss();
    }

    /**
     * Updates the membership UI to reflect most updated values that may have been updating
     * by an Asynchronous call
     */
    public void update() {
        mPointsText.setText(String.valueOf(mSessionManager.getInt(SessionManager.Keys.POINTS) + "/"
                + mSessionManager.getInt(SessionManager.Keys.TOTAL_POINTS)));
        mEmailText.setText(mSessionManager.getString(SessionManager.Keys.EMAIL));
        mNameText.setText(mSessionManager.getString(SessionManager.Keys.NAME));
        mMemberIdText.setText(mSessionManager.getString(SessionManager.Keys.IEEE_ID));
        mPasswordText.setText("");
        mMajorText.setText(mSessionManager.getString(SessionManager.Keys.MAJOR));
        mYearText.setText(mSessionManager.getString(SessionManager.Keys.YEAR));
    }

    /**
     * Converts json array to array of events
     * @param jsonEvents to convert
     * @return list of Event
     */
    private List<Event> jsonEventsToEvents(JsonArray jsonEvents) {
        List<Event> events = new ArrayList<Event>();
        for (int i = 0; i < jsonEvents.size(); i++) {
            Event event = new Event();
            JsonObject jsonEvent = jsonEvents.get(i).getAsJsonObject();
            event.setSummary(jsonEvent.get("summary").getAsString());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            try {
                event.setStartDate(format.parse(jsonEvent.get("start").getAsString()));
                event.setEndDate(format.parse(jsonEvent.get("end").getAsString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            event.setId(jsonEvent.get("event_id").getAsString());
            event.setLocation(jsonEvent.get("location").getAsString());
            event.setAllDay(jsonEvent.get("all_day").getAsInt() == 1);
            events.add(event);
        }

        return events;
    }

    /**
     * Updates attended events with info from most updated session
     */
    public void updateAttendedEvents() {
        mAttendedEvents.clear();
        mEventListAdapter.clear();
        JsonArray jsonEvents = mSessionManager.getJsonArray(SessionManager.Keys.ATTENDED_EVENTS);
        if (jsonEvents != null) {
            for (Event event : jsonEventsToEvents(jsonEvents)) {
                mAttendedEvents.add(event.getAsNews());
            }
            mEventListAdapter.addAll(mAttendedEvents);
            mEventListAdapter.notifyDataSetChanged();
        }

        if (mAttendedEvents.size() > 0) {
            mNoEvents.setVisibility(View.GONE);
        } else {
            mNoEvents.setVisibility(View.VISIBLE);
        }
    }

    /**
     * In order to scroll both the profile and attended events, the profile
     * is used as a header view
     */
    private void setUpAttendedEventsView() {
        mAttendedEventsView.addHeaderView(mHeader);
        List<News> news = new ArrayList<News>();
        for (Event event : jsonEventsToEvents(mSessionManager.getJsonArray(SessionManager.Keys.ATTENDED_EVENTS))) {
            news.add(event.getAsNews());
        }
        if (news.size() > 0 ) {
            mNoEvents.setVisibility(View.GONE);
        }
        mEventListAdapter = new NewsFeedListAdapter(getActivity(), news);
        mAttendedEventsView.setAdapter(mEventListAdapter);
    }

    /**
     * Save changes if at least one field is different than the current session
     */
    private void setUpSaveChanges() {
        Button saveChanges = (Button) mHeader.findViewById(R.id.saveChanges);
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send post request
                mAuthTask = new MembershipEditTask(MembershipFragment.this, mPasswordText);

                toggleEdits(true);

                String newEmail, newName, newId, newPassword, newMajor, newYear;
                newEmail = mEmail.getText().toString();
                newName = mName.getText().toString();
                newId = mMemberId.getText().toString();
                newPassword = mPasswordText.getText().toString();
                newMajor = mMajorText.getText().toString();
                newYear = mYearSpinner.getSelectedItem().toString();

                List<BasicNameValuePair> valuePairs = new ArrayList<BasicNameValuePair>();
                if (!newEmail.equals(mSessionManager.getString(SessionManager.Keys.EMAIL))) {
                    valuePairs.add(new BasicNameValuePair("newEmail", newEmail));
                }
                if (!newName.equals(mSessionManager.getString(SessionManager.Keys.NAME))) {
                    valuePairs.add(new BasicNameValuePair("newName", newName));
                }
                if (!newId.equals(mSessionManager.getString(SessionManager.Keys.IEEE_ID))) {
                    valuePairs.add(new BasicNameValuePair("newId", newId));
                }
                if (!newMajor.equals(mSessionManager.getString(SessionManager.Keys.MAJOR))) {
                    valuePairs.add(new BasicNameValuePair("major", newMajor));
                }
                if (!newYear.equals(mSessionManager.getString(SessionManager.Keys.YEAR))) {
                    valuePairs.add(new BasicNameValuePair("year", newYear));
                }
                if (!TextUtils.isEmpty(newPassword)) {
                    valuePairs.add(new BasicNameValuePair("newPassword", newPassword));
                    valuePairs.add(new BasicNameValuePair("password", mCachedPassword));
                }

                if (valuePairs.size() == 0) {
                    Toast.makeText(getActivity(), "Nothing to change", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuthTask.execute(valuePairs);
            }
        });

        Button revertChanges = (Button) mHeader.findViewById(R.id.revertChanges);
        revertChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEdits(true);
                mEmailText.setText(mSessionManager.getString(SessionManager.Keys.EMAIL));
                mEmail.setText(mSessionManager.getString(SessionManager.Keys.EMAIL));
                mNameText.setText(mSessionManager.getString(SessionManager.Keys.NAME));
                mName.setText(mSessionManager.getString(SessionManager.Keys.NAME));
                mMemberIdText.setText(mSessionManager.getString(SessionManager.Keys.IEEE_ID));
                mMemberIdText.setText(mSessionManager.getString(SessionManager.Keys.IEEE_ID));
                mMajorText.setText(mSessionManager.getString(SessionManager.Keys.MAJOR));
                mMajor.setText(mSessionManager.getString(SessionManager.Keys.MAJOR));
                mYearText.setText(mSessionManager.getString(SessionManager.Keys.YEAR));
                mYearSpinner.setSelection(mYears.indexOf(mSessionManager.getString(SessionManager.Keys.YEAR)));
                mPasswordText.setText("");
            }
        });
    }

    // Click listeners for image buttons
    private void setUpImageButtons() {

        ImageButton aboutPoints = (ImageButton) mHeader.findViewById(R.id.aboutPoints);
        aboutPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog alert = new AlertDialog.Builder(getActivity())
                        .setTitle("About Points")
                        .setMessage("Earn points by checking into events, helping out at events, and more!")
                        .setPositiveButton("Ok", null).create();
                alert.show();
            }
        });

        ImageButton changePassword = (ImageButton) mHeader.findViewById(R.id.changePassword);
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpAlertDialog();
            }
        });
    }

    /**
     * For password changing
     */
    private void setUpAlertDialog() {
        View ll = LayoutInflater.from(getActivity()).inflate(R.layout.snippet_password_popup, null);
        final EditText newPasswordView = (EditText) ll.findViewById(R.id.newPassword);
        final EditText retypedPasswordView = (EditText) ll.findViewById(R.id.retypedPassword);
        final EditText passwordView = (EditText) ll.findViewById(R.id.curPassword);

        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Change Password")
                .setMessage("Please fill in the following fields")
                .setView(ll)
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).create();

        if (!mAlertReady) {
            mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button button = mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (button != null) {
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String newPassword = newPasswordView.getText().toString();
                                String retypedPassword = retypedPasswordView.getText().toString();

                                if (!newPassword.equals(retypedPassword)) {
                                    retypedPasswordView.setError("Passwords don't match!");
                                    retypedPasswordView.requestFocus();
                                } else if (!LoginActivity.isPasswordValid(newPassword)) {
                                    newPasswordView.setError(getString(R.string.error_invalid_password));
                                    newPasswordView.requestFocus();
                                } else {
                                    mCachedPassword = passwordView.getText().toString();
                                    mPasswordText.setText(newPassword);
                                    mAlertDialog.hide();
                                }
                            }
                        });
                    }
                }
            });
        }

        mAlertDialog.show();
    }

    private void setUpViews() {
        mEmail = (EditText) mHeader.findViewById(R.id.memberEmail);
        mEmailText = (TextView) mHeader.findViewById(R.id.memberEmailText);
        mEmail.setText(mSessionManager.getString(SessionManager.Keys.EMAIL));
        mEmailText.setText(mSessionManager.getString(SessionManager.Keys.EMAIL));

        mName = (EditText) mHeader.findViewById(R.id.memberName);
        mNameText = (TextView) mHeader.findViewById(R.id.memberNameText);
        mName.setText(mSessionManager.getString(SessionManager.Keys.NAME));
        mNameText.setText(mSessionManager.getString(SessionManager.Keys.NAME));

        mMemberId = (EditText) mHeader.findViewById(R.id.memberId);
        mMemberIdText = (TextView) mHeader.findViewById(R.id.memberIdText);
        mMemberId.setText(mSessionManager.getString(SessionManager.Keys.IEEE_ID));
        mMemberIdText.setText(mSessionManager.getString(SessionManager.Keys.IEEE_ID));

        mPointsText = (TextView) mHeader.findViewById(R.id.numPoints);
        mPointsText.setText(String.valueOf(mSessionManager.getInt(SessionManager.Keys.POINTS) + "/"
                + mSessionManager.getInt(SessionManager.Keys.TOTAL_POINTS)));

        mPasswordText = (TextView) mHeader.findViewById(R.id.passwordText);
        mPasswordEditPencil = (ImageButton) mHeader.findViewById(R.id.changePassword);

        mNoEvents = (TextView) mHeader.findViewById(R.id.noEventsView);

        mMajorText = (TextView) mHeader.findViewById(R.id.memberMajorText);
        mMajor = (EditText) mHeader.findViewById(R.id.memberMajor);
        mMajor.setText(mSessionManager.getString(SessionManager.Keys.MAJOR));
        mMajorText.setText(mSessionManager.getString(SessionManager.Keys.MAJOR));

        mYearText = (TextView) mHeader.findViewById(R.id.memberYearText);
        mYearText.setText(mSessionManager.getString(SessionManager.Keys.YEAR));
        mYearSpinner.setSelection(mYears.indexOf(mSessionManager.getString(SessionManager.Keys.YEAR)));
    }

    /**
     * @param override Will force edit text's off
     */
    private void toggleEdits(boolean override) {
        toggleVisibility(mEmail, mEmailText, override);
        toggleVisibility(mName, mNameText, override);
        toggleVisibility(mMemberId, mMemberIdText, override);
        toggleVisibility(mMajor, mMajorText, override);
        toggleSpinner(mYearSpinner, mYearText, override);
    }

    private void toggleVisibility(EditText editText, TextView textView, boolean forceText) {
        if (editText.getVisibility() == View.VISIBLE || forceText) {
            editText.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            mPasswordEditPencil.setVisibility(View.GONE);
            textView.setText(editText.getText());
        } else {
            editText.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
            mPasswordEditPencil.setVisibility(View.VISIBLE);
        }
    }

    private void toggleSpinner(Spinner spinner, TextView textView, boolean forceText) {
        if (spinner.getVisibility() == View.VISIBLE || forceText) {
            spinner.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            textView.setText(spinner.getSelectedItem().toString());
        } else {
            spinner.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
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
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.getTaskManager().startUserAsyncCall(false);
            mainActivity.getTaskManager().startAttendedEventsAsyncCall();
        } else if (id == R.id.action_toggle_edit) {
            toggleEdits(false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
