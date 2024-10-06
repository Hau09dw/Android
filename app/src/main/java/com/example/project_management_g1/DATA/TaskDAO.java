package com.example.project_management_g1.DATA;

import android.annotation.SuppressLint;
import android.content.Context;
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
