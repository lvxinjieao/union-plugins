package com.u8.sdk;

import android.content.Intent;
import android.graphics.Color;

import com.quicksdk.QuickSdkSplashActivity;

public class QuickSplashActivity extends QuickSdkSplashActivity {

    @Override
    public int getBackgroundColor() {
        return Color.WHITE;
    }

    @Override
    public void onSplashStop() {
        try{
            Intent intent = new Intent(this, Class.forName("${Game_Launch_Activity}"));
            startActivity(intent);
            this.finish();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
