package com.dds.gles;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dds.gles.ui.GLCameraActivity;
import com.dds.gles.ui.GLGraphicalActivity;
import com.dds.gles.ui.GLImageActivity;

import java.util.ArrayList;

public class OpenGLActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView recyclerView;
    private static final ArrayList<MenuBean> mData = new ArrayList<>();

    static {
        add("绘制图形", GLGraphicalActivity.class);
        add("绘制图片", GLImageActivity.class);
        add("摄像机预览", GLCameraActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_gl);
        recyclerView = (RecyclerView) findViewById(R.id.mList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        initData();
    }

    // 初始化按钮列表
    private void initData() {
        recyclerView.setAdapter(new MenuAdapter());
    }

    private static void add(String name, Class<?> clazz) {
        MenuBean bean = new MenuBean();
        bean.name = name;
        bean.clazz = clazz;
        mData.add(bean);
    }

    private static class MenuBean {
        String name;
        Class<?> clazz;
    }

    private class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuHolder> {


        @NonNull
        @Override
        public MenuHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MenuHolder(getLayoutInflater().inflate(R.layout.item_button, parent, false));
        }

        @Override
        public void onBindViewHolder(MenuHolder holder, int position) {
            holder.setPosition(position);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        class MenuHolder extends RecyclerView.ViewHolder {

            private final Button mBtn;

            MenuHolder(View itemView) {
                super(itemView);
                mBtn = (Button) itemView.findViewById(R.id.mBtn);
                mBtn.setOnClickListener(OpenGLActivity.this);
            }

            public void setPosition(int position) {
                MenuBean bean = mData.get(position);
                mBtn.setText(bean.name);
                mBtn.setTag(position);
            }
        }

    }

    @Override
    public void onClick(View view) {
        int position = (int) view.getTag();
        MenuBean bean = mData.get(position);
        startActivity(new Intent(this, bean.clazz));
    }
}