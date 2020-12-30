package com.u8.sdk.utils;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;


public class EncryptUtils {

    public static String md5Sign(Map<String, String> params, String key) {
        String data = generateUrlSortedParamString(params, "&", true);
        data = data + "&key=" + key;
        Log.d("U8SDK", "sign str:" + data);
        String sign = EncryptUtils.md5(data).toLowerCase();
        Log.d("U8SDK", "sign:" + sign);
        return sign;

    }

    public static String md5(String txt) {
        return encrypt(txt, "md5");
    }

    public static String sha(String txt) {
        return encrypt(txt, "sha");
    }

    private static String encrypt(String txt, String algorithName) {

        if (txt == null || txt.trim().length() == 0) {
            return null;
        }

        if (algorithName == null || algorithName.trim().length() == 0) {
            algorithName = "MD5";
        }

        String result = null;

        try {
            MessageDigest m = MessageDigest.getInstance(algorithName);
            m.reset();
            m.update(txt.getBytes("UTF-8"));
            byte[] bts = m.digest();

            result = hex(bts);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;
    }

    private static String hex(byte[] bts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bts.length; i++) {
            sb.append(Integer.toHexString((bts[i] & 0xFF) | 0x100).substring(1, 3));
        }

        return sb.toString();
    }

    /**
     * 按照URL参数格式，进行参数字符串拼接
     *
     * @param params
     * @param splitChar
     * @return
     */
    public static String generateUrlSortedParamString(Map<String, String> params, String splitChar, boolean nullExcluded) {

        StringBuffer content = new StringBuffer();
        // 按照key做排序
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key) == null ? "" : params.get(key);
            if (nullExcluded && value.length() == 0) {
                continue;
            }
            content.append(key + "=" + value);
            if (!TextUtils.isEmpty(splitChar)) {
                content.append(splitChar);
            }
        }
        if (content.length() > 0 && !TextUtils.isEmpty(splitChar)) {
            content.deleteCharAt(content.length() - 1);
        }

        return content.toString();
    }

}
