package com.dds.base.permission;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.widget.Toast;

public class PermissionUtils {

    public static void requestCameraPermission(Activity activity) {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            permissions = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE};
        }
        Permissions.request(activity, permissions, integer -> {
            if (integer != 0) {
                Toast.makeText(activity, "请给权限", Toast.LENGTH_LONG).show();
            }
        });
    }
}
