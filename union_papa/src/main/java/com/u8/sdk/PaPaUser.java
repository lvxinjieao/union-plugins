package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class PaPaUser extends UserAdapter {

    private String[] supportedMethods = {"login", "switchLogin", "exit", "showAccountCenter", "submitExtraData", "queryAntiAddiction"};

    public PaPaUser(Activity activity) {
        PaPaSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
    }

    @Override
    public void login() {
        PaPaSDK.getInstance().login();
    }


    @Override
    public void switchLogin() {
        PaPaSDK.getInstance().switchLogin();
    }

    public boolean showAccountCenter() {
        PaPaSDK.getInstance().enterUserCenter();
        return true;
    }

    @Override
    public void submitExtraData(UserExtraData userExtraData) {
        PaPaSDK.getInstance().submitExtraData(userExtraData);
    }

    @Override
    public void exit() {
        PaPaSDK.getInstance().exitSDK();
    }

    @Override
    public boolean isSupportMethod(String methodName) {
        return Arrays.contain(supportedMethods, methodName);
    }

    @Override
    public void queryAntiAddiction() {
        PaPaSDK.getInstance().queryRealName();
    }

}
