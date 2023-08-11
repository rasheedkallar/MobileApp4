package com.example.myapplication.model;

import android.content.Context;
import android.content.DialogInterface;
import android.location.GnssAntennaInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;

import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PopupDate extends Popup{

    public Date DefaultValue;
    @Override
    public onFormPopupDateListener getListener(){
        return (onFormPopupDateListener)super.getListener();
    }
    public PopupDate(Context context, String title , Date defaultValue, onFormPopupDateListener listener)
    {
        super(context,title,listener);
        DefaultValue = defaultValue;
    }
    @Override
    public void AddControls(LinearLayout container) {

        DatePicker dtp = new DatePicker(Context);
        TableLayout.LayoutParams txtP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dtp.setLayoutParams(txtP);
        dtp.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(i,i1,i2);

                Pick(calendar.getTime());

            }
        });
        container.addView(dtp);
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
