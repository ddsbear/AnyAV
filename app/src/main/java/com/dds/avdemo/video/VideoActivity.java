package com.dds.avdemo.video;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.avdemo.R;

public class VideoActivity extends AppCompatActivity {


    public static void openActivity(AppCompatActivity activity) {
        Intent intent = new Intent(activity, VideoActivity.class);
        activity.startActivity(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

    }


    public void SurfaceView(View view) {
        SurfaceViewActivity.openActivity(this);
    }

    public void TextureView(View view) {
        TextureViewActivity.openActivity(this);
    }
}
