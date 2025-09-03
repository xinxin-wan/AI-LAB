package com.example.myassistantdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import java.util.ArrayList;
import java.util.Locale;

public class DialogActivity extends Activity {

    private TextView textView;
    private SpeechRecognizer speechRecognizer;
    private static final int TIMEOUT_MS = 10000;
    private Handler timeoutHandler = new Handler();
    private Runnable timeoutRunnable = this::endSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 外层布局（全屏）
        FrameLayout root = new FrameLayout(this);

        // 气泡容器
        LinearLayout bubble = new LinearLayout(this);
        bubble.setOrientation(LinearLayout.VERTICAL);
        bubble.setGravity(Gravity.CENTER);
        int padding = dpToPx(20);
        bubble.setPadding(padding, padding, padding, padding);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dpToPx(24));
        bubble.setBackground(bg);
        bubble.setElevation(dpToPx(8));

        // 显示文字
        textView = new TextView(this);
        textView.setText("有什么可以帮助你的吗？");
        textView.setTextSize(18);
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);

        bubble.addView(textView);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;

        root.addView(bubble, params);

        setContentView(root);

        startListening();
    }

    private void startListening() {
        // speechRecognizer is not available in our platform
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    textView.setText(matches.get(0));
                    resetTimeout();
                }
                restartListening();
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    textView.setText(matches.get(0));
                    resetTimeout();
                }
            }

            @Override
            public void onError(int error) {
                textView.setText("错误: " + error);
                restartListening();
            }

            @Override public void onReadyForSpeech(Bundle params) { resetTimeout(); }
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
        new Handler().postDelayed(this::startListening, 1000);
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
        new Handler().postDelayed(this::finish, 1000);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
