package com.example.myassistantdemo;

import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.service.voice.VoiceInteractionSessionService;

// // 通过 VoiceInteractionSessionService + VoiceInteractionSession 显示语音助手 UI（Voice Plate）。
public class MyVoiceInteractionSessionService extends VoiceInteractionSessionService {
    // 用于创建语音助手的会话（Session）.返回自定义的 MyVoiceInteractionSession 对象
    @Override
    public VoiceInteractionSession onNewSession(Bundle args) {
        return new MyVoiceInteractionSession(this);
    }
}
