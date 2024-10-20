package com.example.project_management_g1.UI;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import com.example.project_management_g1.R;

public class BackgroundMusicService extends Service {
    private MediaPlayer mediaPlayer;
    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        BackgroundMusicService getService() {
            return BackgroundMusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.dandadan);
        mediaPlayer.setLooping(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void playMusic() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void pauseMusic() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

}
