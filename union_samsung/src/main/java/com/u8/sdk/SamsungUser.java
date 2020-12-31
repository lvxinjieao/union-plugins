package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class SamsungUser extends UserAdapter {

	private String[] supportedMethods = {"login","switchLogin", "queryAntiAddiction"};
	
	public SamsungUser(Activity context){
		SamsungSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
	}
	
	@Override
	public boolean isSupportMethod(String methodName) {
		return Arrays.contain(supportedMethods, methodName);
	}
	
	@Override
	public void login() {
		SamsungSDK.getInstance().login();
	}

	@Override
	public void switchLogin() {
		SamsungSDK.getInstance().login();
	}

	@Override
	public void queryAntiAddiction() {
		SamsungSDK.getInstance().queryRealName();
	}

}