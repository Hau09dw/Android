package com.example.project_management_g1.MODEL;

import android.app.StartForegroundCalledOnStoppedServiceException;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_management_g1.DATA.TaskDAO;
import com.example.project_management_g1.R;

import java.util.List;

public class Task_Adapter  extends RecyclerView.Adapter<Task_Adapter.TaskViewHolder>{
    private List<Task> tasklist;

    public Task_Adapter(List<Task> tasklist){
        this.tasklist = tasklist;
    }
    // tao view
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project,parent,false);
        return new TaskViewHolder(view);
    }
    // gan du lieu
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasklist.get(position);
        if(task == null) return;
        holder.taskname_.setText(task.getTask_name());
        holder.assignee_.setText(task.getAssignee());
        holder.estimateday_.setText(String.valueOf(task.getEstimaday())+ " days");
        holder.startdate_.setText(task.getStartdate());
        holder.enddate_.setText(task.getEnddate());
        }

    @Override
    public int getItemCount() {
        if(tasklist != null)
            return tasklist.size();
        return 0;
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder{
        private TextView taskname_, assignee_, estimateday_, startdate_, enddate_;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskname_ = itemView.findViewById(R.id.item_taskname);
            assignee_ = itemView.findViewById(R.id.item_assignee);
            estimateday_ = itemView.findViewById(R.id.item_estimateday);
            startdate_ = itemView.findViewById(R.id.item_startdate);
            enddate_ = itemView.findViewById(R.id.item_enddate);
        }
    }
}
