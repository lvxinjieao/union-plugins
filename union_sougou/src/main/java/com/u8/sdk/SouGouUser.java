package com.u8.sdk;

import com.u8.sdk.utils.Arrays;

import android.app.Activity;

public class SouGouUser extends UserAdapter{
	
	private String[] supportedMethods = {"login","logout","switchLogin","exit"};
	
	public SouGouUser(Activity context){
		U8SDK.getInstance().onInitResult(U8Code.CODE_INIT_SUCCESS, "init success");
	}
	
	@Override
	public void login() {
		SouGouSDK.getInstance().login();
	}

	@Override
	public void logout() {
		SouGouSDK.getInstance().logout();
	}

	@Override
	public void switchLogin() {
		SouGouSDK.getInstance().switchAccount();
	}
	
	@Override
	public void exit() {
		SouGouSDK.getInstance().exitSDK();
	}
	
	@Override
	public boolean isSupportMethod(String methodName) {
		return Arrays.contain(supportedMethods, methodName);
	}

}
