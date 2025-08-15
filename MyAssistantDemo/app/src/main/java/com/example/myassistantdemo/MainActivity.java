package com.example.myassistantdemo;

import android.app.role.RoleManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 通过 RoleManager 注册为系统默认助手
        RoleManager roleManager = getSystemService(RoleManager.class);
        if (roleManager.isRoleAvailable(RoleManager.ROLE_ASSISTANT)) {
            Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_ASSISTANT);
            startActivityForResult(intent, 0);
        }
        Button startBtn = findViewById(R.id.start_button);
        startBtn.setOnClickListener(v -> {
            // 手动启动语音助手 Session
            startActivity(new Intent(Intent.ACTION_ASSIST));
        });
    }

    private void requestAssistantRole() {
        RoleManager roleManager = getSystemService(RoleManager.class);
        if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_ASSISTANT)) {
            Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_ASSISTANT);
            startActivityForResult(intent, 0);
        }
    }

    // Session 回调更新结果
    public void showResult(String result) {
        resultText.setText(result);
    }


}