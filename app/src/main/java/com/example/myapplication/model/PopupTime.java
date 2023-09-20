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
import java.util.function.Function;

public class PopupTime extends PopupBase<PopupTime, PopupTime.PopupTimeArgs>{
    public static PopupTime create(String header,Date value,Function<Date,Boolean> onTimeChanged){
        PopupTime popup = new PopupTime();
        popup.setArgs(new PopupTime.PopupTimeArgs(header,value));
        popup.setOnTimeChanged(onTimeChanged);
        return popup;
    }
    public static PopupTime create(PopupTimeArgs args,Function<Date,Boolean> onTimeChanged){
        PopupTime popup = new PopupTime();
        popup.setArgs(args);
        popup.setOnTimeChanged(onTimeChanged);
        return popup;
    }

    @Override
    public void doOk() {


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
    private void doTimeChanged(Date date){
        if(onTimeChanged == null)super.doOk();
        else if(onTimeChanged.apply(date))super.doOk();
    }


    private Function<Date,Boolean> onTimeChanged;

    public PopupTime setOnTimeChanged(Function<Date, Boolean> onDateChanged) {
        this.onTimeChanged = onDateChanged;
        return this;
    }

    public Function<Date, Boolean> getOnTimeChanged() {
        return onTimeChanged;
    }


    public static class  PopupTimeArgs extends PopupArgs<PopupTimeArgs> {
        public PopupTimeArgs(String header,Date defaultValue){
            super(header);
            setCancelButton("Close");
        }
        private Date DefaultValue;

        public Date getDefaultValue() {
            return DefaultValue;
        }
        public PopupTimeArgs setDefaultValue(Date defaultValue) {
            DefaultValue = defaultValue;
            return this;
        }
    }

}
