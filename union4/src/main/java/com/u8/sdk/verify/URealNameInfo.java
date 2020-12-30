package com.u8.sdk.verify;

public class URealNameInfo {

    public static final int TYPE_QUERY = 1;         //调用查询接口返回的
    public static final int TYPE_UI = 2;            //调用实名认证UI接口返回的

    private int resultType = 1;
    private boolean isRealname; //是否已经实名认证过
    private int age;     //年龄

    public URealNameInfo(int type, boolean isRealname, int age) {
        this.resultType = type;
        this.isRealname = isRealname;
        this.age = age;
    }

    public boolean isRealname() {
        return isRealname;
    }

    public void setRealname(boolean realname) {
        isRealname = realname;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getResultType() {
        return resultType;
    }

    public boolean isTypeQuery() {
        return resultType == TYPE_QUERY;
    }

    public boolean isTypeUI() {
        return resultType == TYPE_UI;
    }

    public void setResultType(int resultType) {
        this.resultType = resultType;
    }
}
