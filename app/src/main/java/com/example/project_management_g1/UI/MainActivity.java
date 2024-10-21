package com.example.project_management_g1.UI;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.example.project_management_g1.MODEL.ClickDebouncer;
import com.example.project_management_g1.MODEL.Task;
import com.example.project_management_g1.MODEL.Task_Adapter;
import com.example.project_management_g1.MODEL.setInputEstimateDayCreate;
import com.example.project_management_g1.MODEL.setInputEstimateDayUpdate;
import com.example.project_management_g1.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "app_preferences";
    private static final String KEY_SHOW_QUESTION = "show_question_dialog";
    private static final String KEY_SHOW_QUESTION_SELECT = "show_question_select_dialog";
    private static final String KEY_ESTIMATEDAY = "key_estimateday";
    private static final String KEY_SORT = "key_sort";
    public static final String MUSIC_STATE = "MusicState";
    private RecyclerView rcvTask;
    private Task_Adapter taskAdapter;
    private TaskDAO taskDAO;
    private List<Task> taskList;
    private  List<Task> assigneeList;
    private SearchView searchView;
    private FloatingActionButton fab;
    private ImageView cancelButton;
    private BackgroundMusicService musicService;
    private boolean isBound = false;
    private Switch musicSwitch;
    private BottomNavigationView bottomNavigationView;
    private boolean wasPlayingBeforePause = false;
    Switch estimateDaySwitch;
    EditText txt_taskname, txt_assignee, txt_estimaday, txt_startdate, txt_enddate;
    ImageButton btnStartdate, btnEnddate;
    Button btn_confirm;
    TextView estimadayy;
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

        initializeViews();
        try {
            loadTasks();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.an_error_occurred_while_loading_data), Toast.LENGTH_SHORT).show();
        }

        //Music Service
        Intent intent = new Intent(this, BackgroundMusicService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        //process  event click bottomappbar
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home_id) {
                return true;
            } else if (itemId == R.id.bottom_setting_id) {
                showSettingsDialog();
                return true;
            } else if (ClickDebouncer.isClickAllowed())
                    if (itemId == R.id.bottom_ganttchart_id) {
                        showGanttChart();
                        return true;
            } else if (itemId == R.id.bottom_sort_id) {
                SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                boolean currentSortState = preferences.getBoolean(KEY_SORT, true);
                boolean newSortState = !currentSortState;
                preferences.edit().putBoolean(KEY_SORT, newSortState).apply();
                loadTasks();
                return true;
            }
            return false;
        });
    }
    //khoi tao va anh xa
    private void initializeViews() {
        taskDAO = new TaskDAO(this);
        rcvTask = findViewById(R.id.id_recyclerview);
        estimadayy = findViewById(R.id.textView5);
        fab = findViewById(R.id.fab);
        searchView = findViewById(R.id.action_search);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fab.setOnClickListener(view -> {
            if (ClickDebouncer.isClickAllowed()) {
                showCreateBottomDialog();
            }
        });
        bottomNavigationView.setBackground(null);
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);

        preferences.edit().putBoolean(KEY_SORT,true).apply();
        preferences.edit().putBoolean(KEY_SHOW_QUESTION_SELECT,true).apply();
        preferences.edit().putBoolean(KEY_SHOW_QUESTION,true).apply();
        boolean isEstimateDay = preferences.getBoolean(KEY_ESTIMATEDAY,true);
        if(isEstimateDay){
            estimadayy.setVisibility(View.VISIBLE);
        }else estimadayy.setVisibility(View.INVISIBLE);
        setupRecyclerView();
        SearchTask();
        setupSwipeToDelete();

    }
    private void setupRecyclerView(){
        rcvTask.setLayoutManager(new LinearLayoutManager(this));
        rcvTask.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
    }
    private void loadTasks() {
        taskList = taskDAO.getAllTasks();
        sortTaskname();

        if (taskList.isEmpty()) {
            taskAdapter = new Task_Adapter(taskList);
            rcvTask.setAdapter(taskAdapter);
            Toast.makeText(this, "There is no data to display", Toast.LENGTH_SHORT).show();
        }else {

            taskAdapter = new Task_Adapter(taskList);
            rcvTask.setAdapter(taskAdapter);
            updateEstimateDayVisibility();
            //process event fab click
            taskAdapter.setOnSelectModeChangeListener(isSelectMode -> {
                if (isSelectMode) {
                    toggSelectionFab(2);// Fab delete(selectModeItems)
                } else {
                    toggSelectionFab(1);// Fab add
                }
            });
            //event update task
            taskAdapter.setOnItemClickListener(this::showUpdateBottomDialog);
        }

    }

    //event sort taskname
    private void sortTaskname(){
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isShort = preferences.getBoolean(KEY_SORT,true);
        if(isShort){
            Collections.sort(taskList,new sortByTaskName1());
        }else Collections.sort(taskList,new sortByTaskName2());
    }
    // xy ly sap xep
    private class sortByTaskName1 implements Comparator<Task> {
        @Override
        public int compare(Task task, Task task1) {
            return task.getTask_name().compareToIgnoreCase(task1.getTask_name()) ;
        }
    }
    private class sortByTaskName2 implements Comparator<Task> {
        @Override
        public int compare(Task task, Task task1) {
            return task1.getTask_name().compareToIgnoreCase(task.getTask_name()) ;
        }
    }
    // event delete task(selectModeItems)
    private void deleteSelectedTasks(){
        List<Task> selectedItems = taskAdapter.getSelectedItems();
        for (Task task : selectedItems) {
            taskDAO.deleteTask(task);
        }
        taskAdapter.clearSelection();
        loadTasks();
    }
    // xu ly thay doi event cho fab
    private void toggSelectionFab(int item){
        switch (item){
            case 1:
                fab.setBackgroundTintList(ColorStateList.valueOf(Color.CYAN));
                fab.setImageResource(R.drawable.floatadd);
                fab.setOnClickListener(view -> {
                    if (ClickDebouncer.isClickAllowed()) {
                        showCreateBottomDialog();
                    }
                });
                break;
            case 2:
                fab.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                fab.setImageResource(android.R.drawable.ic_menu_delete);
                fab.setOnClickListener(view -> {
                    if (ClickDebouncer.isClickAllowed()) {
                        showQuestionDelete(MainActivity.this,0,2);
                    }
                });
                break;
        }
    }

    //tim kiem theo task name hoac assignee
    private void SearchTask(){
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
    //loc theo task name hoac assignee
    private void filterTask(String text) {
        List<Task> filteredListTask = new ArrayList<>();
        for(Task task : taskList){
            if(task.getTask_name().toLowerCase().contains(text.toLowerCase()) || task.getAssignee().toLowerCase().contains(text.toLowerCase())){
                filteredListTask.add(task);
            }
        }
        if(filteredListTask.isEmpty()){
            Toast.makeText(this, getString(R.string.no_data_found),Toast.LENGTH_SHORT).show();
        }else{
            taskAdapter.setFilteredList(filteredListTask);
        }
    }

    // thiet lap keo cho event xoa
    private void setupSwipeToDelete() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT |  ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int i = viewHolder.getAdapterPosition();
                showQuestionDelete(MainActivity.this,i,1);

            }
        });
        itemTouchHelper.attachToRecyclerView(rcvTask);
    }
    //dialog question to delete task
    @SuppressLint("SetTextI18n")
    public void showQuestionDelete(Context context, int i, int item) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        boolean showQuestionDeleteswipe = preferences.getBoolean(KEY_SHOW_QUESTION,true);
        boolean showQuestionDeteleSelect = preferences.getBoolean(KEY_SHOW_QUESTION_SELECT,true);
        switch (item){
            case 1:
                if(!showQuestionDeleteswipe)
                {
                    deleteTask(i);
                    return;
                }
                break;
            case 2:
                if(!showQuestionDeteleSelect)
                {
                    deleteSelectedTasks();
                    return;
                }
                break;
        }

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.question_delete_dialog);

        Button btnDone = dialog.findViewById(R.id.btn_done);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        CheckBox checkBox = dialog.findViewById(R.id.checkbox_question);

        switch (item){
            case 1:
                btnDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(checkBox.isChecked())
                            preferences.edit().putBoolean(KEY_SHOW_QUESTION,false).apply();
                        deleteTask(i);
                        dialog.dismiss();
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loadTasks();
                        dialog.dismiss();
                    }
                });
                break;
            case 2:
                TextView txtContent = dialog.findViewById(R.id.txt_warning);
                ImageView imageView = dialog.findViewById(R.id.image_warning);
                TextView title_content = dialog.findViewById(R.id.title_warning);
                imageView.setImageResource(R.drawable.deleteicon);
                title_content.setText(R.string.delete_task);
                title_content.setTextColor(Color.RED);
                txtContent.setText(R.string.are_you_sure_you_want_to_delete_the_selected_tasks);
                btnDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(checkBox.isChecked())
                            preferences.edit().putBoolean(KEY_SHOW_QUESTION_SELECT,false).apply();
                        deleteSelectedTasks();
                        dialog.dismiss();
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loadTasks();
                        taskAdapter.clearSelection();
                        dialog.dismiss();
                    }
                });
                break;
        }

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
    }
    //event delete task(swiped)
    private void deleteTask(int i) {
        if(i != RecyclerView.NO_POSITION){
            Task task = taskList.get(i);
            taskDAO.deleteTask(task);
            loadTasks();
        } else
            Toast.makeText(MainActivity.this, getString(R.string.failed_to_delete_task), Toast.LENGTH_SHORT).show();
    }

    //event update task(CRUD)
    private void showUpdateBottomDialog(final Task task) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.task_update_layout);
        // click vao ben ngoai de dong action
        dialog.setCanceledOnTouchOutside(true);

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
        cancelButton = dialog.findViewById(R.id.cancelButton);

        txt_taskname.setText(task.getTask_name());
        txt_estimaday.setText(String.valueOf(task.getEstimaday()));
        txt_assignee.setText(task.getAssignee());
        txt_startdate.setText(task.getStartdate());
        txt_enddate.setText(task.getEnddate());

        task.setTask_id(taskDAO.searchTaskIDByTaskName(task.getTask_name()));
        task.setDevtask_id(taskDAO.searchDevIDByTaskId(task.getTask_id()));
        //constrain
        constrainStartAndEndDate(txt_startdate,txt_enddate,txt_estimaday,1);
        constrainStartAndEndDate(txt_enddate,txt_startdate,txt_estimaday,2);
        setInputEstimateDayUpdate.setNonNagativeIntegerInput(txt_estimaday, txt_startdate, txt_enddate);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateInput(txt_taskname,taskname_) && validateTaskName(txt_taskname,task) && validateInput(txt_assignee,assginee_) && validateInput(txt_startdate,startdate_) && validateInput(txt_enddate,enddate_)) {
                    task.setTask_name(txt_taskname.getText().toString().trim());
                    task.setAssignee(txt_assignee.getText().toString().trim());
                    task.setStartdate(txt_startdate.getText().toString().trim());
                    task.setEnddate(txt_enddate.getText().toString().trim());
                    task.setEstimaday(Integer.parseInt(txt_estimaday.getText().toString().trim()));
                    if (checkAssignee(txt_assignee, task)) {
                        if (checkOverLap(task)) {
                            showWarningDialog(dialog.getContext(), task.getTask_name());
                        }

                    }
                    int resultUpdateTask = taskDAO.updateTask(task);
                    if (resultUpdateTask > 0) {
                        loadTasks();
                        int resulUpdateAssingDev = taskDAO.updateAssignDev(task);
                        if (resulUpdateAssingDev > 0) {
                            //Toast.makeText(dialog.getContext(), "Task updated successfull", Toast.LENGTH_SHORT).show();
                            loadTasks();
                        }

                    } else
                        Toast.makeText(dialog.getContext(), getString(R.string.failed_to_updated_task), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
        btnStartdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txt_estimaday.setEnabled(false);
                showDialogDatepicker(txt_startdate);
            }
        });
        btnEnddate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txt_estimaday.setEnabled(false);
                showDialogDatepicker(txt_enddate);
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
    //event add task(CRUD)
    private void showCreateBottomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.task_create_layout);
        // click vao ben ngoai de dong action
        dialog.setCanceledOnTouchOutside(true);

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
        cancelButton = dialog.findViewById(R.id.cancelButton);

        Task task = new Task();
        //constrain
        txt_taskname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                validateTaskName(txt_taskname, task);
            }
        });
        constrainStartAndEndDate(txt_startdate, txt_enddate, txt_estimaday, 1);
        constrainStartAndEndDate(txt_enddate, txt_startdate, txt_estimaday, 2);
        setInputEstimateDayCreate.setNonNagativeIntegerInput(txt_estimaday, txt_startdate, txt_enddate);
        //xu ly button
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInput(txt_taskname, taskname_) && validateTaskName(txt_taskname, task) && validateInput(txt_assignee, assginee_) && validateInput(txt_startdate, startdate_) && validateInput(txt_enddate, enddate_)) {

                    task.setTask_name(txt_taskname.getText().toString().trim());
                    task.setEstimaday(Integer.parseInt(txt_estimaday.getText().toString().trim()));
                    task.setAssignee(txt_assignee.getText().toString().trim());
                    task.setStartdate(txt_startdate.getText().toString().trim());
                    task.setEnddate(txt_enddate.getText().toString().trim());
                    if (checkAssignee(txt_assignee, task)) {
                        if (checkOverLap(task)) {
                            showWarningDialog(dialog.getContext(), task.getTask_name());
                        }

                    }
                    long resultTask = taskDAO.insertTask(task);
                    if (resultTask != -1) {
                        loadTasks();
                        task.setTask_id(taskDAO.searchTaskIDByTaskName(task.getTask_name()));
                        long resultAssginDev = taskDAO.insertAssignDev(task);
                        if (resultAssginDev != -1) {
                            //Toast.makeText(dialog.getContext(), "Task created successfully", Toast.LENGTH_SHORT).show();
                            loadTasks();
                            dialog.dismiss();
                        }
                    } else
                        Toast.makeText(dialog.getContext(), getString(R.string.failed_to_create_task_2), Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(dialog.getContext(), getString(R.string.failed_to_create_task_1), Toast.LENGTH_SHORT).show();
            }
        });
         btnStartdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txt_estimaday.setEnabled(false);
                showDialogDatepicker(txt_startdate);
            }

        });
        btnEnddate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txt_estimaday.setEnabled(false);
                showDialogDatepicker(txt_enddate);
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

    //rang buoc startdate-enddate-estimateday
    private void constrainStartAndEndDate(EditText edittext1, EditText edittext2, EditText estimateday,int item){
        edittext1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                edittext1.setError(null);
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                String txt1 = edittext1.getText().toString().trim();
                String txt2 =edittext2.getText().toString().trim();
                int a = 0;
                if(!txt2.equalsIgnoreCase("")&& !estimateday.isEnabled()){
                switch (item){
                    case 1:
                         a = checkDateAndCalculateEstimatedays(txt1, txt2);
                    break;
                    case 2:
                         a = checkDateAndCalculateEstimatedays(txt2, txt1);
                     break;
                }
                if(a >= 0){
                estimateday.setText(String.valueOf(a));
                } else if (a == -1) {
                estimateday.setText("");
                edittext1.setText("");
                edittext1.setError("");
            }}
            }
        });
    }
    //check task name
    private boolean validateTaskName(EditText editText, Task currentTask) {
        String newTaskName = editText.getText().toString().trim();
        boolean isUpdating = (currentTask != null);

        for (Task task : taskList) {
            if (task.getTask_name().equalsIgnoreCase(newTaskName)) {
                if (!isUpdating || !task.equals(currentTask)) {
                    editText.setError(getString(R.string.task_name_already_exists_in_the_list));
                    editText.requestFocus();
                    return false;
                }
            }
        }

        return true;
    }
    //check assginee
    private boolean checkAssignee(final EditText editText, Task currentTask){
        boolean  isUpdating = (currentTask != null);
        assigneeList = new ArrayList<Task>();
        for(Task task : taskList){
            if(task.getAssignee().equalsIgnoreCase(editText.getText().toString().trim()))
                if(!isUpdating || !task.equals(currentTask)){
                    assigneeList.add(task);
                    return true;
                }
        }
        return false;
    }
    //setup warning dialog
    public void showWarningDialog(Context context, String taskName) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.warning_dialog);

        TextView txtContent = dialog.findViewById(R.id.txt_warning);
        Button btnDone = dialog.findViewById(R.id.btn_done);

        // Set up date time
        SimpleDateFormat outputDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String formatDateTime = outputDateTime.format(new Date());
        String txt1 = getString(R.string.task);
        String txt2 = getString(R.string.causes_an_overlap_to_other_tasks_when_updating_at);
        txtContent.setText(txt1+" " + taskName + txt2 + formatDateTime);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.show();
    }
    //check overlap
    public boolean checkOverLap(final Task task){


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        for(Task taskex : assigneeList){
            try{
                Date startcheck = simpleDateFormat.parse(task.getStartdate().trim());
                Date endcheck = simpleDateFormat.parse(task.getEnddate().trim());
                Date startdateonlist = simpleDateFormat.parse(taskex.getStartdate().trim());
                Date enddateonlist = simpleDateFormat.parse(taskex.getEnddate().trim());
                if(taskex != task && !(startcheck.after(enddateonlist) || endcheck.before(startdateonlist))){
                    assigneeList.clear();
                    return true;
                }

            }catch(ParseException e){
                e.printStackTrace();
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
                     return (int) TimeUnit.DAYS.convert(time,TimeUnit.MILLISECONDS)+1;
                }
                else return -1;
            }
            }catch(ParseException e){
            e.printStackTrace();
        }

        return -2;
    }
    //datetpicker
    public static void showDialogDatepicker(final EditText editText){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(editText.getContext(), new DatePickerDialog.OnDateSetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                // Define the desired date format
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

                String format_date = dateFormat.format(calendar.getTime());
                editText.setText(format_date);
            }
        },year,month,dayOfMonth);
        datePickerDialog.show();
    }
    //kiem tra gia tri nhap vao
    private boolean validateInput(final EditText edittext, TextView textView){
        String txt1 = getString(R.string.is_required);
        if(edittext.getText().toString().trim().isEmpty()){
            edittext.setError(textView.getText().toString() + txt1);
            edittext.requestFocus();
            return false;
        }
        return true;
    }

    // Settings Task
    public ServiceConnection connection = new ServiceConnection() {
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
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isEstimateDayVisible = preferences.getBoolean(KEY_ESTIMATEDAY, true);

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
        estimateDaySwitch = dialog.findViewById(R.id.estimateDaySwitch);
        estimateDaySwitch.setChecked(isEstimateDayVisible);
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
        cancelButton.setOnClickListener(view -> dialog.dismiss());
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
    public boolean getMusicState() {
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
        SharedPreferences estimate = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        return estimate.getBoolean(KEY_ESTIMATEDAY,true);
    }
    private void saveEstimateDayState(boolean state)
    {
        SharedPreferences estimate = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        estimate.edit().putBoolean(KEY_ESTIMATEDAY,state).apply();;
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
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        boolean isVisible = preferences.getBoolean(KEY_ESTIMATEDAY,true);
        if (taskAdapter != null) {
            taskAdapter.setShowEstimateDay(isVisible);

        }
        if(isVisible){
            estimadayy.setVisibility(View.VISIBLE);
        }else estimadayy.setVisibility(View.INVISIBLE);
    }

    // Helper method to get the view for a specific position
    private View getViewByPosition(int position, RecyclerView recyclerView) {
        return recyclerView.getLayoutManager().findViewByPosition(position);
    }
    private void showGanttChart() {
        Intent intent = new Intent(this, GanttChartActivity.class);
        intent.putExtra("music", getMusicState());
        startActivity(intent);
    }
}
