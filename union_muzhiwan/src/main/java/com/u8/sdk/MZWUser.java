package com.u8.sdk;

import com.u8.sdk.utils.Arrays;

import android.app.Activity;

public class MZWUser extends UserAdapter{

	private String[] supportedMethods = {"login","switchLogin","logout", "postGiftCode"};
	
	public MZWUser(Activity context){
		MZWSDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
	}
	
	@Override
	public void login() {
		MZWSDK.getInstance().login();
	}

	@Override
	public void switchLogin() {
		MZWSDK.getInstance().logout();
		MZWSDK.getInstance().login();
	}

	@Override
	public void logout() {
		MZWSDK.getInstance().logout();
		
	}
	
	public void postGiftCode(String code){
		MZWSDK.getInstance().postGiftCode(code);
	}

	@Override
	public boolean isSupportMethod(String methodName) {
		return Arrays.contain(supportedMethods, methodName);
	}

}
