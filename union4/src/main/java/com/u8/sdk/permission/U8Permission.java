package com.u8.sdk.permission;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import com.u8.sdk.U8SDK;
import com.u8.sdk.permission.utils.PermissionHelper;
import com.u8.sdk.utils.Logs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * U8SDK 权限操作辅助类
 */
public class U8Permission {

    private static U8Permission instance;

    private Map<Integer, IPermissionResultListener> listenerMap;

    public static U8Permission getInstance(){
        if(instance == null){
            instance = new U8Permission();
        }
        return instance;
    }

    private U8Permission(){
        listenerMap = new HashMap<Integer, IPermissionResultListener>();
    }

    /**
     * 是否android 23以上
     * @return
     */
    public boolean isOverMarshmallow() {
        return Build.VERSION.SDK_INT >= 23;
    }

    /**
     * 是否需要申请权限
     * @return
     */
    public boolean isNeedAskPermission(){

        return isOverMarshmallow(); //|| OSUtils.isOSNeedPermission();
    }

    /**
     * 是否需要申请权限
     * @return
     */
    public boolean isNeedAskPermission(Activity context, String[] permissions){

        if(!isNeedAskPermission()){
            return false;
        }

        List<String> deniedPermissions = findDeniedPermissions(context, permissions);
        return deniedPermissions != null && deniedPermissions.size() > 0;
    }

    /**
     * 申请权限调用接口
     * @param context
     * @param requestCode
     * @param permissions
     * @param listener
     */
    public void requestPermissions(Activity context, int requestCode, String[] permissions, IPermissionResultListener listener){

        if(!isNeedAskPermission(context, permissions)) {
            listener.onPermissionSuccess();
            return;
        }

        if(listenerMap.containsKey(requestCode)){
            listener.onPermissionCanceled();
            Logs.e("U8SDK", "requestPermissions failed. requestCode already exists");
            return;
        }

        listenerMap.put(requestCode, listener);

        List<String> deniedPermissions = findDeniedPermissions(context, permissions);
        requestPermissions(context, requestCode, deniedPermissions, listener);
    }

    public void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions,
                                                  int[] grantResults) {

        if(!listenerMap.containsKey(requestCode)){
            return;
        }

        IPermissionResultListener listener = listenerMap.get(requestCode);

        onPermisionCheckResult(activity, requestCode, permissions, grantResults, listener);

    }

    @SuppressLint("NewApi")
	public static void requestWriteSetting(Activity context, final IPermissionWriteSettingListener grantedListener) {

        if(Build.VERSION.SDK_INT >= 23) {
            try{

                if (!Settings.System.canWrite(context)) {
                    grantedListener.onPermissionDenied();
                } else {
                    grantedListener.onPermissionGranted();
                }

            }catch (Exception e){
                e.printStackTrace();
                grantedListener.onPermissionGranted();
            }

        }else {
            grantedListener.onPermissionGranted();
        }
    }


    @TargetApi(value = 23)
    private List<String> findDeniedPermissions(Activity activity,
                                                      String... permission) {
        List<String> denyPermissions = new ArrayList<String>();
        for (String value : permission) {
            if (!PermissionHelper.checkSelfPermission(activity, value)) {
                denyPermissions.add(value);
            } else {
                // 适配特殊机型，比如小米
//                AppOpsManager appOpsManager = (AppOpsManager) activity
//                        .getSystemService(Context.APP_OPS_SERVICE);
//                int checkOp = appOpsManager.checkOp(
//                        AppOpsManager.OPSTR_READ_PHONE_STATE, android.os.Process.myUid(),
//                        activity.getPackageName());
//                if (checkOp != AppOpsManager.MODE_ALLOWED) {
//                    denyPermissions.add(value);
//                }
            }
        }

        Logs.d("U8SDK", "findDeniedPermissions->curr denied permissions:");

        for(String p : denyPermissions){
            Logs.d("U8SDK", p);
        }
        Logs.d("U8SDK", "findDeniedPermissions->end");
        return denyPermissions;
    }

    @TargetApi(value = 23)
    private void onPermisionCheckResult(Activity context, int requestCode, String[] permissions,
                                               int[] grantResults, IPermissionResultListener listener){

        try{
            listenerMap.remove(requestCode);

            List<String> deniedPermissions = findDeniedPermissions(context, permissions);
            if(deniedPermissions.size() > 0){

                List<String> deniedForeverPermissions = new ArrayList<String>();
                for(String permission : deniedPermissions){
                    if(!context.shouldShowRequestPermissionRationale(permission)){
                        deniedForeverPermissions.add(permission);
                    }
                }

                listener.onPermissionFailed(deniedPermissions.toArray(new String[deniedPermissions.size()]), deniedForeverPermissions.toArray(new String[deniedForeverPermissions.size()]));
                Logs.d("U8SDK", "onPermissionCheckResult failed.");
                for(String p : deniedPermissions){
                    Logs.d("U8SDK", "permission " + p + " denied");
                }

            } else {
                listener.onPermissionSuccess();
            }
        }catch (Exception e){
            e.printStackTrace();
            listenerMap.remove(requestCode);
            listener.onPermissionFailed(permissions, null);
        }

    }

    @TargetApi(value = 23)
    private void requestPermissions(Activity context, int requestCode, List<String> permissions, IPermissionResultListener listener){

        if(permissions == null || permissions.size() == 0){
            listenerMap.remove(requestCode);
            listener.onPermissionSuccess();

            return;
        }

        Logs.d("U8SDK", "begin to request permissions . size:"+permissions.size());

        context.requestPermissions(permissions.toArray(new String[permissions.size()]), requestCode);

    }
}
