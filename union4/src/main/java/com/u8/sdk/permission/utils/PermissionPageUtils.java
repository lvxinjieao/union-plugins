package com.u8.sdk.permission.utils;

import android.app.Activity;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.u8.sdk.utils.Logs;

//权限页适配
//参考这篇：http://blog.csdn.net/donkor_/article/details/79374442

public class PermissionPageUtils {

    //跳转到应用详情界面
    public static void jumpPermissionPageDefault(Application application) {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.parse("package:" + application.getPackageName()));
        application.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    //跳转到开启 设置权限界面
    public static void jumpWriteSettingPage(Application application) {
        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + application.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        application.startActivity(intent);
    }

    public static void jumpPermissionPage(Activity context) {

        String name = Build.MANUFACTURER;
        Logs.d("U8SDK", "jumpPermissionPage --- name : " + name);
        switch (name) {
            case "HUAWEI":
                goHuaWeiMainager(context);
                break;
            case "vivo":
                goVivoMainager(context);
                break;
            case "OPPO":
                goOppoMainager(context);
                break;
            case "Coolpad":
                goCoolpadMainager(context);
                break;
            case "Meizu":
                goMeizuMainager(context);
                break;
            case "Xiaomi":
                goXiaoMiMainager(context);
                break;
            case "samsung":
                goSangXinMainager(context);
                break;
            case "Sony":
                goSonyMainager(context);
                break;
            case "LG":
                goLGMainager(context);
                break;
            default:
                goIntentSetting(context);
                break;
        }
    }

    private static void goLGMainager(Activity mContext) {
        try {
            Intent intent = new Intent(mContext.getPackageName());
            ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
            intent.setComponent(comp);
            mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            goIntentSetting(mContext);
        }
    }

    private static void goSonyMainager(Activity mContext) {
        try {
            Intent intent = new Intent(mContext.getPackageName());
            ComponentName comp = new ComponentName("com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity");
            intent.setComponent(comp);
            mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            goIntentSetting(mContext);
        }
    }

    private static void goHuaWeiMainager(Activity mContext) {
        try {
            Intent intent = new Intent(mContext.getPackageName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
            intent.setComponent(comp);
            mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            goIntentSetting(mContext);
        }
    }

    private static String getMiuiVersion() {
        String propName = "ro.miui.ui.version.name";
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(
                    new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return line;
    }

    private static void goXiaoMiMainager(Activity mContext) {
        String rom = getMiuiVersion();
        Logs.d("U8SDK", "goMiaoMiMainager --- rom : " + rom);
        Intent intent = new Intent();
        if ("V6".equals(rom) || "V7".equals(rom)) {
            try {
                intent.setAction("miui.intent.action.APP_PERM_EDITOR");
                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                intent.putExtra("extra_pkgname", mContext.getPackageName());
                mContext.startActivity(intent);
            } catch (Exception e) {
                goIntentSetting(mContext);
            }
            return;
        }

        if ("V8".equals(rom) || "V9".equals(rom)) {
            try {
                intent.setAction("miui.intent.action.APP_PERM_EDITOR");
                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                intent.putExtra("extra_pkgname", mContext.getPackageName());
                mContext.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                goIntentSetting(mContext);
            }
            return;

        }

        goIntentSetting(mContext);
    }

    private static void goMeizuMainager(Activity context) {
        try {
            Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("packageName", context.getPackageName());
            context.startActivity(intent);
        } catch (ActivityNotFoundException localActivityNotFoundException) {
            localActivityNotFoundException.printStackTrace();
            goIntentSetting(context);
        }
    }

    private static void goSangXinMainager(Activity context) {
        //三星4.3可以直接跳转
        goIntentSetting(context);
    }

    private static void goIntentSetting(Activity context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void goOppoMainager(Activity context) {
        doStartApplicationWithPackageName(context, "com.coloros.safecenter");
    }

    /**
     * doStartApplicationWithPackageName("com.yulong.android.security:remote")
     * 和Intent open = getPackageManager().getLaunchIntentForPackage("com.yulong.android.security:remote");
     * startActivity(open);
     * 本质上没有什么区别，通过Intent open...打开比调用doStartApplicationWithPackageName方法更快，也是android本身提供的方法
     */
    private static void goCoolpadMainager(Activity context) {
        doStartApplicationWithPackageName(context, "com.yulong.android.security:remote");
      /*  Intent openQQ = getPackageManager().getLaunchIntentForPackage("com.yulong.android.security:remote");
        startActivity(openQQ);*/
    }

    private static void goVivoMainager(Activity context) {
        doStartApplicationWithPackageName(context, "com.bairenkeji.icaller");
     /*   Intent openQQ = getPackageManager().getLaunchIntentForPackage("com.vivo.securedaemonservice");
        startActivity(openQQ);*/
    }

    /**
     * 此方法在手机各个机型设置中已经失效
     *
     * @return
     */
    private static Intent getAppDetailSettingIntent(Activity mContext) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", mContext.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", mContext.getPackageName());
        }
        return localIntent;
    }

    private static void doStartApplicationWithPackageName(Activity mContext, String packagename) {
        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = mContext.getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return;
        }
        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);
        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = mContext.getPackageManager()
                .queryIntentActivities(resolveIntent, 0);
        Logs.e("PermissionPageManager", "resolveinfoList" + resolveinfoList.size());
        for (int i = 0; i < resolveinfoList.size(); i++) {
            Logs.e("PermissionPageManager", resolveinfoList.get(i).activityInfo.packageName + resolveinfoList.get(i).activityInfo.name);
        }
        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packageName参数2 = 参数 packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packageName参数2.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            // 设置ComponentName参数1:packageName参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            try {
                mContext.startActivity(intent);
            } catch (Exception e) {
                goIntentSetting(mContext);
                e.printStackTrace();
            }
        }
    }

}
