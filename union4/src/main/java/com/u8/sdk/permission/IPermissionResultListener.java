package com.u8.sdk.permission;

public interface IPermissionResultListener {

    void onPermissionSuccess();

    void onPermissionFailed(String[] failedPermission, String[] foreverDeniedPermissions);

    void onPermissionCanceled();
}
