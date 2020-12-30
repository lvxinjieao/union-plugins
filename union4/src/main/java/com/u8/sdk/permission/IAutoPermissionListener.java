package com.u8.sdk.permission;

public interface IAutoPermissionListener {

    void onAutoPermissionSuccess();

    void onAutoPermissionFailed(String[] deniedPermissions, String[] deniedForeverPermissions);
}
