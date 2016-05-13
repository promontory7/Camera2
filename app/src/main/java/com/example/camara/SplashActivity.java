package com.example.camara;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.camara.utils.Utils;

/**
 * Created by Administrator on 2016/5/8.
 */
public class SplashActivity extends AppCompatActivity {
    static final int CAMERA_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Button button = (Button) findViewById(R.id.start);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utils.checkCameraHardware(SplashActivity.this)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!(checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
                                requestCameraPermission();
                            } else {
                                startActivity(new Intent(SplashActivity.this, MainActivity.class));

                            }
                        } else {
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        }
                    } else {
                        Toast.makeText(SplashActivity.this, "手机无可摄像头", Toast.LENGTH_LONG).show();
                    }

                }
            });
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestCameraPermission() {
        requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CAMERA_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_CODE) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        }

    }
}
