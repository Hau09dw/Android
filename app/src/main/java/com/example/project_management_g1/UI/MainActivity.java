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
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_management_g1.DATA.TaskDAO;
import com.example.project_management_g1.MODEL.Task;
import com.example.project_management_g1.MODEL.Task_Adapter;
import com.example.project_management_g1.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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

    EditText txt_taskname, txt_assignee, txt_estimaday, txt_startdate, txt_enddate;
    ImageButton btnStartdate, btnEnddate;
    Button btn_confirm;
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
                showCreateBottomDialog();
            }
        });
        //delete task
        deleteProject();
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
    //delete task
    private void deleteProject() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT |   ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int i = viewHolder.getAdapterPosition();
                if(i != RecyclerView.NO_POSITION){
                    Task task = taskList.get(i);
                    taskDAO.deleteTask(task);
                    loadTasks();
                    Toast.makeText(MainActivity.this, "Task deleted succefull", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(MainActivity.this, "Failed to delete task", Toast.LENGTH_SHORT).show();

            }
        });
        itemTouchHelper.attachToRecyclerView(rcvTask);
    }

    //khoi tao reyclerView
    private void initializeViews() {
        taskDAO = new TaskDAO(this);
        rcvTask = findViewById(R.id.id_recyclerview);
    }
    //load lai data
    private void loadTasks() {
        taskList = taskDAO.getAllTasks();
        if (taskList.isEmpty()) {
            // Hiển thị thông báo nếu không có dữ liệu
            Toast.makeText(this, "There is no data to display", Toast.LENGTH_SHORT).show();
        }
        setUpRecycerView();
    }
    private void setUpRecycerView(){
        taskAdapter = new Task_Adapter(taskList);
        rcvTask.setAdapter(taskAdapter);
        rcvTask.setLayoutManager(new LinearLayoutManager(this));
        //duong ngan cach
        rcvTask.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        // xu ly action reyclerview
        taskAdapter.setOnItemClickListener(new Task_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(Task task) {
                showUpdateBottomDialog(task);
            }
        });
    }
    //tim kiem theo task name hoac assignee
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
        searchView.findViewById(R.id.action_search).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (searchView.hasFocus()) {
                        Rect outRect = new Rect();
                        searchView.getGlobalVisibleRect(outRect);
                        if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                            searchView.clearFocus();
                        }
                    }
                }
                return false;
            }
        });
    }
    //loc theo task name hoac assignee
    private void filterTask(String text) {
        List<Task> filteredListTask = new ArrayList<>();
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
    //acion update task
    private void showUpdateBottomDialog(final Task task) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.task_update_layout);

        TextView taskname_ = dialog.findViewById(R.id.textView_TaskName);
        TextView assginee_ = dialog.findViewById(R.id.textView_Asssigee);
        TextView enddate_ = dialog.findViewById(R.id.textView_endDate);
        TextView startdate_ = dialog.findViewById(R.id.textView_startDate);

        txt_taskname = dialog.findViewById(R.id.update_taskName);
        txt_assignee = dialog.findViewById(R.id.update_assignee);
        txt_estimaday = dialog.findViewById(R.id.update_estimateday);
        txt_enddate = dialog.findViewById(R.id.update_EndDate);
        txt_startdate = dialog.findViewById(R.id.update_startDate);

        btnStartdate = dialog.findViewById(R.id.btn_update_StartDate);
        btnEnddate = dialog.findViewById(R.id.btn_update_EndDate);
        btn_confirm = dialog.findViewById(R.id.button_update);

        txt_taskname.setText(task.getTask_name());
        txt_estimaday.setText(String.valueOf(task.getEstimaday()));
        txt_assignee.setText(task.getAssignee());
        txt_startdate.setText(task.getStartdate());
        txt_enddate.setText(task.getEnddate());

        task.setTask_id(taskDAO.searchTaskIDByTaskName(task.getTask_name()));
        task.setDevtask_id(taskDAO.searchDevIDByDevName(task.getAssignee()));

        txt_taskname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                if(checkTaskName(txt_taskname)){
                    txt_taskname.setError("Task must not be similar to the task in the list");
                    txt_taskname.requestFocus();
                }

            }
        });
        txt_enddate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                String start = txt_startdate.getText().toString().trim();
                String end =txt_enddate.getText().toString().trim();
                int i = checkDateAndCalculateEstimatedays(start, end);
                if(i >= 0){
                    txt_estimaday.setText(String.valueOf(i));
                } else if (i == -1) {
                    txt_estimaday.setText(" ");
                    enddate_.setError("End Date must be higher than Start Date!");
                }
            }
        });

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txt_taskname.getText().toString().trim().isEmpty()) {
                    txt_taskname.setError("Task name is required");
                }
                task.setTask_name(txt_taskname.getText().toString().trim());
                task.setAssignee(txt_assignee.getText().toString().trim());
                task.setStartdate(txt_startdate.getText().toString().trim());
                task.setEnddate(txt_enddate.getText().toString().trim());
                task.setEstimaday(Integer.parseInt(txt_estimaday.getText().toString().trim()));
                int resultUpdateTask = taskDAO.updateTask(task);
                if(resultUpdateTask > 0 ){
                    loadTasks();
                    int resulUpdateAssingDev = taskDAO.updateAssignDev(task);
                    if(resulUpdateAssingDev > 0){
                        Toast.makeText(dialog.getContext(),"Task updated successfull",Toast.LENGTH_SHORT).show();
                        loadTasks();
                    }

                }else Toast.makeText(dialog.getContext(),"Failed to updated task",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        btnStartdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogDatepicker(txt_startdate);
            }
        });
        btnEnddate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogDatepicker(txt_enddate);
            }
        });
        // click vao ben ngoai de dong action
        dialog.setCanceledOnTouchOutside(true);
        //click de thoat action
        cancelButton = dialog.findViewById(R.id.cancelButton);
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
    //action add task
    private void showCreateBottomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.task_create_layout);

        TextView taskname_ = dialog.findViewById(R.id.textView_TaskName);
        TextView assginee_ = dialog.findViewById(R.id.textView_Asssigee);
        TextView enddate_ = dialog.findViewById(R.id.textView_endDate);
        TextView startdate_ = dialog.findViewById(R.id.textView_startDate);

        txt_taskname = dialog.findViewById(R.id.create_taskName);
        txt_assignee = dialog.findViewById(R.id.create_assignee);
        txt_estimaday = dialog.findViewById(R.id.create_estimateday);
        txt_enddate = dialog.findViewById(R.id.create_EndDate);
        txt_startdate = dialog.findViewById(R.id.create_startDate);

        btnStartdate = dialog.findViewById(R.id.btn_create_StartDate);
        btnEnddate = dialog.findViewById(R.id.btn_create_EndDate);
        btn_confirm = dialog.findViewById(R.id.button_create);

        txt_taskname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                if(checkTaskName(txt_taskname)){
                    txt_taskname.setError("Task must not be similar to the task in the list");
                    txt_taskname.requestFocus();
                }

            }
        });
        txt_enddate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                String start = txt_startdate.getText().toString().trim();
                String end =txt_enddate.getText().toString().trim();
                int i = checkDateAndCalculateEstimatedays(start, end);
                if(i >= 0){
                    txt_estimaday.setText(String.valueOf(i));
                } else if (i == -1) {
                    txt_estimaday.setText(" ");
                    enddate_.setError("End Date must be higher than Start Date!");
                }
            }
        });

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateInput(txt_taskname,taskname_) && validateInput(txt_assignee,assginee_) && validateInput(txt_startdate,startdate_) && validateInput(txt_enddate,enddate_)){
                    Task task = new Task();
                    task.setTask_name(txt_taskname.getText().toString().trim());
                    task.setEstimaday(Integer.parseInt(txt_estimaday.getText().toString().trim()));
                    task.setAssignee(txt_assignee.getText().toString().trim());
                    task.setStartdate(txt_enddate.getText().toString().trim());
                    task.setEnddate(txt_startdate.getText().toString().trim());

                    long resultTask = taskDAO.insertTask(task);
                    if(resultTask != -1 ){
                        loadTasks();
                        task.setTask_id(taskDAO.searchTaskIDByTaskName(task.getTask_name()));
                        long resultAssginDev = taskDAO.insertAssignDev(task);
                        if(resultAssginDev != -1){
                            Toast.makeText(dialog.getContext(), "Task created successfully", Toast.LENGTH_SHORT).show();
                            loadTasks();
                        }
                    }else Toast.makeText(dialog.getContext(), "Failed to create task", Toast.LENGTH_SHORT).show();
                }
             }
        });
        btnStartdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogDatepicker(txt_startdate);
            }

        });
        btnEnddate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateInput(txt_startdate,startdate_)){
                    showDialogDatepicker(txt_enddate);
                }
            }
        });

        // click vao ben ngoai de dong action
        dialog.setCanceledOnTouchOutside(true);
        //click de thoat action
        cancelButton = dialog.findViewById(R.id.cancelButton);
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
    //check task name
    private boolean checkTaskName(final EditText editText){
        for(Task task: taskList){
            if(task.getTask_name().equalsIgnoreCase(editText.getText().toString().trim())){
                return true;
            }
        }
        return false;
    }
    // kiem tra ngay va tinh estimate day
    private int checkDateAndCalculateEstimatedays(String startdate, String enddate){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd",Locale.getDefault());
        try{
            Date start = simpleDateFormat.parse(startdate);
            Date end = simpleDateFormat.parse(enddate);
            if(start != null && end != null){
                long time = end.getTime() - start.getTime();
                if(time >= 0){
                     return (int) TimeUnit.DAYS.convert(time,TimeUnit.MILLISECONDS);
                }
                else return -1;
            }
        }catch(ParseException e){
            e.printStackTrace();
        }

        return -2;
    }
    //datetpicker
    private void showDialogDatepicker(final EditText editText){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                editText.setText(year+"/"+(month+1)+"/"+dayOfMonth);
            }
        },year,month,dayOfMonth);
        datePickerDialog.show();
    }
    //kiem tra gia tri nhap vao
    private boolean validateInput(final EditText edittext, TextView textView){
        if(edittext.getText().toString().trim().isEmpty()){
            edittext.setError(textView.getText().toString() + " is required!");
            edittext.requestFocus();
            return false;
        }
        return true;
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