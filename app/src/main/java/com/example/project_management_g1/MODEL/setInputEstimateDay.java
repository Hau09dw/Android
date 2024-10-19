package com.example.project_management_g1.MODEL;

import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.widget.EditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class setInputEstimateDay {
    public static void setNonNagativeIntegerInput(EditText estimateDay,EditText startDate, EditText endDate){
        estimateDay.setFilters(new InputFilter[]{
                 new InputFilter() {
                     @Override
                     public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                         // Result String
                         StringBuilder builder = new StringBuilder();
                         for (int i = start; i < end; i++) {
                             char c = source.charAt(i);
                             if (Character.isDigit(c)) {
                                 builder.append(c);
                             }
                         }
                         // toan bo ki tu nhap vao la so-> exit the filter
                         if (builder.length() == end - start) {
                             return null;
                         }
                        return  builder.length() == 0 ? "" : builder.toString(); // ket qua sau khi loc
                     }
                 }
        });
        estimateDay.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

             }

             @Override
             public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

             }

             @Override
             public void afterTextChanged(Editable editable) {
                    String txt = editable.toString().trim() ;
                    if(txt.length()>4){
                        editable.delete(4,txt.length());
                        txt = editable.toString();
                    }
                    while(txt.startsWith("0")){
                        editable.delete(0,1);
                        txt = editable.toString();
                    }
                    if(!txt.isEmpty() && estimateDay.isEnabled()) {
                        int i = Integer.parseInt(txt);

                        set_SE_DatePicker(i,startDate,endDate);
                    }else if(txt.isEmpty() && estimateDay.isEnabled()){
                        startDate.setText("");
                        endDate.setText("");
                    }
             }
         });
    }
    private static void set_SE_DatePicker(int i,EditText start, EditText end){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        try{
            //lay ngay hien tai cho startDate
            Date currentDate = new Date();
            start.setText(dateFormat.format(currentDate));
            //tinh toan endDate = startDate + estimateDay - 1
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            calendar.add(calendar.DAY_OF_YEAR,i-1);
            //set endDate
            Date enddate = calendar.getTime();
            end.setText(dateFormat.format(enddate));
        }catch(Exception  e){
            e.printStackTrace();
        }
    }
}
