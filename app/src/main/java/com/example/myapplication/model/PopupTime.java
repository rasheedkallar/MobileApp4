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

public class PopupTime extends Popup{
    public Date DefaultValue;
    @Override
    public onFormPopupTimeListener getListener(){
        return (onFormPopupTimeListener)super.getListener();
    }
    public PopupTime(Context context, String title , Date defaultValue, onFormPopupTimeListener listener)
    {
        super(context,title,listener);
        DefaultValue = defaultValue;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DefaultValue);
        tp.setHour(calendar.get(Calendar.HOUR));
        tp.setMinute(calendar.get(Calendar.MINUTE));
        secondsPicker.setValue(calendar.get(Calendar.SECOND));
        millisecondsPicker.setValue(calendar.get(Calendar.MILLISECOND));
    }

    @Override
    public String getOkButton() {
        return "Ok";
    }
    @Override
    public void DoOk() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DefaultValue);
        calendar.set(Calendar.HOUR,tp.getHour());
        calendar.set(Calendar.MINUTE,tp.getMinute());
        calendar.set(Calendar.SECOND,secondsPicker.getValue());
        calendar.set(Calendar.MILLISECOND,millisecondsPicker.getValue());
        getListener().onPick(calendar.getTime());
        super.DoOk();
    }
    private TimePicker tp;
    private NumberPicker secondsPicker;
    private NumberPicker millisecondsPicker;
    @Override
    public void AddControls(LinearLayout container) {

        ScrollView sv = new ScrollView(Context);
        ScrollView.LayoutParams scP= new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        scP.setLayoutDirection(LinearLayout.VERTICAL);
        sv.setLayoutParams(scP);
        container.addView(sv);

        LinearLayout main = new LinearLayout(Context);
        LinearLayout.LayoutParams mainP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //mainP.setMargins(0, 0, 0, 0);
        //main.setPadding(0, 0, 0, 0);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setLayoutParams(mainP);
        sv.addView(main);

        LinearLayout linearLayout = new LinearLayout(Context);
        LinearLayout.LayoutParams lllP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(lllP);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        secondsPicker = new NumberPicker(Context);
        secondsPicker.setMinValue(0);
        secondsPicker.setMaxValue(59);
        LinearLayout.LayoutParams secondsLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        secondsPicker.setLayoutParams(secondsLayoutParams);

        millisecondsPicker = new NumberPicker(Context);
        millisecondsPicker.setMinValue(0);
        millisecondsPicker.setMaxValue(999);
        LinearLayout.LayoutParams millisecondsLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        millisecondsPicker.setLayoutParams(millisecondsLayoutParams);

        linearLayout.addView(secondsPicker);
        linearLayout.addView(millisecondsPicker);
        main.addView(linearLayout);

        tp = new TimePicker(Context);
        TimePicker.LayoutParams txtT= new TimePicker.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.setLayoutParams(txtT);
        tp.setIs24HourView(true);
        main.addView(tp);

    }
    public void Pick(Date date){
        if(getListener().onPick(date))super.DoOk();
    }
    public  static abstract  class  onFormPopupTimeListener extends  onFormPopupListener{
        @Override
        public PopupTime getPopup(){
            return  (PopupTime)super.getPopup();
        }
        public abstract boolean onPick(Date date);
    }

}
