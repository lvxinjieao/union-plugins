package com.u8.sdk;

import android.content.Context;
import android.content.res.Configuration;

import com.papa91.pay.pa.business.PPayCenter;

public class PaPaProxyApplication implements IApplicationListener {

    @Override
    public void onProxyCreate() {
        PPayCenter.initConfig(U8SDK.getInstance().getApplication());
    }

    @Override
    public void onProxyAttachBaseContext(Context base) {

    }

    @Override
    public void onProxyConfigurationChanged(Configuration config) {

    }

    @Override
    public void onProxyTerminate() {

    }
}
