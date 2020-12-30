package com.u8.sdk.ysdk.permission.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.u8.sdk.ysdk.permission.PermissionFail;
import com.u8.sdk.ysdk.permission.PermissionSuccess;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;


public class PermissionUtils {

	  public static boolean verifyGrants(int... grantResults) {
		    for (int result : grantResults) {
		      if (result != PackageManager.PERMISSION_GRANTED) {
		        return false;
		      }
		    }
		    return true;
	  }
	
	  public static boolean isOverMarshmallow() {
		    return Build.VERSION.SDK_INT >= 23;
		  }

		  @TargetApi(value = 23)
		  public static List<String> findDeniedPermissions(Activity activity, String... permission){
		    List<String> denyPermissions = new ArrayList<String>();
		    for(String value : permission){
		      if(activity.checkSelfPermission(value) != PackageManager.PERMISSION_GRANTED){
		        denyPermissions.add(value);
		      }else{
		    	  //适配特殊机型，比如小米
		    	  AppOpsManager appOpsManager = (AppOpsManager) activity.getSystemService(Context.APP_OPS_SERVICE);
                  int checkOp = appOpsManager.checkOp(AppOpsManager.OPSTR_READ_PHONE_STATE, Process.myUid(), activity.getPackageName());
                  if (checkOp != AppOpsManager.MODE_ALLOWED) {
                	  denyPermissions.add(value);
                  }
		      }
		    }
		    return denyPermissions;
		  }

		  public static List<Method> findAnnotationMethods(Class clazz, Class<? extends Annotation> clazz1){
		    List<Method> methods = new ArrayList<Method>();
		    for(Method method : clazz.getDeclaredMethods()){
		      if(method.isAnnotationPresent(clazz1)){
		        methods.add(method);
		      }
		    }
		    return methods;
		  }

		  public static <A extends Annotation> Method findMethodPermissionFailWithRequestCode(Class clazz,
		      Class<A> permissionFailClass, int requestCode) {
		    for(Method method : clazz.getDeclaredMethods()){
		      if(method.isAnnotationPresent(permissionFailClass)){
		        if(requestCode == method.getAnnotation(PermissionFail.class).requestCode()){
		          return method;
		        }
		      }
		    }
		    return null;
		  }

		  public static boolean isEqualRequestCodeFromAnntation(Method m, Class clazz, int requestCode){
		    if(clazz.equals(PermissionFail.class)){
		      return requestCode == m.getAnnotation(PermissionFail.class).requestCode();
		    } else if(clazz.equals(PermissionSuccess.class)){
		      return requestCode == m.getAnnotation(PermissionSuccess.class).requestCode();
		    } else {
		      return false;
		    }
		  }

		  public static <A extends Annotation> Method findMethodWithRequestCode(Class clazz,
		      Class<A> annotation, int requestCode) {
		    for(Method method : clazz.getDeclaredMethods()){
		      if(method.isAnnotationPresent(annotation)){
		        if(isEqualRequestCodeFromAnntation(method, annotation, requestCode)){
		          return method;
		        }
		      }
		    }
		    return null;
		  }

		  public static <A extends Annotation> Method findMethodPermissionSuccessWithRequestCode(Class clazz,
		      Class<A> permissionFailClass, int requestCode) {
		    for(Method method : clazz.getDeclaredMethods()){
		      if(method.isAnnotationPresent(permissionFailClass)){
		        if(requestCode == method.getAnnotation(PermissionSuccess.class).requestCode()){
		          return method;
		        }
		      }
		    }
		    return null;
		  }

		  public static Activity getActivity(Object object){
		    if(object instanceof Fragment){
		      return ((Fragment)object).getActivity();
		    } else if(object instanceof Activity){
		      return (Activity) object;
		    }
		    return null;
		  }
		  

	
}
