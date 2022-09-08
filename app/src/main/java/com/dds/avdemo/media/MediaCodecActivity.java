package com.dds.avdemo.media;

import android.content.Intent;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import com.dds.avdemo.R;



public class MediaCodecActivity extends AppCompatActivity {
    //使用MediaCodec进行图像的硬编码


    public static void openActivity(AppCompatActivity activity) {
        Intent intent = new Intent(activity, MediaCodecActivity.class);
        activity.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_codec);
    }
}
