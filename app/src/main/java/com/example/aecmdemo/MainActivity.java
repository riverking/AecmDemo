package com.example.aecmdemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import ru.theeasiestway.libaecm.AEC;

public class MainActivity extends AppCompatActivity {
    private PermissionUtil permissionUtil;
    private Button startNoAecView;
    private Button startOriAecView;
    private Button startAecmView;
    private Button playNoAecView;
    private Button playOriAecView;
    private Button playAecmView;
    private Button stopPlayView;
    private EditText editText;
    private AEC mobileAec = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startNoAecView = findViewById(R.id.start_no_aec_test);
        playNoAecView = findViewById(R.id.play_no_aec_test);
        startOriAecView = findViewById(R.id.start_ori_test);
        playOriAecView = findViewById(R.id.play_ori_test);
        startAecmView = findViewById(R.id.start_webrtc_test);
        playAecmView = findViewById(R.id.play_webrtc_test);
        stopPlayView = findViewById(R.id.stop_play);
        editText = findViewById(R.id.edit_delay);
        startNoAecView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTest(1);
            }
        });
        playNoAecView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioTrackManager.getInstance().stopPlay();
                AudioTrackManager.getInstance().startPlayRecord(1);
            }
        });

        startOriAecView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTest(2);
            }
        });

        playOriAecView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioTrackManager.getInstance().stopPlay();
                AudioTrackManager.getInstance().startPlayRecord(2);
            }
        });

        startAecmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobileAec = new AEC(AEC.SamplingFrequency.FS_16000Hz,AEC.AggressiveMode.MOST_AGGRESSIVE);
                AudioRecordManager.getInstance(getApplicationContext()).setAec(mobileAec);
                AudioTrackManager.getInstance().setAec(mobileAec);
                AudioRecordManager.getInstance(getApplicationContext()).setDelay(Integer.valueOf(editText.getText().toString()));
                startTest(3);
            }
        });

        playAecmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioTrackManager.getInstance().stopPlay();
                AudioTrackManager.getInstance().startPlayRecord(3);
            }
        });

        stopPlayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTest();

            }
        });

    }
    private void startTest(int mode){
        AudioTrackManager.getInstance().startPlay(mode);
        AudioRecordManager.getInstance(this).startRecord(mode);
    }
    private void stopTest(){
        AudioTrackManager.getInstance().stopPlay();
        AudioRecordManager.getInstance(this).stopRecord();

    }

    @Override
    protected void onResume(){
        super.onResume();
        permissionUtil = new PermissionUtil(this, null);
        permissionUtil.request(new String[]{ Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE});
    }

    @Override
    protected void onDestroy(){
       super.onDestroy();
       AudioRecordManager.getInstance(this).onDestroy();
       mobileAec.close();
    }
}
