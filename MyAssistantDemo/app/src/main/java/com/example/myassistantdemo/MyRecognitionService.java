package com.example.myassistantdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;

import java.util.ArrayList;

/**
 * 作用：
 * RecognitionService 是一个 语音识别服务的基类。它定义了语音识别ASR的接口，供系统或应用调用。它是实现「听音转文字」的服务提供者。
 * 使用方式：
 * 需要继承 RecognitionService 并实现 onStartListening、onStopListening 等方法。
 * 系统的语音识别框架（如 Google 语音识别）就是通过这个服务来接收音频、处理、返回识别结果。
 * 开发者一般 不需要自己实现 RecognitionService，除非你要做一个自定义的语音识别引擎。
 * 客户端（普通应用）通常通过 SpeechRecognizer API 来调用这个服务，而不是直接绑定。
 */
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
