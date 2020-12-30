package com.u8.sdk.impl.data;

import org.json.JSONException;
import org.json.JSONObject;

class JSON_KEY {
	static final String CUR_SELECTED = "currSelected";
	static final String ID = "id";
	static final String TOKEN = "token";
	static final String TYPE_NAME = "typeName";
	static final String USER_NAME = "username";
	static final String COIN_NUM = "coinNum";
}

public class SimpleUser {

	/**
	 * 账号id
	 */
	private String id;

	/**
	 * 是否为最后一次使用或者当前选择的
	 */
	private boolean isCurrSelected;

	/**
	 * 玩家上次登陆成功的token
	 */
	private String token;

	/**
	 * 帐户类型名称 "1"和"2"显示在最近登陆列表
	 */
	private String typeName;

	/**
	 * 用户名
	 */
	private String username;
	

	public SimpleUser() {
	}

	public SimpleUser(String id, String typeName, String username, String token, boolean isUsed) {
		this.id = id;
		this.typeName = typeName;
		this.username = username;
		this.token = token;
		this.isCurrSelected = isUsed;
	}

	public void fromJSON(JSONObject obj) throws JSONException {
		this.id = obj.getString(JSON_KEY.ID);
		this.typeName = obj.getString(JSON_KEY.TYPE_NAME);
		this.username = obj.getString(JSON_KEY.USER_NAME);
		this.token = obj.getString(JSON_KEY.TOKEN);
		this.isCurrSelected = obj.getBoolean(JSON_KEY.CUR_SELECTED);
	}


	public String getId() {
		return id;
	}

	public String getToken() {
		return token;
	}

	public String getTypeName() {
		return typeName;
	}

	public String getUsername() {
		return username;
	}

	public boolean isCurrSelected() {
		return isCurrSelected;
	}


	public void setCurrSelected(boolean isCurrSelected) {
		this.isCurrSelected = isCurrSelected;
	}

	public void setToken(String password) {
		this.token = password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put(JSON_KEY.ID, id);
		obj.put(JSON_KEY.TYPE_NAME, typeName);
		obj.put(JSON_KEY.USER_NAME, username);
		obj.put(JSON_KEY.TOKEN, token);
		obj.put(JSON_KEY.CUR_SELECTED, isCurrSelected);
		return obj;
	}

}
