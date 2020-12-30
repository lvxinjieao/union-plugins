package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class LenovoUser extends UserAdapter {

    private String[] supportedMethods = {"login", "switchLogin", "exit", "queryAntiAddiction", "realNameRegister"};

    public LenovoUser(Activity context) {
        LenovoSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
    }


    @Override
    public void login() {
        LenovoSDK.getInstance().login();
    }

    @Override
    public void switchLogin() {
        LenovoSDK.getInstance().login();
    }

    @Override
    public void exit() {
        LenovoSDK.getInstance().exitSDK();
    }

    @Override
    public void queryAntiAddiction() {
        LenovoSDK.getInstance().queryRealName();
    }

    @Override
    public void realNameRegister() {
        LenovoSDK.getInstance().showRealName();
    }

    @Override
    public boolean isSupportMethod(String methodName) {
        return Arrays.contain(supportedMethods, methodName);
    }

}
