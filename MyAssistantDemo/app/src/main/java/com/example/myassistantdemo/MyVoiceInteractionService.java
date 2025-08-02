package com.example.myassistantdemo;

import android.os.Bundle;
import android.service.voice.VoiceInteractionService;
import android.service.voice.VoiceInteractionSession;

// 通过 VoiceInteractionService 提供语音助手入口, 语音助手的主服务入口。
public class MyVoiceInteractionService extends VoiceInteractionService {
    @Override
    public void onReady() {
        super.onReady();
    }

    @Override
    public void onLaunchVoiceAssistFromKeyguard() {
        // 处理系统请求启动助手时的回调
        super.onLaunchVoiceAssistFromKeyguard();
        // 打开会话界面
        showSession(new Bundle(), VoiceInteractionSession.SHOW_SOURCE_ASSIST_GESTURE);
    }
}

