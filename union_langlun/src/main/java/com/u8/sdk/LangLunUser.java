package com.u8.sdk;

import com.u8.sdk.utils.Arrays;

import android.app.Activity;

public class LangLunUser extends UserAdapter {

    public String[] supportedMethods = {"login", "logout", "submitExtraData", "exit"};

    public LangLunUser(Activity activity) {
        LangLunSDK.getInstance().init(activity);
    }

    public void login() {
        LangLunSDK.getInstance().login();
    }

    public void logout() {
        LangLunSDK.getInstance().logout();
    }

    public void submitExtraData(UserExtraData extraData) {
        LangLunSDK.getInstance().submitExtendData(extraData);
    }

    public void exit() {
        LangLunSDK.getInstance().exit();
    }

    public boolean isSupportMethod(String methodName) {
        return Arrays.contain(this.supportedMethods, methodName);
    }
}
