package com.example.project_management_g1.UI;

import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_management_g1.DATA.TaskDAO;
import com.example.project_management_g1.MODEL.Task;
import com.example.project_management_g1.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class GanttChartActivity extends AppCompatActivity {
    private BarChart mBarChart;
    private TaskDAO taskDAO;
    private List<Task> taskList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gantt_chart_view);

        mBarChart = findViewById(R.id.gantt_chart);
        taskDAO = new TaskDAO(this);
        List<Task> tasks = taskDAO.getAllTasks();

        try {
            List<BarEntry> entries = getBarEntries(tasks);
            BarDataSet dataSet = new BarDataSet(entries, "Tasks");
            dataSet.setColor(Color.BLUE);

            BarData barData = new BarData(dataSet);
            mBarChart.setData(barData);
            mBarChart.invalidate();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


    }

    private List<BarEntry> getBarEntries(List<Task> tasks) throws ParseException {
        List<BarEntry> entries = new ArrayList<>();
        int i = 1;
        for (Task task : tasks) {
            // Calculate the duration in days (adjust as needed)
            float duration = 0;
            duration = (float) (stringToDateTime(task.getEnddate(), "dd/MM/yyyy").getTime() - stringToDateTime(task.getStartdate(), "dd/MM/yyyy").getTime()) / (24 * 60 * 60 * 1000);
            entries.add(new BarEntry(i, duration));
            i++;
        }
        return entries;
    }

    public Date stringToDateTime(String dateString, String format) throws ParseException
    {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.parse(dateString);
    }
}
