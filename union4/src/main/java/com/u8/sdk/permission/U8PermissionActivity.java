package com.u8.sdk.permission;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.u8.sdk.U8SDK;
import com.u8.sdk.base.IActivityCallback;
import com.u8.sdk.permission.utils.OSUtils;
import com.u8.sdk.utils.Logs;

public class U8PermissionActivity extends Activity {

    private IPermissionLifeCycle permissionLifeCycle;      //ysdk等渠道有特殊调用需求

    private IAutoPermissionListener permissionListener = new IAutoPermissionListener() {
        @Override
        public void onAutoPermissionSuccess() {
            Logs.d("U8SDK", "u8 auto permission success. goto next activity");
            startNextActivity();
        }

        @Override
        public void onAutoPermissionFailed(String[] deniedPermissions, String[] deniedForeverPermissions) {
            Logs.d("U8SDK", "u8 auto permission failed. goto next activity");
            startNextActivity();
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(ResourceHelper.getIdentifier(this, "R.layout.u8_permission_layout"));
        //this.getWindow().getDecorView().setBackgroundColor(Color.WHITE);

//        if(!isTaskRoot()){
//            Log.d("U8SDK", "the permission activity is not task root. just finished.");
//            finish();
//            return;
//        }

        U8AutoPermission.getInstance().setAutoPermission(true);
        this.permissionLifeCycle = U8AutoPermission.getInstance().getPermissionLifeCycle();

        if(this.permissionLifeCycle != null){
            this.permissionLifeCycle.onCreate(this);
        }

        Logs.d("U8SDK", "u8 auto permission begin.");
        U8AutoPermission.getInstance().setDirectPermission(false);

        requestPermissions();

    }

    private void requestPermissions(){
        try{
            U8AutoPermission.getInstance().requestDangerousPermissions(this, this.permissionListener);
        }catch (Exception e){
            startNextActivity();    //权限异常， 直接跳转到下一个activity， 这里如果严格模式， 可以注释掉
            Logs.e("U8SDK", "auto request permission failed. exception:"+e.getMessage());
            e.printStackTrace();
        }
    }



    private void startNextActivity() {
        try {
            if(!U8AutoPermission.getInstance().isAlreadyDone()){

                U8AutoPermission.getInstance().setAlreadyDone(true);
                Class<?> mainClass = Class.forName("{U8SDK_Permission_Next_Activity}");
                Intent intent = new Intent(this, mainClass);
                startActivity(intent);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }finally {
            finish();
        }
    }

    protected void onNewIntent(Intent newIntent){
        super.onNewIntent(newIntent);

        Logs.d("U8SDK", "onNewIntent called in U8PermissionActivity");
        if(permissionLifeCycle != null){
            permissionLifeCycle.onNewIntent(this, newIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        U8AutoPermission.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Logs.d("U8SDK", "U8PermissionActivity onActivityResult");
        if(permissionLifeCycle != null){
            permissionLifeCycle.onActivityResult(this, requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onStart(){
        Logs.d("U8SDK", "U8PermissionActivity onStart");
        if(permissionLifeCycle != null){
            permissionLifeCycle.onStart(this);
        }
        super.onStart();
    }

    public void onPause(){
        Logs.d("U8SDK", "U8PermissionActivity onPause");
        if(permissionLifeCycle != null){
            permissionLifeCycle.onPause(this);
        }
        super.onPause();
    }
    public void onResume(){
        super.onResume();
        Logs.d("U8SDK", "U8PermissionActivity onResume");

        if(U8AutoPermission.getInstance().isJumpingPermission()){
            Logs.d("U8SDK", "permission return from permission page. request again to check permission");
            requestPermissions();
            return;
        }else if(U8AutoPermission.getInstance().isJumpingWriteSettings()){
            Logs.d("U8SDK", "permission return from write settings page. request again to check permission");
            U8AutoPermission.getInstance().requestWriteSettings(this);
            return;
        }

        if(permissionLifeCycle != null){
            permissionLifeCycle.onResume(this);
        }

    }

    public void onStop(){
        Logs.d("U8SDK", "U8PermissionActivity onStop");
        if(permissionLifeCycle != null){
            permissionLifeCycle.onStop(this);
        }
        super.onStop();
    }
    public void onDestroy(){
        Logs.d("U8SDK", "U8PermissionActivity onDestroy");
        if(permissionLifeCycle != null){
            permissionLifeCycle.onDestroy(this);
        }
        super.onDestroy();
    }
    public void onRestart(){
        Logs.d("U8SDK", "U8PermissionActivity onReStart");
        if(permissionLifeCycle != null){
            permissionLifeCycle.onRestart(this);
        }
        super.onRestart();
    }

}
