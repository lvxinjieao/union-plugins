package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.plugin.U8SpecialInterface;
import com.u8.sdk.utils.Arrays;

public class YSDKUser extends UserAdapter {

    private String[] supportedMethods = {"login", "switchLogin", "logout", "queryAntiAddiction", "submitExtraData"};

    public YSDKUser(Activity activity) {
        try {
            YSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void login() {
        YSDK.getInstance().isLoginCustom = false;
        if (YSDK.getInstance().useLogin) {
            YSDK.getInstance().login();
        }
    }

    @Override
    public void login(String customData) {
        YSDK.getInstance().isLoginCustom = true;
        if (YSDK.getInstance().useLogin) {
            if ("QQ".equalsIgnoreCase(customData)) {
                YSDK.getInstance().login(YSDK.LOGIN_TYPE_QQ);
            } else if ("WX".equalsIgnoreCase(customData)) {
                YSDK.getInstance().login(YSDK.LOGIN_TYPE_WX);
            }
        }
    }


    @Override
    public void switchLogin() {
        if (YSDK.getInstance().useLogin)
            YSDK.getInstance().switchLogin();
    }


    @Override
    public void logout() {
        try {
            if (YSDK.getInstance().useLogin) {
                YSDK.getInstance().logout();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void submitExtraData(UserExtraData extraData) {
        YSDK.getInstance().submitExtraData(extraData);
    }


    @Override
    public void queryAntiAddiction() {
        boolean useYSDKRealname = U8SDK.getInstance().getSDKParams().getBoolean("YSDK_REAL_NAME");
        if (useYSDKRealname) {//ysdk 托管实名
            U8SDK.getInstance().onResult(U8Code.CODE_ADDICTION_ANTI_RESULT, "18");
        } else {//走游戏自己的实名
            U8SDK.getInstance().onResult(U8Code.CODE_ADDICTION_ANTI_RESULT, "0");
        }
    }


    @Override
    public boolean isSupportMethod(String methodName) {
        return Arrays.contain(supportedMethods, methodName);
    }

}
