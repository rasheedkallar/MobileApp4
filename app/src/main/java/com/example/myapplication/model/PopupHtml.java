package com.example.myapplication.model;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.example.myapplication.R;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Function;

public class PopupHtml extends PopupBase<PopupHtml,PopupHtml.PopupHtmlArgs>{

    public static PopupHtml create(String header,String html){
        PopupHtml popup = new PopupHtml();
        popup.setArgs(new PopupHtml.PopupHtmlArgs(header,html));
        return popup;
    }
    public static PopupHtml create(PopupHtmlArgs args){
        PopupHtml popup = new PopupHtml();
        popup.setArgs(args);
        return popup;
    }

    public WebView WebView;
    @Override
    public void AddControls(LinearLayout container) {

        ScrollView sv = new ScrollView(getContext());
        ScrollView.LayoutParams scP= new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        scP.setLayoutDirection(LinearLayout.HORIZONTAL);
        sv.setLayoutParams(scP);
        container.addView(sv);

        WebView = new WebView(getContext());
        ScrollView.LayoutParams lpWv= new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        WebView.setLayoutParams(lpWv);
        WebView.setVerticalScrollBarEnabled(true);
        WebView.setHorizontalScrollBarEnabled(true);
        WebView.loadDataWithBaseURL(null, getArgs().getHtml(), "text/html", "UTF-8", null);
        container.addView(WebView);
    }

    public static class  PopupHtmlArgs extends PopupArgs<PopupHtmlArgs> {
        public PopupHtmlArgs(String header,String html){
            super(header);
            setCancelButton("Close");
            Html = html;
        }
        private String Html;

        public String getHtml() {
            return Html;
        }
        public PopupHtmlArgs setHtml(String html) {
            Html = html;
            return this;
        }
    }



}
