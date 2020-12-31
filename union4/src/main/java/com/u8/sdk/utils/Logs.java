package com.u8.sdk.utils;

import android.util.Log;

public class Logs {

	public static boolean isDebug = true;
	private static final String TAG = "union";

	public static void i(String i) {
		if (isDebug) {
			Log.i(TAG, i);
		}
	}

	public static void d(String i) {
		if (isDebug) {
			Log.d(TAG, i);
		}
	}

	public static void w(String i) {
		if (isDebug) {
			Log.w(TAG, i);
		}
	}

	public static void e(String i) {
		if (isDebug) {
			Log.e(TAG, i);
		}
	}

	public static void v(String i) {
		if (isDebug) {
			Log.v(TAG, i);
		}
	}
	//=================================================================================
	//=================================================================================
	//=================================================================================
	//=================================================================================
	//=================================================================================
	//=================================================================================
	
	public static void i(String tag,String i) {
		if (isDebug) {
			Log.i(tag, i);
		}
	}

	public static void d(String tag,String i) {
		if (isDebug) {
			Log.d(tag, i);
		}
	}

	public static void w(String tag,String i) {
		if (isDebug) {
			Log.w(tag, i);
		}
	}

	public static void e(String tag,String i) {
		if (isDebug) {
			Log.e(tag, i);
		}
	}

	public static void v(String tag,String i) {
		if (isDebug) {
			Log.v(tag, i);
		}
	}
}
