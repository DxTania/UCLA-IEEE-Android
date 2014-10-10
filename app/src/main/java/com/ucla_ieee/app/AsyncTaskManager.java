package com.ucla_ieee.app;

import android.os.Handler;
import android.os.Message;
import com.ucla_ieee.app.calendar.CalendarFragment;
import com.ucla_ieee.app.calendar.CalendarTask;
import com.ucla_ieee.app.content.AnnouncementsFragment;
import com.ucla_ieee.app.content.AnnouncementsTask;
import com.ucla_ieee.app.scan.CheckInScanTask;
import com.ucla_ieee.app.user.AttendedEventsTask;
import com.ucla_ieee.app.user.UpdateTask;

/**
 * This class manages different types of asynchronous calls and sets up
 * a handler in order to update the UI when needed
 */
public class AsyncTaskManager {
    private CalendarTask mCalendarTask;
    private AnnouncementsTask mAnnouncementsTask;
    private CheckInScanTask mCheckInScanTask;
    private UpdateTask mUpdateTask;
    private AttendedEventsTask mAttendedEventsTask;
    private CalendarFragment mCalendarFragment;
    private AnnouncementsFragment mAnnouncementsFragment;
    private Handler mHandler;
    private MainActivity mActivity;

    // TODO: retry async call if it fails!!
    // stop at some number of calls depending on how long they took to complete...
    public AsyncTaskManager(MainActivity mainActivity) {
        this.mActivity = mainActivity;
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (mUpdateTask != null || mCalendarTask != null
                        || mAnnouncementsTask != null || mAttendedEventsTask != null) {
                    mHandler.sendEmptyMessageDelayed(0, 1000);
                } else {
                    mActivity.updateUI();
                }
            }
        };
        mHandler.sendEmptyMessageDelayed(0, 1000);
    }

    public void stopAsyncTasks() {
        if (mCalendarTask != null) {
            mCalendarTask.cancel(true);
        }
        if (mUpdateTask != null) {
            mUpdateTask.cancel(true);
        }

        if (mAnnouncementsTask != null) {
            mAnnouncementsTask.cancel(true);
        }

        if (mAttendedEventsTask != null) {
            mAttendedEventsTask.cancel(true);
        }

        if (mCheckInScanTask != null) {
            mCheckInScanTask.cancel(true);
        }
    }

    public void finishAnnouncementsTask() {
        mAnnouncementsTask = null;
        mHandler.sendEmptyMessageDelayed(0, 0);
    }

    public void finishCalendarTask() {
        mCalendarTask = null;
        mHandler.sendEmptyMessageDelayed(0, 0);
    }

    public void finishUpdateUserTask() {
        mUpdateTask = null;
        mHandler.sendEmptyMessageDelayed(0, 0);
    }

    public void finishGetAttendedEventsTask() {
        mAttendedEventsTask = null;
        mHandler.sendEmptyMessageDelayed(0, 0);
    }

    public void finishCheckInTask() {
        mCheckInScanTask = null;
        mHandler.sendEmptyMessageDelayed(0, 0);
    }

    public void startAttendedEventsAsyncCall() {
        if (mAttendedEventsTask == null) {
            mAttendedEventsTask = new AttendedEventsTask(mActivity);
            mAttendedEventsTask.execute((Void) null);
        }
    }

    // Calendar Functions
    public void startCalendarAsyncCall(CalendarFragment activity) {
        if (mCalendarFragment == null) {
            mCalendarFragment = activity;
        }
        if (mCalendarTask == null) {
            if (activity == null) {
                mActivity.loading = true;
            }
            mCalendarTask = new CalendarTask(mActivity);
            mCalendarTask.execute((Void) null);
        }
    }

    public void startUserAsyncCall(boolean frontPage) {
        if (mUpdateTask == null) {
            // only loading is true if started from main page fragment
            mActivity.loading = frontPage;
            mUpdateTask = new UpdateTask(mActivity);
            mUpdateTask.execute((Void) null);
        }
    }

    public void startAnnouncementsAsyncCall(AnnouncementsFragment activity) {
        if (mAnnouncementsFragment == null) {
            mAnnouncementsFragment = activity;
        }
        if (mAnnouncementsTask == null) {
            if (activity == null) {
                mActivity.loading = true;
            }
            mAnnouncementsTask = new AnnouncementsTask(mActivity);
            mAnnouncementsTask.execute((Void) null);
        }
    }

    public void startCheckInAsyncCall(String qrCode) {
        if (mCheckInScanTask == null) {
            mCheckInScanTask = new CheckInScanTask(mActivity, qrCode);
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
}
