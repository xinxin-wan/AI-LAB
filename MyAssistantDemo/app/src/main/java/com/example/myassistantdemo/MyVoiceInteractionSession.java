package com.example.myassistantdemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.service.voice.VoiceInteractionSession;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

/**
 * VoiceInteractionSession 表示一次 具体的语音交互会话。
 * 就像一个对话的上下文，管理 UI（例如显示对话气泡、候选结果）、交互状态。
 * 在这个会话中可以接收用户语音、展示结果、请求用户确认等。
 * 使用方式：
 * 通常由 VoiceInteractionSessionService 来创建和管理。
 * 你可以在里面定义 UI（通过继承它并覆盖 onCreateContentView），显示语音识别结果、对话内容。
 * 会话生命周期类似 Activity，但和系统语音交互绑定更紧密。
 */
public class MyVoiceInteractionSession extends VoiceInteractionSession {
    private TextView textView;
    private LinearLayout messagesLayout;
    private SpeechRecognizer speechRecognizer;
    // 10秒超时
    private static final int TIMEOUT_MS = 10_000;
    private Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable = this::endSession;

    public MyVoiceInteractionSession(Context context) {
        super(context);
    }

    @Override
    public View onCreateContentView() {
        // 外层父布局（全屏，居中对话框）
        FrameLayout root = new FrameLayout(getContext());

        // 气泡容器
        LinearLayout bubbleLayout = new LinearLayout(getContext());
        bubbleLayout.setOrientation(LinearLayout.VERTICAL);
        bubbleLayout.setGravity(Gravity.CENTER);
        int padding = dpToPx(20);
        bubbleLayout.setPadding(padding, padding, padding, padding);

        // 背景气泡样式：白色、圆角、阴影
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(dpToPx(24));
        bubbleLayout.setBackground(background);
        bubbleLayout.setElevation(dpToPx(8)); // 阴影效果

        // 显示识别结果的文字
        textView = new TextView(getContext());
        textView.setText("有什么可以帮助你的吗？");
        textView.setTextSize(18);
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);

        bubbleLayout.addView(textView);

        // 把气泡放到屏幕中间
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;

        root.addView(bubbleLayout, params);

        startListening();

        return root;
    }

    private void startListening() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getContext().getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    textView.setText("用户: " + matches.get(0));
                    resetTimeout();
                }
                restartListening();
            }


            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches =
                        partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    textView.setText("识别中: " + matches.get(0));
                    resetTimeout();
                }
            }

            @Override
            public void onError(int error) {
                textView.setText("错误: " + error + " - " + getErrorText(error));
                restartListening();
            }

            @Override public void onReadyForSpeech(Bundle params) {resetTimeout();}
            @Override public void onBeginningOfSpeech() { resetTimeout(); }
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        speechRecognizer.startListening(intent);
        resetTimeout();
    }

    private void restartListening() {
        new Handler(Looper.getMainLooper()).postDelayed(this::startListening, 1000);
    }

    private void resetTimeout() {
        timeoutHandler.removeCallbacks(timeoutRunnable);
        timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT_MS);
    }

    private void endSession() {
        textView.setText("超时，已结束对话");
        timeoutHandler.removeCallbacks(timeoutRunnable);
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 1000);
    }

    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private String getErrorText(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO: return "音频采集错误";
            case SpeechRecognizer.ERROR_CLIENT: return "客户端错误";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "权限不足";
            case SpeechRecognizer.ERROR_NETWORK: return "网络错误";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "网络超时";
            case SpeechRecognizer.ERROR_NO_MATCH: return "没有匹配结果";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "识别服务忙";
            case SpeechRecognizer.ERROR_SERVER: return "服务器错误";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "没有检测到语音输入";
            default: return "未知错误";
        }
    }
}
