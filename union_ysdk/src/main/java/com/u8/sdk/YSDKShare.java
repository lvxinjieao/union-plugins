package com.u8.sdk;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.tencent.ysdk.module.share.ShareApi;
import com.tencent.ysdk.module.share.ShareCallBack;
import com.tencent.ysdk.module.share.impl.ShareRet;

public class YSDKShare implements IShare{

	private Bitmap cacheBitmap;
	
	public YSDKShare(Activity context){ }
	
	@Override
	public boolean isSupportMethod(String methodName) {
		return true;
	}

	@Override
	public void share(ShareParams params) {
		ShareApi.getInstance().share(capForBitmap(), params.getTitle(), params.getContent(), "");
	}
	
	private Bitmap capForBitmap(){
		if (cacheBitmap==null){
			View view = U8SDK.getInstance().getContext().getWindow().getDecorView();
			view.setDrawingCacheEnabled(true);
			view.buildDrawingCache();
			Rect rect = new Rect();
			view.getWindowVisibleDisplayFrame(rect);
			int statusBarHeight = rect.top;
			WindowManager windowManager = U8SDK.getInstance().getContext().getWindowManager();
			DisplayMetrics outMetrics = new DisplayMetrics();
			windowManager.getDefaultDisplay().getMetrics(outMetrics);
			int width = outMetrics.widthPixels;
			int height = outMetrics.heightPixels;
			cacheBitmap= Bitmap.createBitmap(view.getDrawingCache(), 0, statusBarHeight, width, height - statusBarHeight);
			view.destroyDrawingCache();
			view.setDrawingCacheEnabled(false);
		}
		return cacheBitmap;
	}

}
