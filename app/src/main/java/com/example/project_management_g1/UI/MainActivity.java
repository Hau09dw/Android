package com.example.project_management_g1.UI;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.project_management_g1.DATA.CreateDatabase;
import com.example.project_management_g1.R;

public class MainActivity extends AppCompatActivity {
    View home_title;
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
        CreateDatabase createDatabase = new CreateDatabase(this);
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
}
