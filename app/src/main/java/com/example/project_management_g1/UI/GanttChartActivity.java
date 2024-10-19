package com.example.project_management_g1.UI;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gantt_chart_view);
        //Initialize variables and get data from DB
        TaskDAO taskDAO = new TaskDAO(this);

        taskList = new ArrayList<Task>();
        taskList = taskDAO.getAllTasks();
        //sort by dev's name
        taskList.sort(Comparator.comparing(Task::getAssignee));
        //Begin to create Gantt Chart
        AnyChartView ganttChart = findViewById(R.id.any_chart_view);
        ganttChart.setProgressBar(findViewById(R.id.progress_bar));
        btn_fromDate = findViewById(R.id.btn_fromDate);
        btn_toDate = findViewById(R.id.btn_toDate);
        txt_fromDate = findViewById(R.id.txt_fromDate);
        txt_toDate = findViewById(R.id.txt_toDate);
        Resource gantt_chart = AnyChart.resource();

        //gantt chart Setups
        gantt_chart.zoomLevel(1d)
                .timeTrackingMode(TimeTrackingMode.AVAILABILITY_PER_CHART);

        gantt_chart.resourceListWidth(120);

        gantt_chart.calendar().availabilities(new Availability[] {
                new Availability(AvailabilityPeriod.DAY, (Double) null, 10d, true, (Double) null, (Double) null, 18d),
                new Availability(AvailabilityPeriod.DAY, (Double) null, 14d, false, (Double) null, (Double) null, 15d),
                new Availability(AvailabilityPeriod.WEEK, (Double) null, (Double) null, false, 5d, (Double) null, 18d),
                new Availability(AvailabilityPeriod.WEEK, (Double) null, (Double) null, false, 6d, (Double) null, 18d)
        });
        //Initialize data for chart
        List<DataEntry> data = new ArrayList<>();

        //Add data to chart
        addDataToGanttChart(data);

        gantt_chart.data(data);

        ganttChart.setChart(gantt_chart);
    }

    private void addDataToGanttChart(List<DataEntry> data) {
        //Loop through the list of tasks and add them to the chart
        for (Task task : taskList) {
            String dev = task.getAssignee();
            //Format the dates's String to match the functions
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

        if(!fromDate.isEmpty() && !toDate.isEmpty()){
            if(taskList.size()<=0)
                return;
            for (Task task : taskList) {
                Date startDate = convertStringToDate(task.getStartdate());
                Date endDate = convertStringToDate(task.getEnddate());
                if( startDate != null && endDate != null) {
                    if (startDate.compareTo(dateFrom) < 0 || endDate.compareTo(dateTo) > 0) {
                        taskList.remove(task);
                    }
                }
            }
        }
        return;
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

    //Format the dates's String to match the functions
    //ex: Input: 2024/02/27 Output: 2024-02-27
    private String formatDate(String date) {
        return date.replace("/", "-");
    }
}

