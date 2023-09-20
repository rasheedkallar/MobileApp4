package com.example.myapplication.model;

import android.content.Context;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.List;
import java.util.function.Function;

import cz.msebera.android.httpclient.Header;

public class PopupConfirmation extends PopupBase<PopupConfirmation,PopupConfirmation.PopupConfirmationArgs>{

    public static PopupConfirmation create(String header,String confirmationMessage,Function<Void,Boolean> onConfirmed){
        PopupConfirmation confirm = new PopupConfirmation();
        confirm.setArgs(new PopupConfirmationArgs(header,confirmationMessage));
        confirm.setOnConfirmed(onConfirmed);
        return confirm;
    }
    public static PopupConfirmation create(PopupConfirmationArgs args,Function<Void,Boolean> onConfirmed){
        PopupConfirmation confirm = new PopupConfirmation();
        confirm.setArgs(args);
        confirm.setOnConfirmed(onConfirmed);
        return confirm;
    }


    public Function<Void,Boolean> onConfirmed;

    public Function<Void, Boolean> getOnConfirmed() {
        return onConfirmed;
    }
    public PopupConfirmation setOnConfirmed(Function<Void, Boolean> onConfirmed) {
        this.onConfirmed = onConfirmed;
        return this;
    }
    private TextView textView;
    @Override
    public void AddControls(LinearLayout container) {

        textView = new TextView(getActivity());
        ScrollView.LayoutParams lpWv= new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        textView.setPadding(20,50,20,0);
        textView.setLayoutParams(lpWv);
        textView.setVerticalScrollBarEnabled(true);
        textView.setHorizontalScrollBarEnabled(true);
        textView.setTextSize(15);
        textView.setText(getArgs().getConfirmationMessage());
        container.addView(textView);
    }
    @Override
    public void doOk() {
        doConformed();
    }
    public void doConformed(){
        if(onConfirmed == null)super.doOk();
        else if(onConfirmed.apply(null))super.doOk();
    }


    public static class  PopupConfirmationArgs extends PopupArgs<PopupConfirmationArgs> {
        public PopupConfirmationArgs(String header,String confirmationMessage){
            super(header);
            setOkButton("Yes");
            setCancelButton("No");
            ConfirmationMessage = confirmationMessage;
        }
        private String ConfirmationMessage;

        public String getConfirmationMessage() {
            return ConfirmationMessage;
        }
        public PopupConfirmationArgs setConfirmationMessage(String confirmationMessage) {
            ConfirmationMessage = confirmationMessage;
            return this;
        }
    }
}
