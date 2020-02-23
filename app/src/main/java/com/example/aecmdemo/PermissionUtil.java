package com.example.aecmdemo;

import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;

public class PermissionUtil {
    private AppCompatActivity appCompatActivity;
    private Result result;

    public interface Result {
        void get();
    }

    public PermissionUtil(AppCompatActivity appCompatActivity, Result result) {
        this.appCompatActivity = appCompatActivity;
        this.result = result;
    }

    public PermissionUtil request(String[] permissions) {
        if (hasPermission(permissions)) {
            //result.get();
        } else {
            requestPermission(permissions);
        }
        return this;
    }

    private boolean hasPermission(String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean allHas = true;
            for (String permission : permissions) {
                allHas &= appCompatActivity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
            }
            return allHas;
        } else {
            return true;
        }
    }

    private void requestPermission(String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            appCompatActivity.requestPermissions(permissions, 1);
        }
    }

    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0) {
                boolean allHas = true;
                for (int grantResult : grantResults) {
                    allHas &= PackageManager.PERMISSION_GRANTED == grantResult;
                }
                if (allHas) {
                    result.get();
                } else {
                    requestPermission(permissions);
                }
            } else {
                requestPermission(permissions);
            }
        }
    }

}
