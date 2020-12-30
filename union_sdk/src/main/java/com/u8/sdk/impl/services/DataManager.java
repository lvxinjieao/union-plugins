package com.u8.sdk.impl.services;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.u8.sdk.impl.common.Consts;
import com.u8.sdk.impl.data.SimpleUser;
import com.u8.sdk.utils.Base64;
import com.u8.sdk.utils.StoreUtils;


public class DataManager {

	private static DataManager instance;

	public static DataManager getInstance() {
		if (instance == null) {
			instance = new DataManager();
		}
		return instance;
	}

	private SimpleUser currLoginedUser;
	
	private long coinNum;		//当前用户拥有的平台币数量

	/**
	 * 添加一个已经登录的帐号
	 * 
	 * @param user
	 */
	private void addLoginedUser(Context context, SimpleUser user) {
		if (user == null)
			return;
		List<SimpleUser> users = getAllLoginedUsers(context);
		for (SimpleUser u : users) {
			if (u.getUsername().equals(user.getUsername())) {
				users.remove(u);
				break;
			}
		}
		users.add(0, user);
		saveLoginedUsers(context, users);
	}

	public void addLoginedUser(Context context, String id, String name, String username, String token, boolean isCurrent) {
		this.addLoginedUser(context, new SimpleUser(id, name, username, token, isCurrent));
	}

	JSONArray getJSONArray(Context context) throws JSONException, Exception {
		String userJsonStr = StoreUtils.getString(context, Consts.SKEY_USER);
		if (!TextUtils.isEmpty(userJsonStr)) {
			userJsonStr = Base64.decodeString(userJsonStr);
			JSONArray array = new JSONArray(userJsonStr);
			return array;
		} else {
			Log.e("U8SDK", "userJsonStr is empty.");
			return new JSONArray();
		}

	}


	/**
	 * 获取所有登录过的本地记录的帐号信息
	 * 
	 * @return
	 */
	private List<SimpleUser> getAllLoginedUsers(Context context) {
		List<SimpleUser> users = new ArrayList<SimpleUser>();
		try {
			JSONArray array = getJSONArray(context);
			for (int i = 0; i < array.length(); i++) {
				SimpleUser user = new SimpleUser();
				user.fromJSON(array.getJSONObject(i));
				users.add(user);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return users;
	}

	public SimpleUser getLastLoginedUser(Context context) {
		List<SimpleUser> users = getAllLoginedUsers(context);
		for (SimpleUser u : users) {
			if (u.isCurrSelected()) {
				return u;
			}
		}
		return null;
	}

	/**
	 * 删除一个已经登录的帐号
	 * 
	 * @param user
	 */
	public void removeLoginedUser(Context context, SimpleUser user) {
		if (user == null)
			return;
		List<SimpleUser> users = getAllLoginedUsers(context);
		for (SimpleUser u : users) {
			if (u.getUsername().equals(user.getUsername())) {
				users.remove(u);
				break;
			}
		}
		saveLoginedUsers(context, users);
	}

	/**
	 * 保存所有登录过的用户信息
	 * 
	 * @param users
	 */
	public void saveLoginedUsers(Context context, List<SimpleUser> users) {
		if (users == null || users.size() == 0) {
			return;
		}
		// 检查当前使用的帐号
		int selectedIndex = -1;
		for (int i = 0; i < users.size(); i++) {
			SimpleUser u = users.get(i);
			if (selectedIndex >= 0 && u.isCurrSelected()) {
				u.setCurrSelected(false);
				continue;
			}
			if (u.isCurrSelected()) {
				selectedIndex = i;
			}
		}
		try {
			JSONArray array = new JSONArray();
			for (SimpleUser user : users) {
				JSONObject obj = user.toJSON();
				array.put(obj);
			}
			StoreUtils.putString(context, Consts.SKEY_USER, Base64.encode(array.toString().getBytes()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setCurrLoginedUser(SimpleUser user) {
		this.currLoginedUser = user;
	}

	public void setCurrLoginedUser(String id, String name, String username, String token) {
		this.currLoginedUser = new SimpleUser(id, name, username, token, true);
	}

	public SimpleUser getCurrLoginedUser() {
		return this.currLoginedUser;
	}

	public long getCoinNum() {
		return coinNum;
	}

	public void setCoinNum(long coinNum) {
		this.coinNum = coinNum;
	}
	
	

}
