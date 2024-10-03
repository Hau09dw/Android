package com.example.project_management_g1.DATA;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class CreateDatabase extends SQLiteOpenHelper {
    public static String TB_DesTask = "DES_TASK";
    public static String TB_Task = "TASK";

    public static String TB_DesTask_DEVName = "DEV_NAME";
    public static String TB_DesTask_TaskID = "TASKID";
    public static String TB_DesTask_StartDate = "STARTDATE";
    public static String TB_DesTasK_EndData = "ENDDATE";

    public static String TB_Task_TaskName ="TASKNAME";
    public static String TB_Task_EstimateDay = "ESTIMATEDAY";

    public CreateDatabase(@Nullable Context context) {
        super(context, "Project_Management", null, 1);
    }
    // datetime(TEXT) - Store date as a string in YYYY-MM-DD
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String tnDESTask = "CREATE TABLE IF NOT EXISTS " + TB_DesTask + "( "
                + TB_DesTask_DEVName + " TEXT, "
                + TB_DesTask_TaskID + " INTEGER, "
                + TB_DesTask_StartDate + " TEXT, "
                + TB_DesTasK_EndData + " TEXT );";
        String tbTask = "CREATE TABLE IF NOT EXISTS "+ TB_Task + "( "
                + TB_Task_TaskName + " TEXT, "
                + TB_Task_EstimateDay + " TEXT );";
        sqLiteDatabase.execSQL(tnDESTask);
        sqLiteDatabase.execSQL(tbTask);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
    public SQLiteDatabase open(){
        return this.getWritableDatabase();
    }
}
