package com.example.project_management_g1.UI;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_management_g1.DATA.TaskDAO;
import com.example.project_management_g1.MODEL.Task;
import com.example.project_management_g1.MODEL.Task_Adapter;
import com.example.project_management_g1.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rcvTask;
    private Task_Adapter taskAdapter;
    private TaskDAO taskDAO;
    private List<Task> taskList;
    private SearchView searchView;
    private FloatingActionButton fab;
    private ImageView cancelButton;

    private BackgroundMusicService musicService;
    private boolean isBound = false;
    private Switch musicSwitch;
    private static final String PREFS_NAME = "MusicPrefs";
    private static final String MUSIC_STATE = "MusicState";
    private BottomNavigationView bottomNavigationView;
    private boolean wasPlayingBeforePause = false;

    TextView txt_startdate, txt_enddate;
    ImageButton btnStartdate, btnEnddate;
    TextView estimateDay;
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
        try {
            initializeViews();
            loadTasks();
            //updateEstimateDayVisibility();
        } catch (Exception e) {
            e.printStackTrace();
            // Hiển thị thông báo lỗi
            Toast.makeText(this, "An error occurred while loading data", Toast.LENGTH_SHORT).show();
        }
        //khai bao action search
        SearchTask();
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });
        //Music Service
        Intent intent = new Intent(this, BackgroundMusicService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home_id) {
                return true;
            } else if (itemId == R.id.bottom_setting_id) {
                showSettingsDialog();
                return true;
            } else if (itemId == R.id.bottom_ganttchart_id) {
                return true;
            }
            return false;
        });
    }

    private void initializeViews() {

        rcvTask = findViewById(R.id.id_recyclerview);
        rcvTask.setLayoutManager(new LinearLayoutManager(this));
        //duong ngan cach
        rcvTask.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
    }

    private void loadTasks() {
        taskDAO = new TaskDAO(this);
        taskList = taskDAO.getAllTasks();

        if (taskList.isEmpty()) {
            // Hiển thị thông báo nếu không có dữ liệu
            Toast.makeText(this, "There is no data to display", Toast.LENGTH_SHORT).show();
        }

        taskAdapter = new Task_Adapter(taskList);
        rcvTask.setAdapter(taskAdapter);
    }

    private void SearchTask(){
        searchView = findViewById(R.id.action_search);
        searchView.clearFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterTask(newText);
                return true;
            }
        });
    }

    private void filterTask(String text) {
        List<Task> filteredListTask = new ArrayList<>();
        //loc theo task name hoac assignee
        for(Task task : taskList){
            if(task.getTask_name().toLowerCase().contains(text.toLowerCase()) || task.getAssignee().toLowerCase().contains(text.toLowerCase())){
                filteredListTask.add(task);
            }
        }
        if(filteredListTask.isEmpty()){
            Toast.makeText(this,"No data found",Toast.LENGTH_SHORT).show();
        }else{
            taskAdapter.setFilteredList(filteredListTask);
        }
    }

    private void showBottomDialog() {
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.bottomsheetlayout);
            dialog.setCanceledOnTouchOutside(true);
            cancelButton = dialog.findViewById(R.id.cancelButton);
            txt_enddate = dialog.findViewById(R.id.create_EndDate);
            txt_startdate = dialog.findViewById(R.id.create_startDate);
            btnStartdate = dialog.findViewById(R.id.btn_calendar_StartDate);
            btnEnddate = dialog.findViewById(R.id.btn_calendar_EndDate);

            btnStartdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openDialogStartDate();
                }
                private void openDialogStartDate() {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(dialog.getContext(), new DatePickerDialog.OnDateSetListener(){
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                            txt_startdate.setText(y +"/"+m+"/"+d);
                        }
                    },2024,10,9);
                    datePickerDialog.show();
                }
            });
            btnEnddate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openDialogEndDate();
                }
                private void openDialogEndDate() {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(dialog.getContext(), new DatePickerDialog.OnDateSetListener(){
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                            txt_enddate.setText(y+"/"+m+"/"+d);
                        }
                    },2024,11,9);
                    datePickerDialog.show();
                }
            });
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.getAttributes().windowAnimations = R.style.DialogAnimation;
                window.setGravity(Gravity.BOTTOM);
            }
            dialog.show();

    }

    // Settings Task
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BackgroundMusicService.LocalBinder binder = (BackgroundMusicService.LocalBinder) service;
            musicService = binder.getService();
            isBound = true;
            // Check saved state and play music if it was on
            if (getMusicState()) {
                musicService.playMusic();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };
    private void showSettingsDialog()
    {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.setting);
        dialog.setCanceledOnTouchOutside(true);

        musicSwitch = dialog.findViewById(R.id.musicSwitch);
        if (musicSwitch != null) {
            musicSwitch.setChecked(getMusicState());
            musicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isBound) {
                    if (isChecked) {
                        musicService.playMusic();
                    } else {
                        musicService.pauseMusic();
                    }
                    saveMusicState(isChecked);
                }
            });
        } else {
            Log.e("MainActivity", "musicSwitch not found in settings layout");
        }
        Switch estimateDaySwitch = dialog.findViewById(R.id.estimateDaySwitch);
        if (estimateDaySwitch != null) {
            estimateDaySwitch.setChecked(getEstimateDayState());
            estimateDaySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveEstimateDayState(isChecked);
                updateEstimateDayVisibility();
            });
        } else {
            Log.e("MainActivity", "estimateDaySwitch not found in settings layout");
        }
        cancelButton = dialog.findViewById(R.id.cancelMusicButton);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(view -> dialog.dismiss());
        } else {
            Log.e("MainActivity", "cancelButton not found in settings layout");
            // Thêm một cách khác để đóng dialog nếu nút cancel không tồn tại
            dialog.setOnCancelListener(dialogInterface -> dialogInterface.dismiss());
        }

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.getAttributes().windowAnimations = R.style.DialogAnimation;
            window.setGravity(Gravity.BOTTOM);
        }
        dialog.show();
    }
    private boolean getMusicState() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(MUSIC_STATE, false); // false is the default value
    }

    private void saveMusicState(boolean state) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(MUSIC_STATE, state);
        editor.apply();
    }
    private boolean getEstimateDayState(){
        SharedPreferences estimate = getSharedPreferences("EstimateDay",0);
        return estimate.getBoolean("On",true);
    }
    private void saveEstimateDayState(boolean state)
    {
        SharedPreferences estimate = getSharedPreferences("EstimateDay",0);
        SharedPreferences.Editor editor = estimate.edit();
        editor.putBoolean("On",state);
        editor.apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isBound && musicService.isPlaying()) {
            musicService.pauseMusic();
            wasPlayingBeforePause = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateEstimateDayVisibility();
        if (isBound && getMusicState() && wasPlayingBeforePause) {
            musicService.playMusic();
            wasPlayingBeforePause = false;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            musicService.pauseMusic(); // Đảm bảo nhạc được tạm dừng khi ứng dụng bị hủy
            unbindService(connection);
            isBound = false;
        }
    }
    private void updateEstimateDayVisibility() {
        estimateDay = findViewById(R.id.item_estimateday);
        if (estimateDay != null) {

            estimateDay.setVisibility(getEstimateDayState() ? View.VISIBLE : View.INVISIBLE);
        }
    }
}