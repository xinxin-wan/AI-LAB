package com.example.myassistantdemo.asr;

public class AsrJniStub {

    static {
        System.loadLibrary("wenet");
    }

    public static native void init(String modelDir);
    public static native void reset();
    public static native void acceptWaveform(short[] waveform);
    public static native void setInputFinished();
    public static native boolean Decode();
    public static native String getResult();
}
