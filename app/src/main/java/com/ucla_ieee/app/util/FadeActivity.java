package com.ucla_ieee.app.util;

import android.app.Activity;
import android.view.MenuItem;

public abstract class FadeActivity extends Activity {
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
