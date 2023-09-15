package com.example.myapplication.model;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TimePicker;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;

public class PopupTime extends PopupBase<PopupTime,PopupBase.PopupArgsDefault, PopupBase.PopupListener>{
    public Date DefaultValue;

    public PopupTime( String title , Date defaultValue)
    {
        //super(title,listener);
        DefaultValue = defaultValue;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DefaultValue);
        tp.setHour(calendar.get(Calendar.HOUR));
        tp.setMinute(calendar.get(Calendar.MINUTE));
        secondsPicker.setValue(calendar.get(Calendar.SECOND));
        millisecondsPicker.setValue(calendar.get(Calendar.MILLISECOND));
    }

    //@Override
    //public String getOkButton() {
    //    return "Ok";
    //}
    @Override
    public void doOk() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DefaultValue);
        calendar.set(Calendar.HOUR,tp.getHour());
        calendar.set(Calendar.MINUTE,tp.getMinute());
        calendar.set(Calendar.SECOND,secondsPicker.getValue());
        calendar.set(Calendar.MILLISECOND,millisecondsPicker.getValue());

        super.doOk();
    }
    private TimePicker tp;
    private NumberPicker secondsPicker;
    private NumberPicker millisecondsPicker;
    @Override
    public void AddControls(LinearLayout container) {

        ScrollView sv = new ScrollView(getContext());
        ScrollView.LayoutParams scP= new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        scP.setLayoutDirection(LinearLayout.VERTICAL);
        sv.setLayoutParams(scP);
        container.addView(sv);

        LinearLayout main = new LinearLayout(getContext());
        LinearLayout.LayoutParams mainP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //mainP.setMargins(0, 0, 0, 0);
        //main.setPadding(0, 0, 0, 0);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setLayoutParams(mainP);
        sv.addView(main);

        LinearLayout linearLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams lllP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(lllP);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        secondsPicker = new NumberPicker(getContext());
        secondsPicker.setMinValue(0);
        secondsPicker.setMaxValue(59);
        LinearLayout.LayoutParams secondsLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        secondsPicker.setLayoutParams(secondsLayoutParams);

        millisecondsPicker = new NumberPicker(getContext());
        millisecondsPicker.setMinValue(0);
        millisecondsPicker.setMaxValue(999);
        LinearLayout.LayoutParams millisecondsLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        millisecondsPicker.setLayoutParams(millisecondsLayoutParams);

        linearLayout.addView(secondsPicker);
        linearLayout.addView(millisecondsPicker);
        main.addView(linearLayout);

        tp = new TimePicker(getContext());
        TimePicker.LayoutParams txtT= new TimePicker.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.setLayoutParams(txtT);
        tp.setIs24HourView(true);
        main.addView(tp);

    }
    public void Pick(Date date){

    }


    public static class  PopupDateTimeArgs extends PopupArgs<PopupDateTimeArgs,PopupListener> {
        public PopupDateTimeArgs(String key,String header,Date defaultValue){
            super(key,header);
            setOkButton("Ok");
            setCancelButton("Cancel");
        }
        private Date DefaultValue;

        public Date getDefaultValue() {
            return DefaultValue;
        }
        public PopupDateTimeArgs setDefaultValue(Date defaultValue) {
            DefaultValue = defaultValue;
            return this;
        }
    }

}
