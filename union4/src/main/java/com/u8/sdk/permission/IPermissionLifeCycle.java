package com.u8.sdk.permission;

import android.app.Activity;
import android.content.Intent;

public interface IPermissionLifeCycle {

    public void onCreate(Activity context);

    public void onNewIntent(Activity context, Intent newIntent);

    public void onStart(Activity context);

    public void onPause(Activity context);

    public void onResume(Activity context);

    public void onStop(Activity context);

    public void onDestroy(Activity context);

    public void onRestart(Activity context);

    public void onActivityResult(Activity context, int requestCode, int resultCode, Intent data);
}
