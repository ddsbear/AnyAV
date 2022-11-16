package com.dds.avdemo.audio;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.dds.avdemo.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * 声音波形图
 */
public class AudioWaveView extends View {


    /**
     * LINE: 线性波形图 RECT: 方形波形图
     */
    private final int LINE = 0;
    private final int RECT = 1;
    private int mode;
    /**
     * 灵敏度
     */
    private int sensibility = 4;
    private float maxVolume = 100;
    /**
     * 振幅
     */
    private float amplitude = 1;
    /**
     * 音量
     */
    private float volume = 10;

    private int middleLineColor = Color.BLACK;
    private int voiceLineColor = Color.BLACK;
    private float middleLineHeight = 4;
    private float translateX = 0;
    private boolean isSet = false;
    private int fineness = 1;
    private float targetVolume = 1;
    private long speedY = 50;
    private float rectWidth = 25;
    private float rectSpace = 5;
    private float rectInitHeight = 4;
    private List<Rect> rectList;

    private long lastTime = 0;
    private int lineSpeed = 90;

    List<Path> paths = null;

    private Paint paint;
    private Paint paintVoiceLine;

    public AudioWaveView(Context context) {
        super(context);
    }

    public AudioWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AudioWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AudioWaveView);
        mode = typedArray.getInt(R.styleable.AudioWaveView_viewMode, 0);
        voiceLineColor = typedArray.getColor(R.styleable.AudioWaveView_voiceLine, ContextCompat.getColor(context, R.color.colorPrimary));
        maxVolume = typedArray.getFloat(R.styleable.AudioWaveView_maxVolume, 100);
        sensibility = typedArray.getInt(R.styleable.AudioWaveView_sensibility, 4);
        if (mode == RECT) {
            rectWidth = typedArray.getDimension(R.styleable.AudioWaveView_rectWidth, 25);
            rectSpace = typedArray.getDimension(R.styleable.AudioWaveView_rectSpace, 5);
            rectInitHeight = typedArray.getDimension(R.styleable.AudioWaveView_rectInitHeight, 4);
        } else if (mode == LINE) {
            middleLineColor = typedArray.getColor(R.styleable.AudioWaveView_middleLine, Color.BLACK);
            middleLineHeight = typedArray.getDimension(R.styleable.AudioWaveView_middleLineHeight, 4);
            lineSpeed = typedArray.getInt(R.styleable.AudioWaveView_lineSpeed, 90);
            fineness = typedArray.getInt(R.styleable.AudioWaveView_fineness, 1);
            paths = new ArrayList<>(20);
            for (int i = 0; i < 20; i++) {
                paths.add(new Path());
            }
        }
        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mode == RECT) {
            drawVoiceRect(canvas);
        } else {
            drawMiddleLine(canvas);
            drawVoiceLine(canvas);
        }
        runDraw();
    }

    private void drawMiddleLine(Canvas canvas) {
        if (paint == null) {
            paint = new Paint();
            paint.setColor(middleLineColor);
            paint.setAntiAlias(true);
        }
        canvas.save();
        canvas.drawRect(0, (getHeight() >> 1) - middleLineHeight / 2, getWidth(), (getHeight() >> 1) + middleLineHeight / 2, paint);
        canvas.restore();
    }

    private void drawVoiceLine(Canvas canvas) {
        lineChange();
        if (paintVoiceLine == null) {
            paintVoiceLine = new Paint();
            paintVoiceLine.setColor(voiceLineColor);
            paintVoiceLine.setAntiAlias(true);
            paintVoiceLine.setStyle(Paint.Style.STROKE);
            paintVoiceLine.setStrokeWidth(2);
        }
        canvas.save();
        int moveY = getHeight() / 2;
        for (int i = 0; i < paths.size(); i++) {
            paths.get(i).reset();
            paths.get(i).moveTo(getWidth(), getHeight() >> 1);
        }
        for (float i = getWidth() - 1; i >= 0; i -= fineness) {
            amplitude = 4 * volume * i / getWidth() - 4 * volume * i * i / getWidth() / getWidth();
            for (int n = 1; n <= paths.size(); n++) {
                float sin = amplitude * (float) Math.sin((i - Math.pow(1.22, n)) * Math.PI / 180 - translateX);
                paths.get(n - 1).lineTo(i, (2 * n * sin / paths.size() - 15 * sin / paths.size() + moveY));
            }
        }
        for (int n = 0; n < paths.size(); n++) {
            if (n == paths.size() - 1) {
                paintVoiceLine.setAlpha(255);
            } else {
                paintVoiceLine.setAlpha(n * 130 / paths.size());
            }
            if (paintVoiceLine.getAlpha() > 0) {
                canvas.drawPath(paths.get(n), paintVoiceLine);
            }
        }
        canvas.restore();
    }

    private void drawVoiceRect(Canvas canvas) {
        if (paintVoiceLine == null) {
            paintVoiceLine = new Paint();
            paintVoiceLine.setColor(voiceLineColor);
            paintVoiceLine.setAntiAlias(true);
            paintVoiceLine.setStyle(Paint.Style.STROKE);
            paintVoiceLine.setStrokeWidth(2);
        }
        if (rectList == null) {
            rectList = new LinkedList<>();
        }
        int totalWidth = (int) (rectSpace + rectWidth);
        if (speedY % totalWidth < 6) {
            Rect rect = new Rect((int) (-rectWidth - 10 - speedY + speedY % totalWidth),
                    (int) (getHeight() / 2 - rectInitHeight / 2 - (volume == 10 ? 0 : volume / 2)),
                    (int) (-10 - speedY + speedY % totalWidth),
                    (int) (getHeight() / 2 + rectInitHeight / 2 + (volume == 10 ? 0 : volume / 2)));
            if (rectList.size() > getWidth() / (rectSpace + rectWidth) + 2) {
                rectList.remove(0);
            }
            rectList.add(rect);
        }
        canvas.translate(speedY, 0);
        for (int i = rectList.size() - 1; i >= 0; i--) {
            canvas.drawRect(rectList.get(i), paintVoiceLine);
        }
        rectChange();
    }

    public void setVolume(int volume) {
        if (volume > maxVolume * sensibility / 25) {
            isSet = true;
            this.targetVolume = ((getHeight() * volume) >> 1) / maxVolume;
        }
    }

    private void lineChange() {
        if (lastTime == 0) {
            lastTime = System.currentTimeMillis();
            translateX += 1.5;
        } else {
            if (System.currentTimeMillis() - lastTime > lineSpeed) {
                lastTime = System.currentTimeMillis();
                translateX += 1.5;
            } else {
                return;
            }
        }
        if (volume < targetVolume && isSet) {
            volume += getHeight() / 30;
        } else {
            isSet = false;
            if (volume <= 10) {
                volume = 10;
            } else {
                if (volume < getHeight() / 30) {
                    volume -= getHeight() / 60;
                } else {
                    volume -= getHeight() / 30;
                }
            }
        }
    }

    private void rectChange() {
        speedY += 6;
        if (volume < targetVolume && isSet) {
            volume += getHeight() / 30;
        } else {
            isSet = false;
            if (volume <= 10) {
                volume = 10;
            } else {
                if (volume < getHeight() / 30) {
                    volume -= getHeight() / 60;
                } else {
                    volume -= getHeight() / 30;
                }
            }
        }
    }

    public void runDraw() {
        if (mode == RECT) {
            postInvalidateDelayed(30);
        } else {
            invalidate();
        }
    }

}
