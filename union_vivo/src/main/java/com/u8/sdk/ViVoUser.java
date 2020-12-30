package com.u8.sdk;

import android.app.Activity;

import com.u8.sdk.utils.Arrays;

public class ViVoUser extends UserAdapter /**implements ISpecialInterface*/{

	private String[] supportedMethods = {"login","switchLogin","submitExtraData", "exit", "queryAntiAddiction"};
	
	public ViVoUser(Activity context){
//		U8SpecialInterface.getInstance().setSpecialInterface(this);
		ViVoSDK.getInstance().initOnCreate();
	}
	
	@Override
	public void login() {
		ViVoSDK.getInstance().login();
	}

	@Override
	public void switchLogin() {
		ViVoSDK.getInstance().login();
	}
	
	@Override
	public void submitExtraData(UserExtraData extraData) {
		ViVoSDK.getInstance().submitExtraData(extraData);
	}
	
	@Override
	public void exit() {
		ViVoSDK.getInstance().sdkExit();
	}

	@Override
	public boolean isSupportMethod(String methodName) {
		return Arrays.contain(supportedMethods, methodName);
	}

//	@Override
//	public boolean isFromGameCenter(Activity context) {
//		return false;
//	}
//
//	@Override
//	public void queryAntiAddiction() {
//		ViVoSDK.getInstance().queryRealName(false);
//	}
//
//	@Override
//	public void realNameRegister() {
//		//ViVoSDK.getInstance().queryRealName(false);
//	}
//
//	@Override
//	public void showGameCenter(Activity context) {
//		ViVoSDK.getInstance().openForum();
//	}
//
//	@Override
//	public void showPostDetail(Activity context, String postId, String extra) {
//
//	}
//
//	@Override
//	public void performFeature(Activity context, String type) {
//		ViVoSDK.getInstance().openForum();
//	}
//
//	@Override
//	public void callSpecailFunc(Activity context, String funcName, SDKParams params) {
//
//	}


}
