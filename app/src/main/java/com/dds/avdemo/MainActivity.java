package com.dds.avdemo;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.avdemo.audio.AudioActivity;
import com.dds.avdemo.media.MediaActivity;
import com.dds.avdemo.media.MediaCodecActivity;
import com.dds.avdemo.media.RecordActivity;
import com.dds.avdemo.opegl.camera.preview.PreviewCameraActivity;
import com.dds.avdemo.opegl.glsv.ImageGLSurfaceView;
import com.dds.avdemo.permission.Consumer;
import com.dds.avdemo.permission.Permissions;
import com.dds.avdemo.video.VideoActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Permissions.request(this, new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA}, new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                if(integer != 0){
                    Toast.makeText(MainActivity.this,"请给权限",Toast.LENGTH_LONG).show();
                }

            }
        });
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            RecordActivity.openActivity(this);
        }
    }

    public void opengl(View view) {

//         setContentView(new TriangleGLSurfaceView(this)); // 绘制三角形

        // setContentView(new SquareGLSurfaceView(this));  // 绘制正方形

        // setContentView(new OvalGLSurfaceView(this)); // 绘制圆形

        // setContentView(new PaintPointGLSurfaceView(this)); // 手绘点

        // setContentView(new RotateTriangleGLSurfaceView(this)); // 旋转三角形

        try {
            setContentView(new ImageGLSurfaceView(this)); // 加载图片
        } catch (IOException e) {
            e.printStackTrace();
        }

//         startActivity(new Intent(this, PreviewCameraActivity.class));  // OpenGL预览摄像头

//        startActivity(new Intent(this, TakePictureActivity.class));  // OpenGL 拍照

    }
}
