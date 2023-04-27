package com.dds.avdemo;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.audio.AudioActivity;
import com.dds.avdemo.media.MediaActivity;
import com.dds.avdemo.media.MediaCodecActivity;
import com.dds.avdemo.media.RecordActivity;
import com.dds.avdemo.permission.Permissions;
import com.dds.avdemo.video.VideoActivity;
import com.dds.gles.OpenGLActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Permissions.request(this, new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA}, integer -> {
                    if (integer != 0) {
                        Toast.makeText(MainActivity.this, "请给权限", Toast.LENGTH_LONG).show();
                    }

                });
    }

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
        RecordActivity.openActivity(this);

    }

    public void opengl(View view) {
        startActivity(new Intent(this, OpenGLActivity.class));

    }
}
