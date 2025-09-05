package com.example.myassistantdemo.kws;

public class KwsJniStub {

    static {
        System.loadLibrary("wekws");
    }

    public static native void init(String modelDir);
    public static native void reset();
    public static native void acceptWaveform(short[] waveform);
    public static native void setInputFinished();
    public static native float Decode();
}
