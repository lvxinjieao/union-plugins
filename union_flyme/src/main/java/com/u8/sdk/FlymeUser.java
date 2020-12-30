package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class FlymeUser extends UserAdapter {

    private String[] supportedMethods = {"login", "switchLogin", "exit", "submitExtraData", "queryAntiAddiction", "realNameRegister"};

    public FlymeUser(Activity context) {
        FlymeSDK.getInstance().onActivityCreate();
    }

    @Override
    public void login() {
        FlymeSDK.getInstance().login();
    }

    @Override
    public void switchLogin() {
        FlymeSDK.getInstance().login();
    }

    @Override
    public void exit() {
        FlymeSDK.getInstance().exitSDK();
    }

    public void submitExtraData(UserExtraData data) {
        FlymeSDK.getInstance().submitExtraData(data);
    }


    @Override
    public void realNameRegister() {
        U8SDK.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                FlymeSDK.getInstance().showRealName();
            }
        });

    }

    @Override
    public void queryAntiAddiction() {
        U8SDK.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                FlymeSDK.getInstance().queryRealName();
            }
        });

    }

    @Override
    public boolean isSupportMethod(String methodName) {

        return Arrays.contain(supportedMethods, methodName);
    }

}
