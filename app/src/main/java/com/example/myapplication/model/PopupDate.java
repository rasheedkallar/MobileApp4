package com.example.myapplication.model;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;

import android.location.GnssAntennaInfo;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TimePicker;

import androidx.core.content.ContextCompat;

import com.example.myapplication.BaseActivity;
import com.example.myapplication.R;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class PopupDate extends PopupBase<PopupDate, PopupDate.PopupDateArgs>{

    public static PopupDate create(String header,Date value,Function<Date,Boolean> onDateChanged){
        PopupDate popup = new PopupDate();
        popup.setArgs(new PopupDateArgs(header,value));
        popup.setOnDateChanged(onDateChanged);
        return popup;
    }

    public static PopupDate create(PopupDateArgs args,Function<Date,Boolean> onDateChanged){
        PopupDate popup = new PopupDate();
        popup.setArgs(args);
        popup.setOnDateChanged(onDateChanged);
        return popup;
    }

    private  Boolean pickerLocked = true;
    private  DatePicker dtp;

    @Override
    public void AddControls(LinearLayout container) {

        PopupDateArgs dateArg = getArgs();

        Calendar calendar = Calendar.getInstance();

        calendar.setTime(dateArg.getValue());




        ScrollView sv = new ScrollView(getActivity());
        ScrollView.LayoutParams scP= new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        scP.setLayoutDirection(LinearLayout.HORIZONTAL);
        sv.setLayoutParams(scP);
        container.addView(sv);

        LinearLayout linearLayout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams lllP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(lllP);
        sv.addView(linearLayout);

        dtp = new DatePicker(getActivity());
        TableLayout.LayoutParams txtP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dtp.setLayoutParams(txtP);


        //dtp.updateDate(col.get(Calendar.YEAR),col.get(Calendar.MONTH),col.get(Calendar.DATE));
        dtp.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                if(!pickerLocked) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(dateArg.getValue());
                    calendar.set(i, i1, i2);
                    doDateChanged(calendar.getTime());
                }
            }
        });
        if(dateArg.getShowTime()){
            Button btnT = new Button(getActivity());
            TableLayout.LayoutParams btnTP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 225);
            btnT.setLayoutParams(btnTP);
            btnT.setBackgroundColor(Color.parseColor("#008477"));
            btnT.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
            btnT.setTextSize(50);
            btnT.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {





                    /*

                    new PopupTime(getArgs().getHeader(), DefaultValue, new PopupTime.onFormPopupTimeListener() {
                        @Override
                        public boolean onPick(Date date) {
                            DefaultValue = date;
                            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss   .SSS");
                            btnT.setText(dateFormat.format(DefaultValue));
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(DefaultValue);
                            pickerLocked = true;
                            dtp.updateDate(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DATE));
                            pickerLocked = false;
                            return true;
                        }
                    });
                    */



                }
            });
            container.addView(btnT);
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss   .SSS");
            btnT.setText(dateFormat.format(dateArg.getValue()));
        }




        pickerLocked = true;
        dtp.updateDate(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DATE));
        pickerLocked = false;


        //super(title,listener);
        //AllowTime = allowTime;
        //if(defaultValue == null)DefaultValue = new Date();
        //else DefaultValue = defaultValue;












        linearLayout.addView(dtp);
    }

    private void doDateChanged(Date date){
        if(onDateChanged == null)super.doOk();
        else if(onDateChanged.apply(date))super.doOk();
    }


    private Function<Date,Boolean> onDateChanged;

    public PopupDate setOnDateChanged(Function<Date, Boolean> onDateChanged) {
        this.onDateChanged = onDateChanged;
        return this;
    }

    public Function<Date, Boolean> getOnDateChanged() {
        return onDateChanged;
    }

    public static class  PopupDateArgs extends PopupArgs<PopupDateArgs> {
        public PopupDateArgs(String header,Date value){
            super(header);
            setCancelButton("Close");
            setShowTime(false);
            if(value == null)Value = new Date();
            else Value = value;
        }
        private Date Value;
        public Date getValue() {
            return Value;
        }
        public PopupDateArgs setValue(Date value) {
            Value = value;
            return this;
        }
        private boolean ShowTime;
        public boolean getShowTime() {
            return ShowTime;
        }
        public PopupDateArgs setShowTime(boolean showTime) {
            ShowTime = showTime;
            return this;
        }

    }




}
