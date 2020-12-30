package com.u8.sdk;

import com.u8.sdk.utils.Arrays;
import android.app.Activity;

public class MMUser extends UserAdapter {
	
	public String[] supportedMethods = { "login", "logout", "submitExtraData", "exit" };

	public MMUser(Activity activity) {
		MMSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());	}

	public void login() {
		MMSDK.getInstance().login();
	}

	public void logout() {
		MMSDK.getInstance().logout();
	}

	public void submitExtraData(UserExtraData extraData) {
		MMSDK.getInstance().submitExtendData(extraData);
	}

	public void exit() {
		MMSDK.getInstance().exit();
	}

	public boolean isSupportMethod(String methodName) {
		return Arrays.contain(this.supportedMethods, methodName);
	}
}
