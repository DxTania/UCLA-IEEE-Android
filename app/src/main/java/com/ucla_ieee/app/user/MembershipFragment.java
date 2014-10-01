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
import com.ucla_ieee.app.newsfeed.News;
import com.ucla_ieee.app.newsfeed.NewsFeedListAdapter;
import org.apache.http.message.BasicNameValuePair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MembershipFragment extends Fragment {
    private MembershipEditTask mAuthTask = null;

    private EditText email, name, memberId;
    private TextView emailText, nameText, memberIdText, passwordText, numPoints, mNoEvents;
    private ImageButton passwordEditPencil;
    private Boolean alertReady = false;
    private String cachePassword = "";
    private SessionManager sessionManager;
    private View rootView;
    private NewsFeedListAdapter mEventListAdapter;
    private List<News> mAttendedEvents;
    private ListView attendedEvents;
    private View header;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        mAttendedEvents = new ArrayList<News>();
        sessionManager = new SessionManager(getActivity());

        header = View.inflate(getActivity(), R.layout.snippet_profile, null);
        setUpViews();
        setUpImageButtons();
        setUpSaveChanges();

        // TODO: QR Codes should be the id of the calendar event. SHould we verify checking in with GPS?
        attendedEvents = (ListView) rootView.findViewById(R.id.attendedEventList);
        attendedEvents.addHeaderView(header);
        List<News> news = new ArrayList<News>();
        for (Event event : jsonEventsToEvents(sessionManager.getAttendedEvents())) {
            news.add(event.getAsNews());
        }
        if (news.size() > 0 ) {
            mNoEvents.setVisibility(View.GONE);
        }
        mEventListAdapter = new NewsFeedListAdapter(getActivity(), news);
        attendedEvents.setAdapter(mEventListAdapter);

        return rootView;
    }

    @Override
    public void onPause() {
        // Just in case...
        attendedEvents = (ListView) rootView.findViewById(R.id.attendedEventList);
        attendedEvents.removeHeaderView(header);
        super.onPause();
    }

    public void update() {
        toggleEdits(true);
        emailText.setText(sessionManager.getEmail());
        nameText.setText(sessionManager.getName());
        memberIdText.setText(sessionManager.getIEEEId());
        passwordText.setText("");
        numPoints.setText(String.valueOf(sessionManager.getPoints()));
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
            event.setAllDay(false);
            events.add(event);
        }

        return events;
    }

    /**
     * Updates attended events with info from session
     */
    public void updateAttendedEvents() {
        mAttendedEvents.clear();
        mEventListAdapter.clear();
        // There was a null pointer exception here
        JsonArray jsonEvents = sessionManager.getAttendedEvents();
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

    // Click listener for save changes button
    private void setUpSaveChanges() {
        Button saveChanges = (Button) header.findViewById(R.id.saveChanges);
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send post request
                mAuthTask = new MembershipEditTask(MembershipFragment.this, passwordText);

                String newEmail, newName, newId, newPassword;
                newEmail = email.getText().toString();
                newName = name.getText().toString();
                newId = memberId.getText().toString();
                newPassword = passwordText.getText().toString();

                List<BasicNameValuePair> valuePairs = new ArrayList<BasicNameValuePair>();
                if (!newEmail.equals(sessionManager.getEmail())) {
                    valuePairs.add(new BasicNameValuePair("newEmail", newEmail));
                }
                if (!newName.equals(sessionManager.getName())) {
                    valuePairs.add(new BasicNameValuePair("newName", newName));
                }
                if (!newId.equals(sessionManager.getIEEEId())) {
                    valuePairs.add(new BasicNameValuePair("newId", newId));
                }
                if (!TextUtils.isEmpty(newPassword)) {
                    valuePairs.add(new BasicNameValuePair("newPassword", newPassword));
                    valuePairs.add(new BasicNameValuePair("password", cachePassword));
                }

                if (valuePairs.size() == 0) {
                    Toast.makeText(getActivity(), "Nothing to change", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuthTask.execute(valuePairs);
                toggleEdits(true);
            }
        });

        Button revertChanges = (Button) header.findViewById(R.id.revertChanges);
        revertChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEdits(true);
                emailText.setText(sessionManager.getEmail());
                email.setText(sessionManager.getEmail());
                nameText.setText(sessionManager.getName());
                name.setText(sessionManager.getName());
                memberIdText.setText(sessionManager.getIEEEId());
                memberIdText.setText(sessionManager.getIEEEId());
                passwordText.setText("");
            }
        });
    }

    // Click listeners for image buttons
    private void setUpImageButtons() {
        ImageButton aboutNextReward = (ImageButton) header.findViewById(R.id.aboutNextReward);
        aboutNextReward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout ll = (LinearLayout) header.findViewById(R.id.nextRewardHint);
                ll.setVisibility(ll.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });

        ImageButton aboutPoints = (ImageButton) header.findViewById(R.id.aboutPoints);
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

        ImageButton changePassword = (ImageButton) header.findViewById(R.id.changePassword);
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpAlertDialog();
            }
        });
    }

    // Password change alert dialog TODO: Can't change password twice in a row and can't undo
    private void setUpAlertDialog() {
        View ll = LayoutInflater.from(getActivity()).inflate(R.layout.snippet_password_popup, null);
        final EditText newPasswordView = (EditText) ll.findViewById(R.id.newPassword);
        final EditText retypedPasswordView = (EditText) ll.findViewById(R.id.retypedPassword);
        final EditText passwordView = (EditText) ll.findViewById(R.id.curPassword);

        final AlertDialog alert = new AlertDialog.Builder(getActivity())
                .setTitle("Change Password")
                .setMessage("Please fill in the following fields")
                .setView(ll)
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).create();

        if (!alertReady) {
            alert.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button button = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (button != null) {
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String newPassword = newPasswordView.getText().toString();
                                String retypedPassword = retypedPasswordView.getText().toString();

                                if (!newPassword.equals(retypedPassword)) {
                                    retypedPasswordView.setError("Passwords don't match!");
                                    retypedPasswordView.requestFocus();
                                } else {
                                    cachePassword = passwordView.getText().toString();
                                    passwordText.setText(newPassword);
                                    alert.dismiss();
                                }
                            }
                        });
                    }
                }
            });
            alertReady = true;
        }

        alert.show();
    }

    private void setUpViews() {
        email = (EditText) header.findViewById(R.id.memberEmail);
        emailText = (TextView) header.findViewById(R.id.memberEmailText);
        email.setText(sessionManager.getEmail());
        emailText.setText(sessionManager.getEmail());

        name = (EditText) header.findViewById(R.id.memberName);
        nameText = (TextView) header.findViewById(R.id.memberNameText);
        name.setText(sessionManager.getName());
        nameText.setText(sessionManager.getName());

        memberId = (EditText) header.findViewById(R.id.memberId);
        memberIdText = (TextView) header.findViewById(R.id.memberIdText);
        memberId.setText(sessionManager.getIEEEId());
        memberIdText.setText(sessionManager.getIEEEId());

        numPoints = (TextView) header.findViewById(R.id.numPoints);
        numPoints.setText(String.valueOf(sessionManager.getPoints()));

        passwordText = (TextView) header.findViewById(R.id.passwordText);
        passwordEditPencil = (ImageButton) header.findViewById(R.id.changePassword);

        mNoEvents = (TextView) header.findViewById(R.id.noEventsView);
    }

    /**
     * @param override Will force edit text's off
     */
    private void toggleEdits(boolean override) {
        toggleVisibility(email, emailText, override);
        toggleVisibility(name, nameText, override);
        toggleVisibility(memberId, memberIdText, override);
    }

    private void toggleVisibility(EditText editText, TextView textView, boolean forceText) {
        if (editText.getVisibility() == View.VISIBLE || forceText) {
            editText.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            passwordEditPencil.setVisibility(View.GONE);
            textView.setText(editText.getText());
        } else {
            editText.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
            passwordEditPencil.setVisibility(View.VISIBLE);
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
            mainActivity.startUserAsyncCall(false);
            mainActivity.startAttendedEventsAsyncCall();
        } else if (id == R.id.action_toggle_edit) {
            toggleEdits(false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
