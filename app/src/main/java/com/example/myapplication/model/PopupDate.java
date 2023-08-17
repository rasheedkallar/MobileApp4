package com.example.myapplication.model;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;

import android.location.GnssAntennaInfo;
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

import com.example.myapplication.R;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PopupDate extends Popup{

    public Date DefaultValue;
    public boolean AllowTime;
    @Override
    public onFormPopupDateListener getListener(){
        return (onFormPopupDateListener)super.getListener();
    }
    public PopupDate(Context context, String title , Date defaultValue, boolean allowTime,onFormPopupDateListener listener)
    {
        super(context,title,listener);
        AllowTime = allowTime;
        if(defaultValue == null)DefaultValue = new Date();
        else DefaultValue = defaultValue;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DefaultValue);
        pickerLocked = true;
        dtp.updateDate(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DATE));
        pickerLocked = false;

        if(allowTime){
            Button btnT = new Button(Context);
            TableLayout.LayoutParams btnTP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 225);
            btnT.setLayoutParams(btnTP);
            btnT.setBackgroundColor(Color.parseColor("#008477"));
            btnT.setTextColor(ContextCompat.getColor(Context, R.color.white));
            btnT.setTextSize(50);
            btnT.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    new PopupTime(Context, Title, DefaultValue, new PopupTime.onFormPopupTimeListener() {
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
                }
            });
            linearLayout.addView(btnT);
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss   .SSS");
            btnT.setText(dateFormat.format(defaultValue));
        }
    }

    private  Boolean pickerLocked = true;
    private  DatePicker dtp;
    private  LinearLayout linearLayout;
    @Override
    public void AddControls(LinearLayout container) {

        ScrollView sv = new ScrollView(Context);
        ScrollView.LayoutParams scP= new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        scP.setLayoutDirection(LinearLayout.HORIZONTAL);
        sv.setLayoutParams(scP);
        container.addView(sv);

        linearLayout = new LinearLayout(Context);
        LinearLayout.LayoutParams lllP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //lllP.setMargins(0, 0, 0, 0);
        //linearLayout.setPadding(0, 0, 0, 8);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(lllP);
        sv.addView(linearLayout);




        dtp = new DatePicker(Context);
        TableLayout.LayoutParams txtP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dtp.setLayoutParams(txtP);
        //dtp.updateDate(col.get(Calendar.YEAR),col.get(Calendar.MONTH),col.get(Calendar.DATE));
        dtp.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                if(!pickerLocked) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(DefaultValue);
                    calendar.set(i, i1, i2);
                    DefaultValue = calendar.getTime();
                    Pick(DefaultValue);
                }
            }
        });
        linearLayout.addView(dtp);


    }
    public void Pick(Date date){
        if(getListener().onPick(date))super.DoOk();
    }

    public  static abstract  class  onFormPopupDateListener extends  onFormPopupListener{

        @Override
        public PopupDate getPopup(){
            return  (PopupDate)super.getPopup();
        }

        public abstract boolean onPick(Date date);
    }

}
