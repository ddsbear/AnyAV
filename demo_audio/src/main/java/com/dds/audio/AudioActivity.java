package com.dds.audio;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import com.example.audio.R;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import io.kvh.media.amr.AmrEncoder;

public class AudioActivity extends AppCompatActivity {

    // 声明 AudioRecord 对象
    private AudioRecord audioRecord = null;

    boolean isRecording;
    byte[] data;
    private String rawFilename;
    private String wavFileName;
    private String amrFileName;
    private RecordThread recordingThread;

    // 声明recordBuffer的大小字段
    private int recordBufSize = 0;
    int frequency;
    int channelConfig;
    int encodingBitRate;


    public static void openActivity(AppCompatActivity activity) {
        Intent intent = new Intent(activity, AudioActivity.class);
        activity.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        rawFilename = Environment.getExternalStorageDirectory() + File.separator + "dds_test" + File.separator + UUID.randomUUID() + "dds.raw";
        wavFileName = Environment.getExternalStorageDirectory() + File.separator + "dds_test" + File.separator + UUID.randomUUID() + "dds.wav";
        amrFileName = Environment.getExternalStorageDirectory() + File.separator + "dds_test" + File.separator + UUID.randomUUID() + "dds.amr";
        File file = new File(rawFilename);
        File wavFile = new File(wavFileName);
        File amrFile = new File(amrFileName);

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!wavFile.exists()) {
            try {
                wavFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!amrFile.exists()) {
            try {
                amrFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void start(View view) {
        //获取buffer的大小并创建AudioRecord
        createAudioRecord();
        // 初始化一个buffer
        data = new byte[recordBufSize];
        //开始录音
        audioRecord.startRecording();
        isRecording = true;
        recordingThread = new RecordThread();
        recordingThread.start();

    }


    public void pause(View view) {
        isRecording = false;
        recordingThread = null;
    }


    public void resume(View view) {
        isRecording = true;
        recordingThread = new RecordThread();
        recordingThread.start();
    }

    public void stop(View view) {
        isRecording = false;
        if (null != audioRecord) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            recordingThread = null;
        }
    }

    public void createAudioRecord() {
        if (audioRecord == null) {
            //采样率
            frequency = 8000;

            channelConfig = AudioFormat.CHANNEL_IN_MONO;
            //编码制式和采样大小
            encodingBitRate = AudioFormat.ENCODING_PCM_16BIT;

            //AudioFormat.CHANNEL_IN_DEFAULT 声道个数
            //AudioFormat.ENCODING_PCM_16BIT 采样字节数
            recordBufSize = AudioRecord.getMinBufferSize(frequency, channelConfig, encodingBitRate);

            //android 支持MONO单声道 和 AudioFormat.CHANNEL_IN_STEREO立体声
            int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;

            //MediaRecorder.AudioSource.MIC设定录音来源为主麦克风
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, encodingBitRate, recordBufSize);
        }


    }

    // 将raw源文件转为可用wav格式
    public void save(View view) {
        PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(frequency, channelConfig, encodingBitRate);
        pcmToWavUtil.pcmToWav(rawFilename, wavFileName);
    }


    private AudioTrack mAudioTrack;
    PlayAudioThread playAudioThread;
    int audioBufSize = 0;

    //播放声音
    public void play(View view) {
        createAudioTrack();
        if (playAudioThread != null && Thread.State.RUNNABLE == playAudioThread.getState()) {
            try {
                Thread.sleep(500);
                playAudioThread.interrupt();
            } catch (Exception e) {
                playAudioThread = null;
            }
        }
        playAudioThread = null;
        playAudioThread = new PlayAudioThread();
        playAudioThread.start();


    }

    public void wav2amr(View view) {
        new Thread(() -> AmrEncoder.convertAMR(wavFileName, amrFileName)).start();


    }

    public void amrPlay(View view) {

    }

    private void createAudioTrack() {
        // 获得构建对象的最小缓冲区大小
        if (mAudioTrack == null) {
            audioBufSize = AudioTrack.getMinBufferSize(frequency, AudioFormat.CHANNEL_OUT_MONO, encodingBitRate);
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, AudioFormat.CHANNEL_OUT_MONO, encodingBitRate,
                    audioBufSize, AudioTrack.MODE_STREAM);
        }


    }


    class PlayAudioThread extends Thread {
        @Override
        public void run() {
            DataInputStream dis;
            try {
                dis = new DataInputStream(new FileInputStream(rawFilename));
                byte[] tempBuffer = new byte[audioBufSize];
                int readCount;
                while (dis.available() > 0) {
                    readCount = dis.read(tempBuffer);
                    if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
                        continue;
                    }
                    if (readCount != 0 && readCount != -1) {
                        mAudioTrack.play();
                        mAudioTrack.write(tempBuffer, 0, readCount);
                    }
                }
                if (mAudioTrack != null) {
                    if (mAudioTrack.getState() == AudioRecord.STATE_INITIALIZED) {
                        mAudioTrack.stop();
                    }
                }
                dis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    class RecordThread extends Thread {
        @Override
        public void run() {
            try {
                FileOutputStream os = new FileOutputStream(rawFilename);
                int read;
                while (isRecording) {
                    read = audioRecord.read(data, 0, recordBufSize);
                    // 如果读取音频数据没有出现错误，就将数据写入到文件
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        Log.d("dds", "write...");
                        os.write(data);
                    }
                }
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAudioTrack != null) {
            mAudioTrack.release();
            mAudioTrack = null;
        }
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }
}
