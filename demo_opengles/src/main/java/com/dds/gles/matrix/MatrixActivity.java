package com.dds.gles.matrix;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.dds.gles.R;

public class MatrixActivity extends AppCompatActivity {


    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matrix);
        imageView = findViewById(R.id.iv_image);
    }

    public void onScale(View view) {
        Matrix matrix = imageView.getImageMatrix();
        matrix.setRotate(20, 100,100);
        imageView.setImageMatrix(matrix);
    }
}