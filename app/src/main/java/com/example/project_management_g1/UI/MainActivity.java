package com.example.project_management_g1.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.project_management_g1.DATA.CreateDatabase;
import com.example.project_management_g1.R;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    View home_title;
    private MediaPlayer mediaPlayer;
    boolean isPaused = false;
    private boolean isMusicOn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        checkMusic();
        CreateDatabase createDatabase = new CreateDatabase(this);
        LinearLayout settings = (LinearLayout)findViewById(R.id.setting_part);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SettingActivity.class);
                startActivity(intent);
            }
        });

        createDatabase.open();
    }
    public void createDynamicGradient() {
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, // Từ Top-Left đến Bottom-Right
                new int[] {
                        Color.parseColor("#FFB5C5"), // Pastel pink
                        Color.parseColor("#E6E6FA"), // Pastel lavender
                        Color.parseColor("#87CEEB")  // Pastel blue
                }
        );
        home_title = findViewById(R.id.id_home_title);
        home_title.setBackground(gradientDrawable);
    }
    private void checkMusic()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("MusicPrefs", MODE_PRIVATE);
        isMusicOn = sharedPreferences.getBoolean("isMusicOn", false);

        if (isMusicOn) {
            Intent intent = new Intent(this, BackgroundMusicService.class);
            intent.setAction("PLAY");
            startService(intent);
        }
    }

}
