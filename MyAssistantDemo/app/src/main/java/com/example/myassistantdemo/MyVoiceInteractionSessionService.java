package com.example.myassistantdemo;

import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.service.voice.VoiceInteractionSessionService;

/**
 * VoiceInteractionSessionService 是一个 负责创建和管理 VoiceInteractionSession 的服务。
 * 它本身不直接处理交互，而是负责提供 VoiceInteractionSession 实例。
 * 可以把它理解为「语音会话的工厂和管理器」。
 * 使用方式：
 * 继承 VoiceInteractionSessionService，实现 onNewSession，返回自定义的 VoiceInteractionSession。
 * 和 VoiceInteractionService 配合使用，系统在启动语音助手时，会通过这个服务创建会话。
 */
public class MyVoiceInteractionSessionService extends VoiceInteractionSessionService {
    // 用于创建语音助手的会话（Session）.返回自定义的 MyVoiceInteractionSession 对象
    @Override
    public VoiceInteractionSession onNewSession(Bundle args) {
        return new MyVoiceInteractionSession(this);
    }
}
