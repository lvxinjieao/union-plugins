package com.u8.sdk.permission.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import com.u8.sdk.utils.Logs;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * 在vivo和oppo等设备上面， 申请权限，不管用户是同意还是拒绝， checkSelfPermission始终返回已授权
 * 特殊机型，采用特殊手段来判断权限是否开启
 */
public class PermissionHelper {

    public static boolean checkSelfPermission(Activity context, String permission){
        return selfPermissionGranted(context, permission);
    }

    // 对于国内部分机型做非正常权限申请
    private static boolean fuckSpecialOSPermission(Activity context, String permission){

        Logs.d("U8SDK", "check permission with special method." + permission);

        switch (permission){

            case Manifest.permission.CAMERA:
                return checkCameraPermissions(context);

            case Manifest.permission.RECORD_AUDIO:
                return checkAudioPermission(context, permission);

            case Manifest.permission.BODY_SENSORS:
                return checkSensorsPermission(context, permission);

            case Manifest.permission.READ_CALENDAR:
            case Manifest.permission.WRITE_CALENDAR:
                return checkCalanderPermission(context, permission);

            case Manifest.permission.READ_CONTACTS:
            case Manifest.permission.WRITE_CONTACTS:
            case Manifest.permission.GET_ACCOUNTS:
                return checkContactsPermission(context, permission);

            case Manifest.permission.ACCESS_FINE_LOCATION:
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                return checkLocationsPermission(context, permission);

            case Manifest.permission.READ_PHONE_STATE:
            case Manifest.permission.CALL_PHONE:
            case Manifest.permission.READ_CALL_LOG:
            case Manifest.permission.WRITE_CALL_LOG:
            case Manifest.permission.ADD_VOICEMAIL:
            case Manifest.permission.USE_SIP:
            case Manifest.permission.PROCESS_OUTGOING_CALLS:
                return checkReadPhoneStatePermission(context, permission);

            case Manifest.permission.SEND_SMS:
            case Manifest.permission.RECEIVE_SMS:
            case Manifest.permission.READ_SMS:
            case Manifest.permission.RECEIVE_WAP_PUSH:
            case Manifest.permission.RECEIVE_MMS:
                return checkSMSPermission(context, permission);

            case Manifest.permission.READ_EXTERNAL_STORAGE:
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return checkWritePermission(context, permission);

            default:
                return selfPermissionGranted(context, permission);
        }

    }

    /**
     * READ_PHONE_STATE
     * @return 是否有读取手机状态的权限
     * 电话	READ_PHONE_STATE	危险	允许对电话状态进行只读访问,包括设备的电话号码，当前蜂窝网络信息,任何正在进行的呼叫的状态以及设备上注册的任何PhoneAccounts列表
     * 电话	CALL_PHONE	危险	允许应用程序在不通过拨号器用户界面的情况下发起电话呼叫，以便用户确认呼叫
     * 电话	READ_CALL_LOG	危险	允许应用程序读取用户的通话记录
     * 电话	WRITE_CALL_LOG	危险	允许应用程序写入（但不读取）用户的呼叫日志数据
     * 电话	ADD_VOICEMAIL	危险	允许应用程序将语音邮件添加到系统中
     * 电话	USE_SIP	危险	允许应用程序使用SIP服务
     * 电话	PROCESS_OUTGOING_CALLS	危险	允许应用程序查看拨出呼叫期间拨打的号码，并选择将呼叫重定向到其他号码或完全中止呼叫
     * 耗时3ms左右
     */
    @SuppressLint({"MissingPermission"})
    private static boolean checkReadPhoneStatePermission(Activity context, String permission) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        try {
            tm.getDeviceId();
            return selfPermissionGranted(context, permission);
        }catch (Exception e){
            return false;
        }
    }


    /**
     * 检查是否拥有读写权限
     * WRITE_EXTERNAL_STORAGE
     * 耗时 1-12ms
     * @return
     */
    private static boolean checkWritePermission(Activity context, String permission) {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "check_permission.dxt");
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.flush();
            outputStream.close();
            file.delete();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return selfPermissionGranted(context, permission);
    }

    /**
     * 日历	READ_CALENDAR	危险	允许应用程序读取用户的日历数据
     * 日历	WRITE_CALENDAR	危险	允许应用程序写入用户的日历数据
     * 检查日历权限
     * 耗时 30ms左右
     * @return
     */
    private static boolean checkCalanderPermission(Activity context, String permission){
        Cursor cursor = null;
        try{
            String CALANDER_EVENT_URL = "content://com.android.calendar/events";
            Uri uri = Uri.parse(CALANDER_EVENT_URL);
            cursor = context.getContentResolver().query(uri, null, null, null, null);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return selfPermissionGranted(context, permission);
    }


    /**
     * 检测相机权限，这个因为直接拿camera还不一定行，当前测试机型：oppo 魅族 小米
     * * 相机	CAMERA	危险	使用摄像头做相关工作
     * @return
     */
    private static boolean checkCameraPermissions(Activity context) {
        Camera mCamera = null;
        try {
            // 大众路线 过了基本就是有权限
            mCamera = Camera.open();
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);

            // 特殊路线，oppo和vivo得根据rom去反射mHasPermission
            Field fieldPassword;
            if(mCamera == null) mCamera = Camera.open();
            //通过反射去拿相机是否获得了权限
            fieldPassword = mCamera.getClass().getDeclaredField("mHasPermission");
            fieldPassword.setAccessible(true);
            Boolean result = (Boolean) fieldPassword.get(mCamera);
            if (mCamera != null) {
                mCamera.release();
            }
            mCamera = null;
            return result;
        } catch (Exception e) {
            if (mCamera != null) {
                mCamera.release();
            }
            mCamera = null;
            e.printStackTrace();
        }
        return selfPermissionGranted(context, Manifest.permission.CAMERA);
    }

    /**
     * 联系人	READ_CONTACTS	危险	读取联系人
     * 联系人	WRITE_CONTACTS	危险	写入联系人
     * 联系人	GET_ACCOUNTS	危险	允许访问帐户服务中的帐户列表
     * @return
     */
    private static boolean checkContactsPermission(Activity context, String permission){
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return selfPermissionGranted(context, permission);
    }

    /**
     *
     * 位置	ACCESS_FINE_LOCATION	危险	允许应用访问精确位置
     * 位置	ACCESS_COARSE_LOCATION	危险	允许应用访问大致位置
     * @return
     */
    private static boolean checkLocationsPermission(Activity context, String permission){
        try {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            // 无用操作
            lm.getAllProviders();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return selfPermissionGranted(context, permission);
    }

    /**
     *
     * 麦克风	RECORD_AUDIO  危险	麦克风的使用
     * @return
     */
    private static boolean checkAudioPermission(Activity context, String permission){
        try {
            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            // 无用操作
            audioManager.getMode();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return selfPermissionGranted(context, permission);
    }

    /**
     *
     * 传感器	BODY_SENSORS	危险	允许应用程序访问来自传感器的数据
     * @return
     */
    private static boolean checkSensorsPermission(Activity context, String permission){
        try {
            SensorManager sorMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            // 无用操作
            sorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return selfPermissionGranted(context, permission);
    }


    /**
     *
     * 短信	SEND_SMS	危险	允许应用程序发送SMS消息
     * 短信	RECEIVE_SMS	危险	允许应用程序接收SMS消息
     * 短信	READ_SMS	危险	允许应用程序读取SMS消息
     * 短信	RECEIVE_WAP_PUSH	危险	允许应用程序接收WAP推送消息
     * 短信	RECEIVE_MMS	危险	允许应用程序监视传入的MMS消息（彩信）
     * * @return
     */
    private static boolean checkSMSPermission(Activity context, String permission){
        Cursor cursor = null;
        try {
            Uri uri = Uri.parse("content://sms/failed");
            String[] projection = new String[] { "_id", "address", "person",
                    "body", "date", "type", };
            cursor = context.getContentResolver().query(uri, projection, null,
                    null, "date desc");
        } catch (Exception e){
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return selfPermissionGranted(context, permission);
    }

    @TargetApi(23)
    private static boolean selfPermissionGranted(Activity context, String permission){
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

}
