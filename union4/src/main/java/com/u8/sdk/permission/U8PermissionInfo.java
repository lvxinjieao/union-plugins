package com.u8.sdk.permission;

public class U8PermissionInfo {

    public static final int STATE_PERMITTED = 1;        //已获得授权
    public static final int STATE_DENIED = 2;           //被拒绝

    private String group;
    private String cname;
    private String name;
    private boolean state;

    public U8PermissionInfo() {

    }

    public U8PermissionInfo(String name, String cname, String group) {
        this.name = name;
        this.cname = cname;
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
