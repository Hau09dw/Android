package com.example.project_management_g1.MODEL;

public class Task {
    private String task_name;
    private String assignee;
    private int estimaday;
    private String startdate;
    private String enddate;

    public Task(String assignee, String task_name, int estimaday, String startdate, String enddate) {
        this.assignee = assignee;
        this.task_name = task_name;
        this.estimaday = estimaday;
        this.startdate = startdate;
        this.enddate = enddate;
    }

    public Task() {
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getTask_name() {
        return task_name;
    }

    public void setTask_name(String task_name) {
        this.task_name = task_name;
    }

    public int getEstimaday() {
        return estimaday;
    }

    public void setEstimaday(int estimaday) {
        this.estimaday = estimaday;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }


}
