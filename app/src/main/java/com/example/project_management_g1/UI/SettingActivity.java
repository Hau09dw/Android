package com.example.project_management_g1.UI;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_management_g1.R;

public class SettingActivity extends AppCompatActivity {
    private Switch musicSwitch;
    private BackgroundMusicService musicService;
    private boolean bound = false;
    private SharedPreferences sharedPreferences;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BackgroundMusicService.LocalBinder binder = (BackgroundMusicService.LocalBinder) service;
            musicService = binder.getService();
            bound = true;
            updateSwitchState();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        sharedPreferences = getSharedPreferences("MusicPrefs", MODE_PRIVATE);

        musicSwitch = findViewById(R.id.musicSwitch);
        musicSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (bound) {
                    Intent intent = new Intent(SettingActivity.this, BackgroundMusicService.class);
                    if (isChecked) {
                        intent.setAction("PLAY");
                    } else {
                        intent.setAction("PAUSE");
                    }
                    startService(intent);

                    // Lưu trạng thái của switch
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isMusicOn", isChecked);
                    editor.apply();
                }
            }
        });
    }

    private void updateSwitchState() {
        boolean isMusicOn = sharedPreferences.getBoolean("isMusicOn", false);
        musicSwitch.setChecked(isMusicOn);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BackgroundMusicService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override

    protected void onStop() {
        super.onStop();
        if (bound) {
            // Gửi intent để tạm dừng nhạc
            /*Intent intent = new Intent(this, BackgroundMusicService.class);
            intent.setAction("PAUSE");
            startService(intent);*/

            unbindService(connection);
            bound = false;
        }
    }
}
