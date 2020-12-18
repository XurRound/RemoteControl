package me.xurround.remotecontrol.utils;

import java.net.URLEncoder;
import java.util.ArrayList;

public class RequestBuilder
{
    public static String EMPTY_REQUEST = "";

    private ArrayList<String> dataParts;

    public RequestBuilder()
    {
        dataParts = new ArrayList<>();
    }

    public void add(String key, String value)
    {
        try
        {
            dataParts.add(urlEncode(key) + "=" + urlEncode(value));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String build()
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < dataParts.size() - 1; i++)
            builder.append(dataParts.get(i)).append('&');
        builder.append(dataParts.get(dataParts.size() - 1));
        return builder.toString();
    }

    private String urlEncode(String str) throws Exception
    {
        return URLEncoder.encode(str, "UTF-8");
    }
}