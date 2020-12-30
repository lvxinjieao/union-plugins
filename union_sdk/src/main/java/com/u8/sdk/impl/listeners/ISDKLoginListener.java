package com.u8.sdk.impl.listeners;

public interface ISDKLoginListener {

	public void onSuccess(String id, String name);

	public void onFailed(int code);
}
