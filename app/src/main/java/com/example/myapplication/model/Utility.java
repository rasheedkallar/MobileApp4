package com.example.myapplication.model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.myapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import kotlin.text.UStringsKt;

public class Utility {
    private static void selectRow(TableRow row, TableRow header, TableLayout tableLayout) {
        row.setBackgroundColor(Color.parseColor("#C5C6C6"));

        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            TableRow otherRow = (TableRow)tableLayout.getChildAt(i);
            if(row != otherRow && header != otherRow){
                otherRow.setBackgroundResource(android.R.color.transparent);
            }
        }
    }

    public  static abstract  class  onGridListener  {
        public abstract boolean onRowSelected(TableRow row, JSONObject data);

        public  boolean isSelected(TableRow row, JSONObject data){
            return  false;
        }
    }


    /*
    public static void applyValues(JSONObject value, ArrayList<Control.ControlBase> controls) throws JSONException {
        for (Iterator<String> it = value.keys(); it.hasNext(); ) {
            String key = it.next();
            Optional<Control.ControlBase> control = controls.stream().filter(i->i.getName().equals(key)).findAny();
            if(control.isPresent()){
                control.get().readValue(value.get(key));
            }
        }
    }
    */

/*
    public static   void CreateGrid(TableLayout table,String idField,Long selectedValue ,List<Control.ControlBase> controls, JSONArray list,onGridListener listener) throws JSONException, ParseException {

        if (table == null) {
            table = new TableLayout(table.getContext());
            TableLayout.LayoutParams tableP = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            table.setLayoutParams(tableP);
        }
        table.removeAllViews();

        final TableRow header = new TableRow(table.getContext());
        table.addView(header);
        TableLayout.LayoutParams headerP = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        header.setLayoutParams(headerP);
        header.setBackgroundColor(Color.parseColor("#054678"));
        header.setPadding(5, 5, 5, 5);
        final TableLayout parentTable = table;
        for (com.example.myapplication.model.Control.ControlBase control : controls) {
            float weight = 1f;
            if (control.getControlSize() < -1) weight = 2f;
            TextView hc = new TextView(table.getContext());
            TableRow.LayoutParams hcP = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
            hc.setLayoutParams(hcP);
            hc.setText(control.getCaption());
            hc.setTextColor(Color.parseColor("#C3DEF3"));
            header.addView(hc);
        }

        for (int i = 0; i < list.length(); i++) {
            JSONObject obj = (JSONObject) list.get(i);
            TableRow item = new TableRow(table.getContext());
            table.addView(item);
            TableLayout.LayoutParams itemP = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            item.setLayoutParams(itemP);
            item.setPadding(5, 5, 5, 5);
            item.setTag(obj);
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectRow(item, header, parentTable);
                    listener.onRowSelected(item, (JSONObject) item.getTag());
                }
            });
            for (com.example.myapplication.model.Control.ControlBase control : controls) {
                float weight = 1f;
                if (control.getControlSize() < -1) weight = 2f;
                TextView hc = new TextView(table.getContext());
                TableRow.LayoutParams hcP = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
                hc.setLayoutParams(hcP);
                if(obj.has(control.getName())) {
                    Object value = obj.get(control.getName());
                    if (value != null) {
                        hc.setText(control.getFormatValue(control.convertValue(value)));
                    }
                }
                item.addView(hc);
            }
            if(selectedValue != null && Long.parseLong(obj.get(idField).toString()) == selectedValue){
                selectRow(item, header, parentTable);
                listener.onRowSelected(item, (JSONObject) item.getTag());
            }
        }
    }
/*
    public static void showAlertDialog(Context context,String title,String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static void showConfirmDialog(Context context,String title,String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

 */



    public static Date AddDay(Date date,int day){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, day);
        return calendar.getTime();
    }

    public static   Button GenerateButton(Context context,String text, View.OnClickListener listener ) {


        Button btl = new Button(context);
        ViewGroup.LayoutParams btlP= new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 100);

        btl.setLayoutParams(btlP);
        btl.setText(text);
        btl.setOnClickListener(listener);
        return  btl;
    }



    private static Date validateDate(Context context,EditText tvl,Date currentDate){
        Calendar currentCalendar = null;
        if(currentDate != null){
            currentCalendar = Calendar.getInstance();
            currentCalendar.setTime(currentDate);
        }
        String text = null;
        Date date = null;
        boolean invalid = false;
        if(tvl.getText() != null) text = tvl.getText().toString();
        if(text != null && text.length() != 0){
            date = isInvalidDate(text, "dd/MM/yy");
            if(date == null){
                date = isInvalidDate(text, "dd/MM/yy HH:mm");
                if(date == null) {
                    date = isInvalidDate(text, "dd/MM/yy HH:mm:ss");
                    if(date == null){
                        date = isInvalidDate(text, "dd/MM/yy HH:mm:ss.SSS");
                    }
                    else{
                        if(currentCalendar != null){
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            calendar.set(Calendar.MILLISECOND,currentCalendar.get(Calendar.MILLISECOND));
                            date = calendar.getTime();
                        }
                    }
                }
                else {
                    if(currentCalendar != null){
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        calendar.set(Calendar.SECOND,currentCalendar.get(Calendar.SECOND));
                        calendar.set(Calendar.MILLISECOND,currentCalendar.get(Calendar.MILLISECOND));
                        date = calendar.getTime();
                    }
                }
            }
            if(date == null)invalid = true;
        }
        if(invalid){
            tvl.setTextColor(ContextCompat.getColor(context, com.google.android.material.R.color.design_default_color_error));
            return null;
        }
        else {
            tvl.setTextColor(ContextCompat.getColor(context, R.color.black));
        }
        return  date;
    }
    private static Date isInvalidDate(String dateString, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setLenient(false);
        try {
            Date date = sdf.parse(dateString);
            String parsedDateString = sdf.format(date);
            if(parsedDateString.equals(dateString)){
                return date;
            }else{
                return  null;
            }
        } catch (ParseException e) {
            return null;
        }
    }


}
