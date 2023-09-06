package com.example.myapplication.model;

import android.content.Context;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class PopupConfirmation extends Popup{
    public String ConfirmMessage;
    private TextView textView;

    @Override
    public String getCancelButton() {
        return "No";
    }
    @Override
    public String getOkButton() {
        return "Yes";
    }

    public PopupConfirmation(Context context, String confirmMessage, onFormPopupConfirmListener listener)
    {
        super(context, "Confirmation", listener);
        ConfirmMessage = confirmMessage;
        textView.setText(confirmMessage);
    }
    //private  Boolean pickerLocked = true;
    //private  DatePicker dtp;
    //private  LinearLayout linearLayout;
    @Override
    public void AddControls(LinearLayout container) {

        textView = new TextView(Context);
        ScrollView.LayoutParams lpWv= new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        textView.setPadding(20,50,20,0);
        textView.setLayoutParams(lpWv);
        textView.setVerticalScrollBarEnabled(true);
        textView.setHorizontalScrollBarEnabled(true);
        textView.setTextSize(15);
        container.addView(textView);
    }
    public  static abstract  class  onFormPopupConfirmListener extends  Popup.onFormPopupListener{
        @Override
        public boolean onDoOk() {
            onConfirm();
            return true;
        }
        public abstract void onConfirm();
    }
}
