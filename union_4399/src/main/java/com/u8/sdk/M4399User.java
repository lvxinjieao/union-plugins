package com.u8.sdk;

import com.u8.sdk.utils.Arrays;

import android.app.Activity;

public class M4399User extends UserAdapter{

	private String[] supportedMethods = {"login", "switchLogin", "logout","submitExtraData","exit","realNameRegister", "queryAntiAddiction"};
	
	public M4399User(Activity context){
		M4399SDK.getInstance().initSDK(U8SDK.getInstance().getSDKParams());
	}
	
	@Override
	public void login() {

		M4399SDK.getInstance().login();
		
	}

	@Override
	public void switchLogin() {
		M4399SDK.getInstance().switchAccount();
		
	}

	@Override
	public void logout() {
		M4399SDK.getInstance().logout();
		
	}

	@Override
	public void submitExtraData(UserExtraData extraData) {
		M4399SDK.getInstance().sendExtraData(extraData);
	}

	@Override
	public void exit() {
		M4399SDK.getInstance().exitSDK();
		
	}

	@Override
	public void realNameRegister() {

		M4399SDK.getInstance().callRealName();

	}

	@Override
	public void queryAntiAddiction() {
		M4399SDK.getInstance().checkRealName();
	}

	@Override
	public boolean isSupportMethod(String methodName) {

		return Arrays.contain(supportedMethods, methodName);
	}

}
