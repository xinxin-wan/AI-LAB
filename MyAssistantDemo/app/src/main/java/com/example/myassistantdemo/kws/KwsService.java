package com.example.myassistantdemo.kws;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class KwsService {
    private static final String LOG_TAG = "KwsService";

    private final Context mContext;
    private double mKwsThreshold = 0.09;  // 唤醒的阈值
    private long mKwsTimeSlot = 1000; // 连续两次唤醒的最小时间间隔，单位ms

    private static final List<String> resource = Arrays.asList("kws.ort");
    private AtomicBoolean keepRunning = new AtomicBoolean(false);
    private boolean autoStop = true;  // 唤醒后自动停止
    private final WakeUpCallback mWakeUpCallbackHandle;
    private RecordThread mRecorder;
    private DecodeThread mDecoder;
    private AudioManager mAudioManager;

    /**
     * 唤醒回调，使用唤醒的现场应该实现WakeUpCallback接口并且实现其中的方法
     */
    public interface WakeUpCallback {
        /**
         * 达到唤醒阈值时，触发该方法
         *
         * @param keywords 唤醒关键词
         */
        void onWakeUp(String keywords);

        /**
         * 实时更新当前的唤醒概率和唤醒阈值
         *
         * @param value     当前的唤醒概率
         * @param threshold 唤醒阈值
         */
        void onRealTimeProb(double value, double threshold);
    }

    /**
     * 初始化
     *
     * @param context              android context
     * @param kwsThreshold         kws threshold, prob> threshold will callback WakeUpCallback::onWakeUp, default is 0.09
     * @param kwsTimeSlot          min time (ms) slot between two wake-up events, default is 1000
     * @param wakeUpCallbackHandle WakeUpCallback
     * @throws IOException you need catch this!
     */
    public KwsService(Context context,
                      double kwsThreshold,
                      long kwsTimeSlot,
                      WakeUpCallback wakeUpCallbackHandle) throws IOException {
        mContext = context;
        mKwsThreshold = kwsThreshold;
        mKwsTimeSlot = kwsTimeSlot;
        mWakeUpCallbackHandle = wakeUpCallbackHandle;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        assetsInit();
        KwsJniStub.init(context.getFilesDir().getPath());

        Log.i(LOG_TAG, "KwsService init finished!");
    }

    /**
     * assets 的 kws.ort 复制到本地目录
     */
    private void assetsInit() throws IOException {
        AssetManager assetMgr = mContext.getAssets();
        // Unzip all files in resource from assets to context.
        // Note: Uninstall the APP will remove the resource files in the context.
        for (String file : assetMgr.list("")) {
            if (resource.contains(file)) {
                File dst = new File(mContext.getFilesDir(), file);
                if (!dst.exists() || dst.length() == 0) {
                    Log.i(LOG_TAG, "Unzipping " + file + " to " + dst.getAbsolutePath());
                    InputStream is = assetMgr.open(file);
                    OutputStream os = new FileOutputStream(dst);
                    byte[] buffer = new byte[4 * 1024];
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                    }
                    os.flush();
                }
            }
        }
    }

    /**
     * 切换自动停止功能，默认为启用自动停止，即在发生唤醒之后自动停止唤醒模型的监听和推理
     * @param value
     */
    public void setAutoStop(boolean value){
        this.autoStop = value;
    }

    /**
     * 开始唤醒监听
     */
    public boolean start() {
        // 当前功能没有启动，但是音频资源被占用，说明别的功能正在占用，拒绝启动
        if (!keepRunning.get()) {
            if (!mAudioManager.getActiveRecordingConfigurations().isEmpty()) {
                Log.e(LOG_TAG, "Another app or thread are holding the audio service, refuse to start kws!");
                return false;
            } else {
                keepRunning.set(true);
                KwsJniStub.reset();
                mRecorder = new RecordThread();
                mDecoder = new DecodeThread(mKwsThreshold);
                mRecorder.start();
                mDecoder.start();
                Log.d(LOG_TAG, "start KWS");
                return true;
            }
        } else {
            Log.d(LOG_TAG, "KWS already stared!");
            return true;
        }
    }

    /**
     * 停止唤醒监听
     */
    public void stop() {
        if (keepRunning.get()) {
            keepRunning.set(false);
            KwsJniStub.setInputFinished();
            Log.d(LOG_TAG, "Stop KWS");
        } else {
            Log.d(LOG_TAG, "KWS already stopped!");
        }
    }


    /**
     * 录音线程
     */
    private class RecordThread extends Thread {
        private int miniBufferSize = 0;  // 1280 bytes 648 byte 40ms, 0.04s
        private AudioRecord record = null;
        private static final int SAMPLE_RATE = 16000;  // The sampling rate

        /**
         * init recorder
         */
        public RecordThread()  {

            // buffer size in bytes 1280
            miniBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (miniBufferSize == AudioRecord.ERROR || miniBufferSize == AudioRecord.ERROR_BAD_VALUE) {
                Log.e(LOG_TAG,"Audio buffer can't initialize!");
            }
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Log.e(LOG_TAG,"Audio Record can't get RECORD_AUDIO permission!");
            }
            record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    miniBufferSize);
            if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(LOG_TAG,"Audio Record can't initialize!");
            }
            Log.i(LOG_TAG, "Record init okay");
        }

        @Override
        public void run() {
            record.startRecording();
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
            while (keepRunning.get()) {
                short[] buffer = new short[miniBufferSize / 2];
                int read = record.read(buffer, 0, buffer.length);
                switch (read) {
                    case AudioRecord.ERROR_INVALID_OPERATION:
                        Log.e(LOG_TAG, "kws recorder get ERROR_INVALID_OPERATION!");
                    case AudioRecord.ERROR_BAD_VALUE:
                        Log.e(LOG_TAG, "kws recorder get ERROR_BAD_VALUE!");
                    case AudioRecord.ERROR_DEAD_OBJECT:
                        Log.e(LOG_TAG, "kws recorder get ERROR_DEAD_OBJECT!");
                    case AudioRecord.ERROR:
                        Log.e(LOG_TAG, "kws recorder get ERROR!");
                    default:
                        KwsJniStub.acceptWaveform(buffer);
                }
            }
            record.stop();
        }
    }

    /**
     * 解码线程
     */
    private class DecodeThread extends Thread {
        private final double kwsProbThreshold;
        private long lastTime = -mKwsTimeSlot;

        public DecodeThread(double threshold) {
            kwsProbThreshold = threshold;
        }

        @Override
        public void run() {
            while (keepRunning.get()) {
                float prob = KwsJniStub.Decode();
                Log.i(LOG_TAG, "kws.Decode() = " + prob);
                mWakeUpCallbackHandle.onRealTimeProb(prob, mKwsThreshold);
                long currentTime = System.currentTimeMillis();
                if (prob > kwsProbThreshold && currentTime - lastTime >= mKwsTimeSlot) {
                    // 达到阈值，且距离上次唤醒的时常大于 kwsTimeSlot
                    lastTime = currentTime;
                    Log.i(LOG_TAG, "kws.getResult:" + prob);
                    mWakeUpCallbackHandle.onWakeUp("你好问问！");
                    if(autoStop){
                        keepRunning.set(false);
                        KwsJniStub.setInputFinished();
                        Log.d(LOG_TAG, "Auto Stop KWS");
                    }
                }
            }
        }
    }

}
