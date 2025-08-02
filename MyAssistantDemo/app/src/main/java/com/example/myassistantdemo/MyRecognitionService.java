package com.example.myassistantdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;

import java.util.ArrayList;

// 通过 RecognitionService 模拟语音识别（返回固定字符串 "打开音乐"）
public class MyRecognitionService extends RecognitionService {
    @Override
    protected void onStartListening(Intent intent, Callback listener) {
        ArrayList<String> resultsList = new ArrayList<>();
        resultsList.add("打开音乐");
        Bundle results = new Bundle();
        results.putStringArrayList(RecognizerIntent.EXTRA_RESULTS, resultsList);
        try {
            listener.results(results);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCancel(Callback listener) {
    }

    @Override
    protected void onStopListening(Callback listener) {
    }
}
