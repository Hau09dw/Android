package com.example.project_management_g1.UI;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
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


    TextView txt_startdate, txt_enddate;
    ImageButton btnStartdate, btnEnddate;
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
                showBottomDialog();
            }
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
}