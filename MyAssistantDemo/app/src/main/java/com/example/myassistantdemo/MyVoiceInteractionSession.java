package com.example.myassistantdemo;

import android.content.Context;
import android.service.voice.VoiceInteractionSession;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

// 提供语音识别服务（系统通过这个服务获取语音识别结果）
public class MyVoiceInteractionSession extends VoiceInteractionSession {
    public MyVoiceInteractionSession(Context context) {
        super(context);
    }

    // 收到 Assist 请求时显示 Toast（模拟指令处理)
    @Override
    public void onHandleAssist(AssistState state) {
        super.onHandleAssist(state);
        Toast.makeText(getContext(), "识别到命令: 打开音乐", Toast.LENGTH_SHORT).show();
    }

    // 加载简单的浮层 UI (voice_plate.xml)
    @Override
    public View onCreateContentView() {
        return LayoutInflater.from(getContext()).inflate(R.layout.voice_plate, null);
    }
}
