package com.dds.gles.demo2.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class AutoFitSurfaceView extends SurfaceView {

    private float mAspectRatio;

    public AutoFitSurfaceView(Context context) {
        super(context);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAspectRatio(int width, int height) {
        mAspectRatio = (float) width / height;
        getHolder().setFixedSize(width, height);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (mAspectRatio == 0) {
            setMeasuredDimension(width, height);
        } else {
            int newW, newH;
            float actualRatio;
            if (width > height) {
                actualRatio = mAspectRatio;
            } else {
                actualRatio = 1 / mAspectRatio;
            }

            if (width < height * actualRatio) {
                newH = height;
                newW = (int) (height * actualRatio);
            } else {
                newW = width;
                newH = (int) (width / actualRatio);
            }
            setMeasuredDimension(newW, newH);

        }
    }

}
