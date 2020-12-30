package com.u8.sdk.permission;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Xml;

import com.u8.sdk.SDKTools;
import com.u8.sdk.U8SDK;
import com.u8.sdk.permission.utils.PermissionPageUtils;
import com.u8.sdk.utils.GUtils;
import com.u8.sdk.utils.Logs;
import com.u8.sdk.utils.StoreUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * U8SDK 权限自动处理类
 */
public class U8AutoPermission implements IPermissionResultListener {

    private static final int CODE_U8_PERMISSION = 20151001;

    private static U8AutoPermission instance;

    private Map<String, U8PermissionInfo> permissions;

    private boolean loaded = false;

    private Activity context;
    private IAutoPermissionListener listener;
    private IPermissionLifeCycle permissionLifeCycle;
    private boolean autoPermission = false;     //在后面activity中判断当前是否走过自动权限流程
    private boolean alreadyDone = false;        //处理ysdk等登陆回调之后会重启launcher activity等问题
    private boolean directPermission = true;   //是否直接在当前activity中申请权限


    private boolean needWriteSetting = false;       //是否需要write settings 权限

    private boolean jumpingPermission = false;
    private boolean jumpingWriteSettings = false;

    private String protocolUrl;
    private String protocolOrientation;

    private U8AutoPermission() {

    }

    public static U8AutoPermission getInstance() {
        if (instance == null) {
            instance = new U8AutoPermission();
        }
        return instance;
    }

    public IPermissionLifeCycle getPermissionLifeCycle() {
        return this.permissionLifeCycle;
    }

    public void setPermissionLifeCycle(IPermissionLifeCycle callback) {
        this.permissionLifeCycle = callback;
    }

    public void init(Context context) {
        if (loaded) {
            return;
        }

        this.permissions = loadPermissions(context);
        this.loaded = true;
    }

    public void requestDangerousPermissions(final Activity context, final IAutoPermissionListener listener) {

        if (!loaded) {
            init(context);
        }

        final List<String> ps = getDangerousPermissions();
        if (ps == null || ps.size() == 0) {
            Logs.d("U8SDK", "there is no dangerous permission");
            listener.onAutoPermissionSuccess();
            return;
        }

        if (!StoreUtils.getBoolean(context, "u8_permission_dialog_showed", false)) {
            Logs.d("U8SDK", "u8 protocol url configed. now to show protocol dialog.");
            U8PermissionDialog.showDialog(context, this.protocolUrl, this.protocolOrientation, new ArrayList<U8PermissionInfo>(this.permissions.values()), new IProtocolListener() {
                @Override
                public void onAgreed() {
                    StoreUtils.putBoolean(context, "u8_permission_dialog_showed", true);
                    doRequestDangerousPermissions(context, ps, listener);
                }
            });
        } else {
            Logs.d("U8SDK", "u8 protocol url not configed. don't show protocol dialog.");
            doRequestDangerousPermissions(context, ps, listener);
        }
    }

    public String getProtocolUrl() {

        return protocolUrl;
    }

    private void doRequestDangerousPermissions(Activity context, List<String> ps, IAutoPermissionListener listener) {
        this.context = context;
        this.listener = listener;
        this.jumpingPermission = false;
        this.jumpingWriteSettings = false;
        requestPermissions(ps.toArray(new String[ps.size()]));
    }


    public void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions,
                                           int[] grantResults) {

        if (requestCode != CODE_U8_PERMISSION) {
            return;
        }

        Logs.d("U8SDK", "U8AutoPermission: onRequestPermissionsResult called.");
        U8Permission.getInstance().onRequestPermissionsResult(activity, requestCode, permissions, grantResults);

    }


    private List<String> getDangerousPermissions() {

        if (this.permissions == null) {
            return null;
        }

        List<String> result = new ArrayList<String>();
        for (U8PermissionInfo p : this.permissions.values()) {
            result.add(p.getName());
        }

        return result;

    }

    //加载assets下面的危险权限列表
    private Map<String, U8PermissionInfo> loadPermissions(Context context) {

        String xmlPlugins = SDKTools.getAssetConfigs(context, "u8_permissions.xml");

        if (xmlPlugins == null) {
            Logs.e("U8SDK", "fail to load u8_permissions.xml");
            return null;
        }

        XmlPullParser parser = Xml.newPullParser();
        Map<String, U8PermissionInfo> permissionList = new HashMap<String, U8PermissionInfo>();
        try {
            parser.setInput(new StringReader(xmlPlugins));

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String tag = parser.getName();
                        if ("permission".equals(tag)) {
                            String name = parser.getAttributeValue(null, "name");
                            String cname = parser.getAttributeValue(null, "cname");
                            String group = parser.getAttributeValue(null, "group");

                            U8PermissionInfo permissionInfo = new U8PermissionInfo(name, cname, group);
                            permissionList.put(name, permissionInfo);
                            Logs.d("U8SDK", "load a dangerous permission: " + name + "; group:" + group);
                        } else if ("permissions".equals(tag)) {
                            String writeSettings = parser.getAttributeValue(null, "writeSettings");
                            if (!TextUtils.isEmpty(writeSettings)) {
                                try {
                                    Logs.d("U8SDK", "sdk current need write settings permission:" + writeSettings);
                                    needWriteSetting = Boolean.valueOf(writeSettings);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            protocolUrl = parser.getAttributeValue(null, "protocolUrl");
                            protocolOrientation = parser.getAttributeValue(null, "protocolOrientation");

                        }
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return permissionList;
    }

    private void requestPermissions(String[] permissions) {
        U8Permission.getInstance().requestPermissions(context, CODE_U8_PERMISSION, permissions, this);
    }

    private void showPermissionTipDialog(final String[] failedPermissions, final boolean gotoPermissionSettings) {
        try {

            String tips = getFailedPermissionNames(failedPermissions);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("权限申请失败");
            builder.setMessage("为了游戏能正常进行，我们需要访问您的 " + tips + " 权限");
            builder.setCancelable(true);
            builder.setPositiveButton("退出游戏",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            //这里什么都不用做
                            Logs.d("U8SDK", "user cancel to open permission. so app kill self.");
                            U8SDK.getInstance().getContext().finish();
                            System.exit(0);
                        }
                    });
            builder.setNeutralButton(gotoPermissionSettings ? "开启权限" : "确  定",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {

                            if (gotoPermissionSettings) {
                                jumpingPermission = true;
                                PermissionPageUtils.jumpPermissionPageDefault(U8SDK.getInstance().getApplication());
                            } else {
                                requestPermissions(failedPermissions);
                            }


                        }
                    });
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showWriteSettingTipDialog() {
        try {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("权限申请");
            builder.setMessage("为了游戏能正常进行，我们需要访问您的 【修改设置】 权限，请前往系统设置中进行开启");
            builder.setCancelable(true);
            builder.setPositiveButton("退出游戏",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            //这里什么都不用做
                            Logs.d("U8SDK", "user cancel to open permission. so app kill self.");
                            U8SDK.getInstance().getContext().finish();
                            System.exit(0);
                        }
                    });
            builder.setNeutralButton("开启权限",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            jumpingWriteSettings = true;
                            PermissionPageUtils.jumpWriteSettingPage(U8SDK.getInstance().getApplication());
                        }
                    });
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String getFailedPermissionNames(String[] failedPermissions) {

        String tips = "";
        for (String p : failedPermissions) {
            U8PermissionInfo pInfo = this.permissions.get(p);
            if (pInfo != null) {
                tips += "【" + pInfo.getCname() + "】";
            }
        }
        return tips;
    }

    @Override
    public void onPermissionSuccess() {
        jumpingPermission = false;

        if (needWriteSetting) {
            requestWriteSettings(context);
        } else {
            if (listener != null) {
                listener.onAutoPermissionSuccess();
            }
        }
    }

    public void requestWriteSettings(Activity context) {

        jumpingWriteSettings = false;
        U8Permission.getInstance().requestWriteSetting(context, new IPermissionWriteSettingListener() {
            @Override
            public void onPermissionGranted() {
                Logs.d("U8SDK", "write settings permission already granted.");
                if (listener != null) {
                    listener.onAutoPermissionSuccess();
                }
            }

            @Override
            public void onPermissionDenied() {
                //goto settings
                Logs.d("U8SDK", "write settings permission was denied.");
                showWriteSettingTipDialog();
//                if(listener != null){
//                    listener.onAutoPermissionSuccess();
//                }
            }
        });
    }

    @Override
    public void onPermissionFailed(String[] failedPermissions, String[] deniedForeverPermissions) {

        //严格模式， 这里直接处理失败之后的提示和重复申请，但是部分渠道要求， 权限被拒绝，需要依然可以进入游戏

        boolean strictMode = false;

        if (strictMode) {
            boolean gotoPermissionSettings = false;
            if (deniedForeverPermissions != null && deniedForeverPermissions.length > 0) {
                gotoPermissionSettings = true;
            }

            showPermissionTipDialog(failedPermissions, gotoPermissionSettings);
        } else {
            if (listener != null) {
                listener.onAutoPermissionFailed(failedPermissions, deniedForeverPermissions);
            }
        }


    }

    @Override
    public void onPermissionCanceled() {

    }

    public boolean isAutoPermission() {
        return autoPermission;
    }

    public boolean isAutoProtocol() {

        return !TextUtils.isEmpty(this.protocolUrl);
    }

    public void setAutoPermission(boolean autoPermission) {
        this.autoPermission = autoPermission;
    }

    public boolean isAlreadyDone() {
        return alreadyDone;
    }

    public void setAlreadyDone(boolean alreadyDone) {
        this.alreadyDone = alreadyDone;
    }


    public boolean isDirectPermission() {
        return directPermission;
    }

    public void setDirectPermission(boolean directPermission) {
        this.directPermission = directPermission;
    }

    public boolean isJumpingPermission() {
        return jumpingPermission;
    }

    public boolean isJumpingWriteSettings() {
        return jumpingWriteSettings;
    }
}
