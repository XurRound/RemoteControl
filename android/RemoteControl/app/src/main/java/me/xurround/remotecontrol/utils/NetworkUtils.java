package me.xurround.remotecontrol.utils;

public class NetworkUtils
{
    public static void performGetRequest(String url, String requestData, RequestResultListener listener)
    {
        new NetTask(listener).execute(url + "?" + requestData, "GET");
    }

    public static void performPostRequest(String url, String requestData, RequestResultListener listener)
    {
        new NetTask(listener).execute(url + "?" + requestData, "POST");
    }
}