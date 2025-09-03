package com.example.myassistantdemo;

import android.os.Bundle;
import android.service.voice.VoiceInteractionService;
import android.service.voice.VoiceInteractionSession;

// 通过 VoiceInteractionService 提供语音助手入口, 语音助手的主服务入口。

/**
 * VoiceInteractionService 是 Android 定义的一个 语音交互服务基类，它可以和系统进行更深度的集成（比如作为系统助手，响应 Home 长按、"Hey Google" 之类的唤醒）。
 * 它的目标是做 完整的语音交互体验，不仅仅是转文字，而是包括理解、对话、执行指令。
 * 使用方式：
 * 继承 VoiceInteractionService 并在 manifest 中声明（带 <voice-interaction-service>）。
 * 需要实现自己的 VoiceInteractionSessionService 来管理会话。
 * 系统只允许一个默认的 VoiceInteractionService，用户需要在系统设置中选择。
 * 典型的实现是 Google Assistant。
 */
public class MyVoiceInteractionService extends VoiceInteractionService {
    // entrance, no need to implement
//    @Override
//    public void onReady() {
//        super.onReady();
//    }
//
//    @Override
//    public void onLaunchVoiceAssistFromKeyguard() {
//        // 处理系统请求启动助手时的回调
//        super.onLaunchVoiceAssistFromKeyguard();
//        // 打开会话界面
//        showSession(new Bundle(), VoiceInteractionSession.SHOW_SOURCE_ASSIST_GESTURE);
//    }
}

