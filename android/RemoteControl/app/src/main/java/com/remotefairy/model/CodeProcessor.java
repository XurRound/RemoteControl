package com.remotefairy.model;

public class CodeProcessor
{
    public static native String process(String enc, String key);

    static
    {
        System.loadLibrary("ndk1");
    }
}