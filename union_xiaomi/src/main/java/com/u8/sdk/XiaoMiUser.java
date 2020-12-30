package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class XiaoMiUser extends UserAdapter {

    private String[] supportedMethods = {"login", "switchLogin", "exit", "submitExtraData", "queryAntiAddiction"};

    public XiaoMiUser(Activity activity) {
        XiaoMiSDK.getInstance().initOnCreate();
    }

    public void login() {
        XiaoMiSDK.getInstance().login();
    }

    public void switchLogin() {
        XiaoMiSDK.getInstance().switchLogin();
    }

    public void exit() {
        XiaoMiSDK.getInstance().exit();
    }

    public void submitExtraData(UserExtraData extraData) {
        XiaoMiSDK.getInstance().submitGameData(extraData);
    }

    public boolean isSupportMethod(String methodName) {
        return Arrays.contain(this.supportedMethods, methodName);
    }

}
