package com.ucla_ieee.app.util;

import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

public abstract class FadeFragmentActivity extends FragmentActivity {
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                super.onMenuItemSelected(featureId, item);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            default:
                break;
        }

        return true;
    }

}
