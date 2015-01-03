package com.ucla_ieee.app.content;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.ucla_ieee.app.MainActivity;
import com.ucla_ieee.app.user.SessionManager;
import com.ucla_ieee.app.util.JsonServerUtil;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class RewardsTask extends AsyncTask<Void, Void, String> {
    private MainActivity mContext;
    private SessionManager mSessionManager;
    private JsonServerUtil mUtil;

    public RewardsTask(Context context) {
        mContext = (MainActivity) context;
        mSessionManager = new SessionManager(mContext);
        mUtil = new JsonServerUtil();
    }

    @Override
    protected String doInBackground(Void... params) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://ieeebruins.org/membership_serve/rewards.php");
        HttpResponse response;
        try {
            response = httpClient.execute(httpGet);
        } catch (IOException e) {
            return null;
        }
        return mUtil.getStringFromServerResponse(response.getEntity());
    }

    @Override
    protected void onPostExecute(String response) {
        JsonArray rewards = mUtil.getJsonArrayFromString(response);
        if (rewards == null) {
            Toast.makeText(mContext, "Couldn't load rewards", Toast.LENGTH_SHORT).show();
            mContext.getTaskManager().finishRewardsTask();
            return;
        }

        if (rewards.size() > 0) {
            mSessionManager.storeRewards(rewards.toString());
            if (mContext.getTaskManager().getRewardsActivity() != null) {
                mContext.getTaskManager().getRewardsActivity().updateRewards();
            }
        }

        mContext.getTaskManager().finishRewardsTask();
    }
}