package com.example.myassistantdemo;

import android.app.role.RoleManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("My Assistant App");
        setContentView(tv);

        // 通过 RoleManager 注册为系统默认助手
        RoleManager roleManager = getSystemService(RoleManager.class);
        if (roleManager.isRoleAvailable(RoleManager.ROLE_ASSISTANT)) {
            Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_ASSISTANT);
            startActivityForResult(intent, 0);
        }
    }
}