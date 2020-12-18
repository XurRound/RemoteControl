package me.xurround.remotecontrol.utils;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetTask extends AsyncTask<String, Void, Void>
{
    private RequestResultListener requestResultListener;

    public NetTask(RequestResultListener requestResultListener)
    {
        this.requestResultListener = requestResultListener;
    }

    @Override
    protected Void doInBackground(String... params)
    {
        try
        {
            URL url = new URL(params[0]);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod(params[1]);
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String strData = null;
            while ((strData = reader.readLine()) != null)
                builder.append(strData + "\n");
            reader.close();
            requestResultListener.onSuccess(builder.toString());
            return null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        requestResultListener.onFail();
        return null;
    }
}