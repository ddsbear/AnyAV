package com.dds.avdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.dds.avdemo.audio.AudioActivity;
import com.dds.avdemo.media.MediaActivity;
import com.dds.avdemo.media.MediaCodecActivity;
import com.dds.avdemo.video.VideoActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //
    public void audioRecord(View view) {
        AudioActivity.openActivity(this);
    }

    public void videoRecord(View view) {
        VideoActivity.openActivity(this);
    }

    public void media(View view) {
        MediaActivity.openActivity(this);
    }

    public void MediaCodec(View view) {
        MediaCodecActivity.openActivity(this);
    }

    // 录屏功能
    public void ScreenRecord(View view) {


    }


}
