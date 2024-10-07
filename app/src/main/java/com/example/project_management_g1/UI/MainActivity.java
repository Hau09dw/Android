package com.example.project_management_g1.UI;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_management_g1.DATA.CreateDatabase;
import com.example.project_management_g1.DATA.TaskDAO;
import com.example.project_management_g1.MODEL.Task;
import com.example.project_management_g1.MODEL.Task_Adapter;
import com.example.project_management_g1.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rcvTask;
    private Task_Adapter taskAdapter;
    private TaskDAO taskDAO;
    private List<Task> taskList;
    private SearchView searchView;
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

    private void initializeViews() {

        rcvTask = findViewById(R.id.id_recyclerview);
        //2 dong duoi chua hoat dong nhung chua biet li do(dang fix)
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvTask.setLayoutManager(linearLayoutManager);
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
    private void filterTask(String text) {
        List<Task> filteredListIask = new ArrayList<>();
        //loc tassk name hoac assignee
        for(Task task : taskList){
            if(task.getTask_name().toLowerCase().contains(text.toLowerCase()) || task.getAssignee().toLowerCase().contains(text.toLowerCase())){
                filteredListIask.add(task);
            }
        }
        if(filteredListIask.isEmpty()){
            Toast.makeText(this,"No data found",Toast.LENGTH_SHORT).show();
        }else{
            taskAdapter.setFilteredList(filteredListIask);
        }
    }
}