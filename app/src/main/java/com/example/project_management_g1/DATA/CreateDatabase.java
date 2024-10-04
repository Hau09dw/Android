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
        super(context, "Project_Management_1", null, 1);
    }
    // datetime(TEXT) - Store date as a string in YYYY-MM-DD
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String tnDESTask = "CREATE TABLE " + TB_DesTask + "( "
                + TB_DesTask_DEVName + " TEXT, "
                + TB_DesTask_TaskID + " INTEGER, "
                + TB_DesTask_StartDate + " TEXT, "
                + TB_DesTasK_EndData + " TEXT );";
        String tbTask = "CREATE TABLE "+ TB_Task + "( "
                + TB_Task_TaskName + " TEXT, "
                + TB_Task_EstimateDay + " INTEGER );";
        String insertQuery1 = "INSERT INTO " + TB_DesTask + " (" + TB_DesTask_DEVName + ", " + TB_DesTask_TaskID + ", " + TB_DesTask_StartDate + ", " + TB_DesTasK_EndData + ") " +
                "VALUES('Ramesh', 3, '2024-5-1', '2024-5-3');";

        String insertQuery2 = "INSERT INTO " + TB_DesTask + " (" + TB_DesTask_DEVName + ", " + TB_DesTask_TaskID + ", " + TB_DesTask_StartDate + ", " + TB_DesTasK_EndData + ") " +
                "VALUES('Khilan', 2, '2024-5-2', '2024-5-4');";

        String insertQuery3 = "INSERT INTO " + TB_DesTask + " (" + TB_DesTask_DEVName + ", " + TB_DesTask_TaskID + ", " + TB_DesTask_StartDate + ", " + TB_DesTasK_EndData + ") " +
                "VALUES('Kaushik', 1, '2024-4-28', '2024-4-30');";

        String insertQuery4 = "INSERT INTO " + TB_DesTask + " (" + TB_DesTask_DEVName + ", " + TB_DesTask_TaskID + ", " + TB_DesTask_StartDate + ", " + TB_DesTasK_EndData + ") " +
                "VALUES('Kaushik', 4, '2024-5-6', '2024-5-8');";

        String insertQuery5 = "INSERT INTO " + TB_DesTask + " (" + TB_DesTask_DEVName + ", " + TB_DesTask_TaskID + ", " + TB_DesTask_StartDate + ", " + TB_DesTasK_EndData + ") " +
                "VALUES('Superman', 5, '2024-5-3', '2024-5-5');";

        String insertQuery6 = "INSERT INTO " + TB_Task + " (" + TB_Task_TaskName + ", " + TB_Task_EstimateDay + ") " +
                "VALUES('Order list', 5);";

        String insertQuery7 = "INSERT INTO " + TB_Task + " (" + TB_Task_TaskName + ", " + TB_Task_EstimateDay + ") " +
                "VALUES('Order detail', 3);";

        String insertQuery8 = "INSERT INTO " + TB_Task + " (" + TB_Task_TaskName + ", " + TB_Task_EstimateDay + ") " +
                "VALUES('Product list', 3);";

        String insertQuery9 = "INSERT INTO " + TB_Task + " (" + TB_Task_TaskName + ", " + TB_Task_EstimateDay + ") " +
                "VALUES('Product detail', 3);";

        String insertQuery10 = "INSERT INTO " + TB_Task + " (" + TB_Task_TaskName + ", " + TB_Task_EstimateDay + ") " +
                "VALUES('Coupon list', 3);";

        // Execute table creation and data insertion
        sqLiteDatabase.execSQL(tnDESTask);
        sqLiteDatabase.execSQL(tbTask);

        sqLiteDatabase.execSQL(insertQuery1);
        sqLiteDatabase.execSQL(insertQuery2);
        sqLiteDatabase.execSQL(insertQuery3);
        sqLiteDatabase.execSQL(insertQuery4);
        sqLiteDatabase.execSQL(insertQuery5);
        sqLiteDatabase.execSQL(insertQuery6);
        sqLiteDatabase.execSQL(insertQuery7);
        sqLiteDatabase.execSQL(insertQuery8);
        sqLiteDatabase.execSQL(insertQuery9);
        sqLiteDatabase.execSQL(insertQuery10);
    }

//    public void inserDevTask(String devname, int taskid, String startdate, String enddate){
//        String insertQuery = "INSERT INTO "+TB_DesTask+" ("+ TB_DesTask_DEVName+", "+TB_DesTask_TaskID+", "+TB_DesTask_StartDate+", "+TB_DesTasK_EndData+" ) " +
//                "VALUES('"+devname +"', '"+ taskid +"', '"+ startdate +"', '"+ enddate +"');";
//        sqLiteDatabase.execSQL(insertQuery);
//    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
    public SQLiteDatabase open(){
        return this.getWritableDatabase();
    }
}
