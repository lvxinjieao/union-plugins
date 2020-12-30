package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class HuaWeiUser extends UserAdapter {

    private String[] supportedMethods = {"login", "switchLogin", "submitExtraData", "queryAntiAddiction"};

    public HuaWeiUser(Activity activity) {
        HuaWeiSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
    }

    @Override
    public boolean isSupportMethod(String methodName) {
        return Arrays.contain(supportedMethods, methodName);
    }

    @Override
    public void login() {
        HuaWeiSDK.getInstance().login();
    }

    @Override
    public void switchLogin() {
        HuaWeiSDK.getInstance().login();
    }

    @Override
    public void submitExtraData(UserExtraData extraData) {
        HuaWeiSDK.getInstance().submitExtraData(extraData);
    }

    @Override
    public void realNameRegister() {

    }

    @Override
    public void queryAntiAddiction() {
        //华为自己托管了实名限制，这里直接返回18
        U8SDK.getInstance().onResult(U8Code.CODE_ADDICTION_ANTI_RESULT, "18");
        //HuaWeiSDK.getInstance().checkRealName();
    }
}
