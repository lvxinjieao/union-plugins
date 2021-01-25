package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class QuickUser extends UserAdapter {

	private String[] supportedMethods = {"login","switchLogin", "logout", "submitExtraData", "exit"};
	
	public QuickUser(Activity context){
		MyQuickSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
	}
	
	@Override
	public boolean isSupportMethod(String methodName) {
		return Arrays.contain(supportedMethods, methodName);
	}
	
	@Override
	public void login() {
		MyQuickSDK.getInstance().login();
	}

	@Override
	public void switchLogin() {
		MyQuickSDK.getInstance().login();
	}

	@Override
	public void logout() {
		MyQuickSDK.getInstance().logout();
	}

	@Override
	public void submitExtraData(UserExtraData extraData) {
		MyQuickSDK.getInstance().submitGameData(extraData);
	}

	@Override
	public void exit() {
		MyQuickSDK.getInstance().exit();
	}

}
