package com.u8.sdk.impl.listeners;

public interface ISDKRegisterOnekeyListener {
	public void onSuccess(String id, String name, String password);

	public void onFailed(int code);
}
