package com.example.project_management_g1.DATA;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.credentials.CreateCredentialRequest;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.project_management_g1.MODEL.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
    private SQLiteDatabase db;
    private CreateDatabase dbHelper;

    public TaskDAO(Context context) {
        dbHelper = new CreateDatabase(context);// khoi tao csdl
    }

    //tìm id từ name
    @SuppressLint("Range")
    public int searchTaskIDByTaskName(String taskname) {
        int ID = -1;
        String searchQuery = "SELECT " + CreateDatabase.TB_Task_ID
                            + " FROM " + CreateDatabase.TB_Task
                            + " WHERE " + CreateDatabase.TB_Task_TaskName + " = ?";
        Cursor cursor = db.rawQuery(searchQuery, new String[]{taskname});
        // Check if a record was found
        if (cursor.moveToFirst()) {
            ID = cursor.getInt(cursor.getColumnIndex(CreateDatabase.TB_Task_ID));
        }
        cursor.close();
        return ID;
    }
    @SuppressLint("Range")
    public int searchDevIDByDevName(String devname) {
        int ID = -1;
        String searchQuery = "SELECT " + CreateDatabase.TB_DevTask_Id
                            + " FROM " + CreateDatabase.TB_DevTask
                            + " WHERE " + CreateDatabase.TB_DevTask_DEVName + " = ?";
        Cursor cursor = db.rawQuery(searchQuery, new String[]{devname});
        // Check if a record was found
        if (cursor.moveToFirst()) {
            ID = cursor.getInt(cursor.getColumnIndex(CreateDatabase.TB_DevTask_Id));
        }
        cursor.close();
        return ID;
    }

    //add task
    public long insertTask(Task task){
        ContentValues values = new ContentValues();
        values.put(CreateDatabase.TB_Task_TaskName,task.getTask_name());
        values.put(CreateDatabase.TB_Task_EstimateDay,task.getEstimaday());
        long id = db.insert(CreateDatabase.TB_Task, null, values);
        db.close();
        return id;
    }
    public long insertAssignDev(Task task){
        ContentValues values = new ContentValues();
        values.put(CreateDatabase.TB_DevTask_DEVName,task.getAssignee());
        values.put(CreateDatabase.TB_DevTask_TaskID,task.getTask_id());
        values.put(CreateDatabase.TB_DevTask_StartDate,task.getStartdate());
        values.put(CreateDatabase.TB_DevTask_EndDate,task.getEnddate());
        long id = db.insert(CreateDatabase.TB_DevTask, null, values);
        db.close();
        return id;
    }
    //update task
    public int updateTask(Task task){
        ContentValues values = new ContentValues();
        values.put(CreateDatabase.TB_Task_TaskName,task.getTask_name());
        values.put(CreateDatabase.TB_Task_EstimateDay, task.getEstimaday());
        int id = db.update(CreateDatabase.TB_Task, values,CreateDatabase.TB_Task_ID +"=?", new String[]{String.valueOf(task.getTask_id())});
        db.close();
        return id;
    }
    public int updateAssignDev(Task task){
        ContentValues values = new ContentValues();
        values.put(CreateDatabase.TB_DevTask_DEVName,task.getAssignee());
        values.put(CreateDatabase.TB_DevTask_TaskID,task.getTask_id());
        values.put(CreateDatabase.TB_DevTask_StartDate,task.getStartdate());
        values.put(CreateDatabase.TB_DevTask_EndDate,task.getEnddate());
        int id = db.update(CreateDatabase.TB_DevTask, values, CreateDatabase.TB_DevTask_Id+"=?", new String[]{String.valueOf(task.getDevtask_id())});
        db.close();
        return id;
    }
    //delete task
    public void deleteTask(Task task){
        int iddevtask = searchDevIDByDevName(task.getAssignee());
        int idtask = searchTaskIDByTaskName(task.getTask_name());
        db.delete(CreateDatabase.TB_DevTask,CreateDatabase.TB_DevTask_Id+"=?",new String[]{String.valueOf(iddevtask)});
        db.delete(CreateDatabase.TB_Task,CreateDatabase.TB_Task_ID+"=?",new String[]{String.valueOf(idtask)});
    }
    //get all tasks
    public List<Task> getAllTasks() {
        List<Task> listTasks = new ArrayList<>();
        db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT d." + CreateDatabase.TB_DevTask_DEVName + ", "
                    + "t." + CreateDatabase.TB_Task_TaskName + ", "
                    + "t." + CreateDatabase.TB_Task_EstimateDay + ", "
                    + "d." + CreateDatabase.TB_DevTask_StartDate + ", "
                    + "d." + CreateDatabase.TB_DevTask_EndDate
                    + " FROM " + CreateDatabase.TB_DevTask + " d "
                    + "INNER JOIN " + CreateDatabase.TB_Task + " t "
                    + "ON d." + CreateDatabase.TB_DevTask_TaskID + " = t." + CreateDatabase.TB_Task_ID;

            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Task task = new Task(
                            cursor.getString(0),  // DEV_NAME
                            cursor.getString(1),  // TASKNAME
                            cursor.getInt(2),     // ESTIMATEDAY
                            cursor.getString(3),  // STARTDATE
                            cursor.getString(4)   // ENDDATE
                    );
                    listTasks.add(task);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return listTasks;
    }

}
