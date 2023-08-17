package com.example.myapplication.model;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;

import androidx.core.content.ContextCompat;

import com.example.myapplication.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PopupHtml extends Popup{


    public String Html;
    public WebView WebView;

    @Override
    public String getCancelButton() {
        return "Close";
    }

    public PopupHtml(Context context, String title , String html)
    {
        super(context, title, new onFormPopupListener() {
            @Override
            public Popup getPopup() {
                return super.getPopup();
            }
        });
        Html = html;
        WebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    private  Boolean pickerLocked = true;
    private  DatePicker dtp;
    private  LinearLayout linearLayout;
    @Override
    public void AddControls(LinearLayout container) {
        /*
        ScrollView sv = new ScrollView(Context);
        ScrollView.LayoutParams scP= new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        scP.setLayoutDirection(LinearLayout.HORIZONTAL);
        sv.setLayoutParams(scP);
        container.addView(sv);
        */
        WebView = new WebView(Context);
        ScrollView.LayoutParams lpWv= new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        WebView.setLayoutParams(lpWv);
        WebView.setVerticalScrollBarEnabled(true);
        WebView.setHorizontalScrollBarEnabled(true);
        container.addView(WebView);
    }

}
