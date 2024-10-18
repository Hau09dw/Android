package com.example.project_management_g1.UI;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class GanttChartActivity extends Fragment {
    private AnyChartView mGanttChart;
    private ProgressBar mProgressBar;
    private TaskDAO taskDAO;
    private List<Task> taskList;


    public GanttChartActivity() {
        super(R.layout.gantt_chart_view);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.gantt_chart_view, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize variables and get data from DB
        taskDAO = new TaskDAO(this.getContext());

        taskList = new ArrayList<Task>();
        taskList = taskDAO.getAllTasks();
        //sort by dev's name
        taskList.sort(Comparator.comparing(Task::getAssignee));
        //Begin to create Gantt Chart
        mGanttChart = mGanttChart.findViewById(R.id.any_chart_view);
        mGanttChart.setProgressBar(mProgressBar.findViewById(R.id.progress_bar));
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

        mGanttChart.setChart(gantt_chart);
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



    //Format the dates's String to match the functions
    //ex: Input: 2024/02/27 Output: 2024-02-27
    private String formatDate(String date) {
        return date.replace("/", "-");
    }
}

