package com.example.project_management_g1.MODEL;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project_management_g1.R;
import java.util.ArrayList;
import java.util.List;

public class Task_Adapter  extends RecyclerView.Adapter<Task_Adapter.TaskViewHolder>{
    private List<Task> taskList;
    List<Task> selectModeItems = new ArrayList<>();
    private OnItemClickListener listener;
    private OnSelectModeChangeListener selectModeChangeListener;
    private boolean showEstimateDay;
    private boolean isSelectMode = false;

    public interface OnItemClickListener{
        void onItemClick(Task item);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    // call back toi main: xem lua chon fab nao
    public interface OnSelectModeChangeListener {
        void onSelectModeChanged(boolean isSelectMode);
    }
    public void setOnSelectModeChangeListener(OnSelectModeChangeListener listener) {
        this.selectModeChangeListener = listener;
    }
    // clear selectionMode
    @SuppressLint("NotifyDataSetChanged")
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

    public List<Task> getSelectedItems() {
        return new ArrayList<>(selectModeItems);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFilteredList(List<Task> filteredList){
        this.taskList = filteredList;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setShowEstimateDay(boolean show) {
        this.showEstimateDay = show;
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
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        if(task == null) return;
        holder.taskname_.setText(task.getTask_name());
        holder.assignee_.setText(task.getAssignee());
        holder.estimateday_.setText(task.getEstimaday() + " days");
        holder.startdate_.setText(task.getStartdate());
        holder.enddate_.setText(task.getEnddate());
        //kiem tra xem có hien thi estimateday khong
        holder.estimateday_.setVisibility(showEstimateDay ? View.VISIBLE : View.INVISIBLE);
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
            itemView.setOnClickListener(v -> {
                if (ClickDebouncer.isClickAllowed()) {
                    if (isSelectMode) {
                        SelectModeClick();
                    } else if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(taskList.get(position));
                        }
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
