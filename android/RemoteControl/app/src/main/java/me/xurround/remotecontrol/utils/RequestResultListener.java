package me.xurround.remotecontrol.utils;

public interface RequestResultListener
{
    void onSuccess(String response);
    void onFail();
}