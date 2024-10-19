package com.example.project_management_g1.MODEL;

import android.annotation.SuppressLint;
import android.app.StartForegroundCalledOnStoppedServiceException;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_management_g1.DATA.TaskDAO;
import com.example.project_management_g1.R;

import java.util.ArrayList;
import java.util.List;

public class Task_Adapter  extends RecyclerView.Adapter<Task_Adapter.TaskViewHolder>{
    private List<Task> taskList;
    private OnItemClickListener listener;
    boolean isSelectMode = false;
    List<Task> selectModeItems = new ArrayList<>();
    private OnSelectModeChangeListener selectModeChangeListener;
    private boolean showEstimateDay;

    public interface OnItemClickListener{
        void onItemClick(Task item);
    }
    public interface OnSelectModeChangeListener {
        void onSelectModeChanged(boolean isSelectMode);
    }
    public void setOnSelectModeChangeListener(OnSelectModeChangeListener listener) {
        this.selectModeChangeListener = listener;
    }

    public List<Task> getSelectedItems() {
        return new ArrayList<>(selectModeItems);
    }
    public void clearSelection() {
        selectModeItems.clear();
        isSelectMode = false;
        notifyDataSetChanged();
        if (selectModeChangeListener != null) {
            selectModeChangeListener.onSelectModeChanged(false);  // Báo về MainActivity
        }
    }
    public Task_Adapter(List<Task> tasklist){
        this.taskList = tasklist;
        this.showEstimateDay = true;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public void setFilteredList(List<Task> filteredList){
        this.taskList = filteredList;
        notifyDataSetChanged();
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
        Task task = taskList.get(position);
        if(task == null) return;
        holder.taskname_.setText(task.getTask_name());
        holder.assignee_.setText(task.getAssignee());
        holder.estimateday_.setText(task.getEstimaday() + " days");
        holder.startdate_.setText(task.getStartdate());
        holder.enddate_.setText(task.getEnddate());
        holder.estimateday_.setVisibility(showEstimateDay ? View.VISIBLE : View.INVISIBLE);
    }

    public void setShowEstimateDay(boolean show) {
        this.showEstimateDay = show;
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        if(taskList != null)
            return taskList.size();
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
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    isSelectMode = true;
                    if (selectModeChangeListener != null) {
                        selectModeChangeListener.onSelectModeChanged(true);
                    }
                    SelectModeClick();
                    return true;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isSelectMode) {
                        SelectModeClick();
                    } else if (listener != null) {
                        listener.onItemClick(taskList.get(getAdapterPosition()));
                    }
                }
            });

        }
        private void  SelectModeClick() {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            Task task = taskList.get(position);

            if (selectModeItems.contains(task)) {
                selectModeItems.remove(task);
                itemView.setBackgroundColor(Color.TRANSPARENT);
            } else {
                selectModeItems.add(task);
                itemView.setBackgroundColor(Color.LTGRAY);
            }

            // Nếu không còn item nào được chọn, tắt chế độ chọn
            if (selectModeItems.isEmpty() && selectModeChangeListener != null) {
                isSelectMode = false;
                selectModeChangeListener.onSelectModeChanged(false);
            }

        }
    }
}
