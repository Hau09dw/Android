package com.example.project_management_g1.UI;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.charts.Resource;
import com.anychart.enums.AvailabilityPeriod;
import com.anychart.enums.TimeTrackingMode;
import com.anychart.scales.calendar.Availability;
import com.example.project_management_g1.DATA.TaskDAO;
import com.example.project_management_g1.MODEL.Task;
import com.example.project_management_g1.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class GanttChartActivity extends AppCompatActivity {
    private List<Task> taskList;
    ImageButton btn_fromDate;
    ImageButton btn_toDate;
    EditText txt_fromDate;
    EditText txt_toDate;
    Button btn_filter,btn_hide;
    Resource gantt_chart;
    AnyChartView ganttChart;

    boolean hideDialog = false;
    Boolean isPlaying = false;
    private BackgroundMusicService musicService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gantt_chart_view);
        handleMusicBinding();
        ganttLogic();
    }

    private void handleMusicBinding() {
        // Bind to the service
        Intent intent = getIntent();
        isPlaying = intent.getBooleanExtra("music", false);
        bindService(new Intent(this, BackgroundMusicService.class), connection, Context.BIND_AUTO_CREATE);
    }

    public void ganttLogic() {
        //Initialize variables and get data from DB
        TaskDAO taskDAO = new TaskDAO(this);

        taskList = new ArrayList<Task>();
        taskList = taskDAO.getAllTasks();
        //sort by dev's name
        taskList.sort(Comparator.comparing(Task::getAssignee));
        //Begin to create Gantt Chart
        ganttChart = findViewById(R.id.any_chart_view);
        ganttChart.setProgressBar(findViewById(R.id.progress_bar));
        btn_fromDate = findViewById(R.id.btn_fromDate);
        btn_toDate = findViewById(R.id.btn_toDate);
        txt_fromDate = findViewById(R.id.txt_fromDate);
        txt_toDate = findViewById(R.id.txt_toDate);

        btn_filter = findViewById(R.id.btn_filter);
        btn_hide = findViewById(R.id.btn_hide);

        gantt_chart = AnyChart.resource();

        txt_fromDate.setFocusable(false);

        btn_fromDate.setOnClickListener(v -> {
            txt_fromDate.setFocusable(false);
            MainActivity.showDialogDatepicker(txt_fromDate);
        });
        btn_toDate.setOnClickListener(v -> {
            txt_toDate.setFocusable(false);
            MainActivity.showDialogDatepicker(txt_toDate);
        });
        btn_filter.setOnClickListener(v -> {
            filterGanttChart(txt_fromDate.getText().toString(), txt_toDate.getText().toString());
            List<DataEntry> filtered_data = new ArrayList<>();
            addDataToGanttChart(filtered_data);
            gantt_chart.data(filtered_data);
        });
        btn_hide.setOnClickListener(v -> {
            if(!hideDialog){
                findViewById(R.id.layout_from_date).setVisibility(View.GONE);
                findViewById(R.id.layout_to_date).setVisibility(View.GONE);
                btn_filter.setVisibility(View.GONE);
                btn_hide.setText(R.string.show);
                hideDialog = true;
            }
            else {
                findViewById(R.id.layout_from_date).setVisibility(View.VISIBLE);
                findViewById(R.id.layout_to_date).setVisibility(View.VISIBLE);
                btn_filter.setVisibility(View.VISIBLE);
                btn_hide.setText(R.string.hide);
                hideDialog = false;
            }
        });
        //gantt chart Setups
        ganttIniSetups(gantt_chart);
        //Initialize data for chart
        List<DataEntry> data = new ArrayList<>();

        //Add data to chart
        addDataToGanttChart(data);

        gantt_chart.data(data);
        gantt_chart.autoRedraw();
        ganttChart.setChart(gantt_chart);
    }

    private void ganttIniSetups(Resource ChartInstance) {
        ChartInstance.zoomLevel(1)
                .timeTrackingMode(TimeTrackingMode.AVAILABILITY_PER_CHART);
        ChartInstance.resourceListWidth(90);
        //to-from: maximum hours to work in a day, if exceeds, mark with red warning
        ChartInstance.calendar().availabilities(new Availability[] {
                new Availability(AvailabilityPeriod.DAY, (Double) null, 0, true, (Double) null, (Double) null,2),
                new Availability(AvailabilityPeriod.DAY, (Double) null, 14d, false, (Double) null, (Double) null, 15d),
                new Availability(AvailabilityPeriod.WEEK, (Double) null, (Double) null, false, 5d, (Double) null, 18d),
                new Availability(AvailabilityPeriod.WEEK, (Double) null, (Double) null, false, 6d, (Double) null, 18d)
        });
    }

    private void addDataToGanttChart(List<DataEntry> data) {
        //Loop through the list of tasks and add them to the chart
        for (Task task : taskList) {
            String dev = task.getAssignee();
            //Format the date's String to match the functions
            String startDate = formatDate(task.getStartdate());
            String endDate = formatDate(task.getEnddate());

            //Create variable to store the data for the chart
            ResourceDataEntry resData = new ResourceDataEntry(
                    dev,
                    "Developer",
                    new Activity[]{
                            new Activity(
                                    task.getTask_name(),
                                    new Interval[]{
                                            new Interval(startDate, endDate, 120)
                                    },
                                    "#62BEC1"
                            )
                    });
            //check if the dev is already in the data, if true, append the task to the dev's activities
            if(!data.isEmpty() && ((ResourceDataEntry)data.get(data.size() - 1)).getName().equals(dev)){
                int lastIndex = data.size() - 1;
                Activity[] tasks = (Activity[]) data.get(lastIndex).getValue("activities");
                tasks = Arrays.copyOf(tasks, tasks.length + 1);
                tasks[tasks.length - 1] = new Activity(
                        task.getTask_name(),
                        new Interval[]{
                                new Interval(startDate, endDate, 120)
                        },
                        "#62BEC1"
                );
                data.get(lastIndex).setValue("activities", tasks);
            }
            else
            {
                data.add(resData); //add if new data
            }

        }
    }

    public void exitGanttChart(View view) {
        this.finish();
    }

    public void txtDateInfo(View view) {
        Toast.makeText(this, R.string.click_text_warning, Toast.LENGTH_SHORT).show();
    }

    //name: Developer's name
    //description: Developer's description - Default to "Developer"
    //activities: list of activities/Timeline details (Can have many activities here)
    private class ResourceDataEntry extends DataEntry {
        ResourceDataEntry(String name, String description, Activity[] activities) {
            setValue("name", name);
            setValue("description", description);
            setValue("activities", activities);
        }

        String getName() {
            return (String) getValue("name");
        }
    }

    // name: Task's name
    // intervals: time intervals to do tasks
    // fill: color of the task
    private class Activity extends DataEntry {
        Activity(String name, Interval[] intervals, String fill) {
            setValue("name", name);
            setValue("intervals", intervals);
            setValue("fill", fill);
        }
    }

    //start: start date
    //end: end date
    //minutesPerDay: minutes per day - how long should you spend for that task - default to 120
    private class Interval extends DataEntry {
        Interval(String start, String end, Integer minutesPerDay) {
            setValue("start", start);
            setValue("end", end);
            setValue("minutesPerDay", minutesPerDay);
        }
    }


    //Author: MinhNT
    //filter list of tasks by date input from filter

    void filterGanttChart(String fromDate , String toDate) {
        Date dateFrom = convertStringToDate(fromDate);
        Date dateTo = convertStringToDate(toDate);

        if(!validateInput(dateFrom, dateTo) || taskList.isEmpty())
            return;
        if(!fromDate.isEmpty() && !toDate.isEmpty()){
            List<Task> copy =  new ArrayList<>(taskList);
            for (Task task : taskList) {
                Date startDate = convertStringToDate(task.getStartdate());
                Date endDate = convertStringToDate(task.getEnddate());
                if( startDate != null && endDate != null) {
                    if (startDate.compareTo(dateFrom) < 0 || endDate.compareTo(dateTo) > 0) {
                        copy.remove(task);
                    }
                }
            }
            taskList.clear();
            taskList.addAll(copy);
        }
    }

    private boolean validateInput(Date dateFrom, Date dateTo) {
        if(dateFrom == null || dateTo == null){
            Toast.makeText(this, "Null date or can't parse date", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(dateFrom.compareTo(dateTo) > 0){
            Toast.makeText(this, "Invalid date", Toast.LENGTH_LONG).show();
            txt_fromDate.setText("");
            txt_toDate.setText("");
            return false;
        }
        return true;
    }

    public static Date convertStringToDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            System.err.println("Error parsing date: " + e.getMessage());
            return null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    //Format the date's String to match the functions
    //ex: Input: 2024/02/27 Output: 2024-02-27
    private String formatDate(String date) {
        return date.replace("/", "-");
    }

    public ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BackgroundMusicService.LocalBinder binder = (BackgroundMusicService.LocalBinder) service;
            musicService = binder.getService();
            // Check saved state and play music if it was on
            if (isPlaying) {
                musicService.playMusic();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            if (isPlaying) {
                musicService.pauseMusic();
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if ( musicService.isPlaying()) {
            musicService.pauseMusic();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(musicService != null)
        {
            if (!musicService.isPlaying() && isPlaying) {
                musicService.playMusic();
            }
        }
    }
}

