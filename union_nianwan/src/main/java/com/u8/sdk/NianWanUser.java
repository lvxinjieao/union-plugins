package com.u8.sdk;

import com.u8.sdk.utils.Arrays;
import android.app.Activity;

public class NianWanUser extends UserAdapter {
	
	public String[] supportedMethods = { "login", "logout", "submitExtraData", "exit" };

	public NianWanUser(Activity activity) {
		NianWanSDK.getInstance().init(activity);
	}

	public void login() {
		NianWanSDK.getInstance().login();
	}

	public void logout() {
		NianWanSDK.getInstance().logout();
	}

	public void submitExtraData(UserExtraData extraData) {
		NianWanSDK.getInstance().submitExtendData(extraData);
	}

	public void exit() {
		NianWanSDK.getInstance().exit();
	}

	public boolean isSupportMethod(String methodName) {
		return Arrays.contain(this.supportedMethods, methodName);
	}
}
