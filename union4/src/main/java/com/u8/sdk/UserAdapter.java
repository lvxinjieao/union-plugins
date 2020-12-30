package com.u8.sdk;


public abstract class UserAdapter implements IUser {

    @Override
    public void login() {
    }

    @Override
    public void login(String customData) {
    }

    @Override
    public void switchLogin() {
    }


    @Override
    public void logout() {
    }

    @Override
    public void submitExtraData(UserExtraData extraData) {
    }

    @Override
    public void exit() {

    }

    @Override
    public void realNameRegister() {
    }

    @Override
    public void queryAntiAddiction() {
    }

    @Override
    public abstract boolean isSupportMethod(String methodName);

}
