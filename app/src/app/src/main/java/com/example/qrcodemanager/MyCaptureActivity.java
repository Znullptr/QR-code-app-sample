package com.example.qrcodemanager;

import android.content.pm.ActivityInfo;

import com.journeyapps.barcodescanner.CaptureActivity;

public class MyCaptureActivity extends CaptureActivity {
    @Override
    public int getRequestedOrientation() {
        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

}

