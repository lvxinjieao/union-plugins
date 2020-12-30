package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.log.Log;
import com.u8.sdk.utils.Arrays;

public class OppoUser extends UserAdapter {

    private String[] supportedMethods = {"login","logout", "switchLogin", "submitExtraData", "exit"};

    public OppoUser(Activity activity) {
        try {
            Log.d("U8SDK", "enter oppo sdk user init...");
            OppoSDK.getInstance().onActivityCreate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void login() {
        OppoSDK.getInstance().login();
    }

    @Override
    public void logout() {
        OppoSDK.getInstance().logout();
    }

    @Override
    public void switchLogin() {
        OppoSDK.getInstance().switchAccount();
    }

    @Override
    public void submitExtraData(UserExtraData extraData) {
        OppoSDK.getInstance().sendExtraData(extraData);
    }

	@Override
	public void exit() {
		OppoSDK.getInstance().sdkExit();
	}

    @Override
    public boolean isSupportMethod(String methodName) {
        return Arrays.contain(supportedMethods, methodName);
    }
}
