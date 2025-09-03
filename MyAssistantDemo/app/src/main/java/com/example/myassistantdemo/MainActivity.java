package com.example.myassistantdemo;

import android.Manifest;
import android.app.ActivityManager;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        // 通过 RoleManager 注册为系统默认助手
        RoleManager roleManager = getSystemService(RoleManager.class);
        if (roleManager.isRoleAvailable(RoleManager.ROLE_ASSISTANT)) {
            Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_ASSISTANT);
            startActivityForResult(intent, 0);
        }

        Button startBtn = findViewById(R.id.start_button);
        startBtn.setOnClickListener(v -> {
            // 手动启动语音助手 Session
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (isLocalVoiceInteractionSupported()) {
                    startLocalVoiceInteraction(null);
                } else {
//                    Toast.makeText(this, "设备不支持本地 VoiceInteraction", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, DialogActivity.class);
                    startActivity(intent);
                }
            } else {
                Toast.makeText(this, "系统版本过低，不支持 VoiceInteraction", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLocalVoiceInteractionStarted() {
        super.onLocalVoiceInteractionStarted();
        Toast.makeText(this, "语音对话已启动", Toast.LENGTH_SHORT).show();
    }

    // Session 回调更新结果
    @Override
    public void onLocalVoiceInteractionStopped() {
        super.onLocalVoiceInteractionStopped();
        Toast.makeText(this, "语音对话已结束", Toast.LENGTH_SHORT).show();
    }


}