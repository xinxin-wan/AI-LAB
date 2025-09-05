package com.example.myassistantdemo.asr;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.myassistantdemo.rtc.AudioProcessJni;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class AsrService {
    private static final String LOG_TAG = "AsrService";

    private final Context mContext;

    public interface AsrCallback {
        void onAsrMessage(String message);
        void onAsrStreaming(String message);   // 新增
        void onAsrError(String message);
    }

    private static final int SAMPLE_RATE = 16000;  // The sampling rate
    private AtomicBoolean asrKeepRunning = new AtomicBoolean(false);
    private final AsrCallback mAsrCallbackHandle;
    private static final List<String> resource = Arrays.asList("final.zip", "units.txt", "ctc.ort", "decoder.ort", "encoder.ort");
    private AsrRecordThread mAsrRecordThread = null;
    private AsrDecodeThread mAsrWaitingforFinishThread = null;

    private boolean enableAGC = false;
    private boolean autoStop = true;  // 识别成功后自动停止
    private final Object enableAGC_lock = new Object();

    private AudioManager mAudioManager;

    /**
     * 模型初始化
     */
    public AsrService(Context context, AsrCallback asrCallback) {
        mContext = context;
        mAsrCallbackHandle = asrCallback;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        try {
            assetsInit();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error process asset files to file path");
        }
        AsrJniStub.init(mContext.getFilesDir().getPath());
        Log.i(LOG_TAG, "AsrService init Recognize with path : " + context.getFilesDir().getPath());
    }

    public void setAgcEnable(boolean value) {
        synchronized (enableAGC_lock) {
            enableAGC = value;
        }
        Log.i(LOG_TAG, "AsrService.setAgcEnable set enableAGC = " + value);
    }

    /**
     * 切换自动停止功能，默认为启用自动停止，即在发生唤醒之后自动停止唤醒模型的监听和推理
     * @param value
     */
    public void setAutoStop(boolean value){
        this.autoStop = value;
    }

    /**
     * 启动语音识别
     * @return true：启动成功，或者已经启动； false：由于音频服务被占用，导致启动失败
     */
    public boolean start() {
        if (!asrKeepRunning.get()) {
            if (!mAudioManager.getActiveRecordingConfigurations().isEmpty()) {
                Log.e(LOG_TAG, "Another app or thread are holding the audio service, refuse to start asr!");
                return false;
            } else {
                asrKeepRunning.set(true);
                AsrJniStub.reset();

                mAsrRecordThread = new AsrRecordThread();
                mAsrWaitingforFinishThread = new AsrDecodeThread();

                mAsrRecordThread.start();
                mAsrWaitingforFinishThread.start();

                Log.i(LOG_TAG, "ASR started!");
                return true;
            }
        } else {
            Log.w(LOG_TAG, "ASR already started!");
            return true;
        }
    }

    /**
     * 停止监听
     */
    public void stop() {
        if (asrKeepRunning.get()) {
            asrKeepRunning.set(false);
            AsrJniStub.setInputFinished();
            Log.i(LOG_TAG, "ASR stopped!");
        } else {
            Log.i(LOG_TAG, "ASR already stopped!");
        }
    }

    /**
     * @throws IOException
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
     * 录音线程
     */
    private class AsrRecordThread extends Thread {
        private int miniBufferSize = 0;  // 1280 bytes 648 byte 40ms, 0.04s
        private boolean recordInitSucceed = true;
        private AudioRecord record = null;

        /**
         * 初始化录音线程
         */
        public AsrRecordThread() {
            // buffer size in bytes 1280
            miniBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (miniBufferSize == AudioRecord.ERROR || miniBufferSize == AudioRecord.ERROR_BAD_VALUE) {
                Log.e(LOG_TAG, "Audio buffer can't initialize，AsrRecordThread will never provide valid audio buffer to asr!");
                recordInitSucceed = false;
                return;
            }
            // 没有录音权限
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                recordInitSucceed = false;
                Log.e(LOG_TAG, "NO RECORD_AUDIO permission, AsrRecordThread will never provide valid audio buffer to asr!");
                return;
            }

            record = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    miniBufferSize);
            // 录音机初始化失败
            if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(LOG_TAG, "AudioRecord init failed! AsrRecordThread will never provide valid audio buffer to asr!");
                recordInitSucceed = false;
                return;
            }
            Log.i(LOG_TAG, "AsrRecordThread init succeed!");
        }

        /**
         * 录音并把数据传递给asr执行器
         */
        @Override
        public void run() {
            if (record == null || !recordInitSucceed) {
                return;
            }

            int ns_status = AudioProcessJni.initiateNSInstance(SAMPLE_RATE, 3);
            long agc_status = AudioProcessJni.AgcInit(0, 255, 16000);
            Log.i(LOG_TAG, "Noise Suppressor is enabled! AudioProcessJni.initiateNSInstance " + (ns_status == 7 ? "succeed" : "failed"));
            Log.i(LOG_TAG, "Auto Gain Control is enabled! AudioProcessJni.AgcInit " + (agc_status == 7 ? "succeed" : "failed"));

            if (ns_status != 7 || agc_status != 7) {
                return;
            }

            record.startRecording();
//            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

            while (asrKeepRunning.get()) {
                short[] buffer = new short[miniBufferSize / 2];
                int read = record.read(buffer, 0, buffer.length);

                synchronized (enableAGC_lock) {
                    if (enableAGC) {
                        ByteBuffer outBuffer = ByteBuffer.allocateDirect(640);
                        //                    ByteBuffer outBuffer = ByteBuffer.allocateDirect(320);
                        for (int i = 0; i < buffer.length; i += 160) {
                            // 1. noise suppression
                            ns_status = AudioProcessJni.processNS(outBuffer, buffer, 160);
                            if (ns_status == 7) {  // NS_SUCCESS = 7 define in rtc/ns_jni_wrapper.c
                                for (int j = 0; j < 160; j++) {
                                    buffer[j] = (short) ((outBuffer.get(2 * j) & 0x00FF) + (outBuffer.get(2 * j + 1) << 8));
                                }
                            }
                            // 2. voice enhance
                            agc_status = AudioProcessJni.AgcFun(outBuffer, buffer, 160);
                            if (agc_status == 7) {  // AGC_SUCCESS = 7 define in rtc/ns_jni_wrapper.c
                                for (int j = 0; j < 160; j++) {
                                    buffer[j] = (short) (outBuffer.get(2 * j) + (outBuffer.get(2 * j + 1) << 8));
                                }
                            }
                        }
                    }
                }

                switch (read) {
                    case AudioRecord.ERROR_INVALID_OPERATION:
                        Log.e(LOG_TAG, "asr recorder get ERROR_INVALID_OPERATION!");
                    case AudioRecord.ERROR_BAD_VALUE:
                        Log.e(LOG_TAG, "asr recorder get ERROR_BAD_VALUE!");
                    case AudioRecord.ERROR_DEAD_OBJECT:
                        Log.e(LOG_TAG, "asr recorder get ERROR_DEAD_OBJECT!");
                    case AudioRecord.ERROR:
                        Log.e(LOG_TAG, "asr recorder get ERROR!");
                    default:
                        AsrJniStub.acceptWaveform(buffer);
                        Log.i(LOG_TAG, "asr partial result: " + AsrJniStub.getResult());
                        mAsrCallbackHandle.onAsrStreaming(AsrJniStub.getResult());           // 新增
                }
            }
            record.stop();

            AudioProcessJni.destoryNS();
            AudioProcessJni.AgcFree();

        }
    }

    /**
     * 解码线程
     */
    private class AsrDecodeThread extends Thread {
        @Override
        public void run() {
            // Send all data
            while (asrKeepRunning.get()) {
                if (AsrJniStub.Decode()) {
                    String text = AsrJniStub.getResult();
                    Log.d(LOG_TAG, "asr finished:" + text);
                    mAsrCallbackHandle.onAsrMessage(text);
                    if (autoStop){
                        asrKeepRunning.set(false);
                        AsrJniStub.setInputFinished();
                        Log.i(LOG_TAG, "asr auto stopped!");
                    }
                }
            }
        }
    }

}
