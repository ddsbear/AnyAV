package com.dds.camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dds.base.permission.Permissions;
import com.dds.camera.camera1.Camera1SurfaceViewActivity;
import com.dds.camera.camera2.Camera2SurfaceViewActivity;
import com.dds.fbo.R;

import java.util.ArrayList;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView recyclerView;
    private static final ArrayList<MenuBean> mData = new ArrayList<>();
    public static final String BUNDLE_KEY = "bundle";

    static {
        add("Camera1+SurfaceView", Camera1SurfaceViewActivity.class);
        add("Camera2+SurfaceView", Camera2SurfaceViewActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        recyclerView = (RecyclerView) findViewById(R.id.mList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        initData();

        Permissions.request(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, integer -> {
            if (integer != 0) {
                Toast.makeText(this, "请给权限", Toast.LENGTH_LONG).show();
            }

        });
    }

    // 初始化按钮列表
    private void initData() {
        recyclerView.setAdapter(new MenuAdapter());
    }

    private static void add(String name, Class<?> clazz) {
        add(name, clazz, null);
    }

    private static void add(String name, Class<?> clazz, Bundle bundle) {
        MenuBean bean = new MenuBean();
        bean.name = name;
        bean.clazz = clazz;
        bean.bundle = bundle;
        mData.add(bean);
    }

    private static class MenuBean {
        String name;
        Class<?> clazz;
        Bundle bundle;
    }

    private class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuHolder> {


        @NonNull
        @Override
        public MenuHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            Button button = new Button(parent.getContext());
            button.setLayoutParams(params);
            button.setAllCaps(false);
            return new MenuHolder(button);
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


            MenuHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(CameraActivity.this);
            }

            public void setPosition(int position) {
                MenuBean bean = mData.get(position);
                ((Button) itemView).setText(bean.name);
                itemView.setTag(position);
            }
        }

    }

    @Override
    public void onClick(View view) {
        int position = (int) view.getTag();
        MenuBean bean = mData.get(position);
        Intent intent = new Intent(this, bean.clazz);
        intent.putExtra(BUNDLE_KEY, bean.bundle);
        startActivity(intent);
    }
}