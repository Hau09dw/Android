package com.example.project_management_g1.DATA;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class CreateDatabase extends SQLiteOpenHelper {
    public static String TB_DevTask = "DEV_TASK";
    public static String TB_Task = "TASK";

    public static String TB_DevTask_Id = "ID_DEVTASK";
    public static String TB_DevTask_DEVName = "DEV_NAME";
    public static String TB_DevTask_TaskID = "TASKID";
    public static String TB_DevTask_StartDate = "STARTDATE";
    public static String TB_DevTask_EndDate = "ENDDATE";

    public static String TB_Task_ID = "ID_TASK";
    public static String TB_Task_TaskName ="TASKNAME";
    public static String TB_Task_EstimateDay = "ESTIMATEDAY";

    public CreateDatabase(@Nullable Context context) {
        super(context, "Project_Management", null, 1);
    }
    // datetime(TEXT) - Store date as a string in YYYY-MM-DD
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String tnDESTask = "CREATE TABLE " + TB_DevTask + "( "
                + TB_DevTask_Id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TB_DevTask_DEVName + " TEXT, "
                + TB_DevTask_TaskID + " INTEGER, "
                + TB_DevTask_StartDate + " DATETIME, "
                + TB_DevTask_EndDate + " DATETIME );";
        String tbTask = "CREATE TABLE "+ TB_Task + "( "
                + TB_Task_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TB_Task_TaskName + " TEXT, "
                + TB_Task_EstimateDay + " INTEGER );";
        String insertQuery1 = "INSERT INTO " + TB_DevTask + " (" + TB_DevTask_DEVName + ", " + TB_DevTask_TaskID + ", " + TB_DevTask_StartDate + ", " + TB_DevTask_EndDate + ") " +
                "VALUES('Ramesh', 3, '2024-5-1', '2024-5-3');";

//        String insertQuery2 = "INSERT INTO " + TB_DevTask + " (" + TB_DevTask_DEVName + ", " + TB_DevTask_TaskID + ", " + TB_DevTask_StartDate + ", " + TB_DevTask_EndDate + ") " +
//                "VALUES('Khilan', 2, '2024-5-2', '2024-5-4');";
//
//        String insertQuery3 = "INSERT INTO " + TB_DevTask + " (" + TB_DevTask_DEVName + ", " + TB_DevTask_TaskID + ", " + TB_DevTask_StartDate + ", " + TB_DevTask_EndDate + ") " +
//                "VALUES('Kaushik', 1, '2024-4-28', '2024-4-30');";
//
//        String insertQuery4 = "INSERT INTO " + TB_DevTask + " (" + TB_DevTask_DEVName + ", " + TB_DevTask_TaskID + ", " + TB_DevTask_StartDate + ", " + TB_DevTask_EndDate + ") " +
//                "VALUES('Kaushik', 4, '2024-5-6', '2024-5-8');";
//
//        String insertQuery5 = "INSERT INTO " + TB_DevTask + " (" + TB_DevTask_DEVName + ", " + TB_DevTask_TaskID + ", " + TB_DevTask_StartDate + ", " + TB_DevTask_EndDate + ") " +
//                "VALUES('Superman', 5, '2024-5-3', '2024-5-5');";
//
        String insertQuery6 = "INSERT INTO " + TB_Task + " (" + TB_Task_TaskName + ", " + TB_Task_EstimateDay + ") " +
                "VALUES('Order list', 5);";

        String insertQuery7 = "INSERT INTO " + TB_Task + " (" + TB_Task_TaskName + ", " + TB_Task_EstimateDay + ") " +
                "VALUES('Order detail', 3);";

        String insertQuery8 = "INSERT INTO " + TB_Task + " (" + TB_Task_TaskName + ", " + TB_Task_EstimateDay + ") " +
                "VALUES('Product list',null );";
//
//        String insertQuery9 = "INSERT INTO " + TB_Task + " (" + TB_Task_TaskName + ", " + TB_Task_EstimateDay + ") " +
//                "VALUES('Product detail', 3);";
//
//        String insertQuery10 = "INSERT INTO " + TB_Task + " (" + TB_Task_TaskName + ", " + TB_Task_EstimateDay + ") " +
//                "VALUES('Coupon list', 3);";

        // Execute table creation and data insertion
        sqLiteDatabase.execSQL(tnDESTask);
        sqLiteDatabase.execSQL(tbTask);

        sqLiteDatabase.execSQL(insertQuery1);
//        sqLiteDatabase.execSQL(insertQuery2);
//        sqLiteDatabase.execSQL(insertQuery3);
//        sqLiteDatabase.execSQL(insertQuery4);
//        sqLiteDatabase.execSQL(insertQuery5);
        sqLiteDatabase.execSQL(insertQuery6);
        sqLiteDatabase.execSQL(insertQuery7);
        sqLiteDatabase.execSQL(insertQuery8);
//        sqLiteDatabase.execSQL(insertQuery9);
//        sqLiteDatabase.execSQL(insertQuery10);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TB_DevTask);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TB_Task);
    }
    public SQLiteDatabase open(){
        return this.getWritableDatabase();
    }

}
