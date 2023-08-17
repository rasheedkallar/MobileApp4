package com.example.myapplication.model;
import android.content.Context;
import android.content.DialogInterface;
import android.location.GnssAntennaInfo;
import android.opengl.Visibility;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.example.myapplication.R;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class PopupForm extends Popup {
    public final List<Utility.Control> Controls;
    private FlexboxLayout _container;


    @Override
    public  onFormPopupFormListener getListener(){
        return (onFormPopupFormListener)super.getListener();
    }

    public PopupForm(android.content.Context context, String title , List<Utility.Control> controls,  onFormPopupFormListener listener){
        super(context,title,listener);
        Controls = controls;

        for (Utility.Control control : Controls) {
           AddFormControl(control,_container);
        }
    }

    @Override
    public void DoOk() {
        new DataService().post(getListener().getUrl(),getPostRequestParams(),new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                if(getListener().onSuccess( statusCode,  headers,  responseBody,result)){
                    PopupForm.super.DoOk();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String result = new String(responseBody);
                new PopupHtml(Context,"Save Error",result);
            }
        });
    }

    public RequestParams getPostRequestParams(){
        RequestParams params = new RequestParams();
        for (Utility.Control control : Controls) {
            //params.add(control.Name, control.getValue());
            params.put(control.Name,control.getValue());
        }

        return params;
    }

    @Override
    public  String getOkButton(){
        return "Save";
    }
    @Override
    public void AddControls(LinearLayout container) {

        ScrollView sv = new ScrollView(Context);
        ScrollView.LayoutParams scP= new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        scP.setLayoutDirection(LinearLayout.HORIZONTAL);
        sv.setLayoutParams(scP);
        container.addView(sv);



        FlexboxLayout fbl = new FlexboxLayout(Context);
        TableLayout.LayoutParams fblP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fbl.setLayoutParams(fblP);
        fbl.setFlexWrap(FlexWrap.WRAP);
        sv.addView(fbl);

        EditText txt = new EditText(Context);
        TableLayout.LayoutParams txtP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        txt.setLayoutParams(txtP);
        txt.setVisibility(View.GONE);
        fbl.addView(txt);

        _container = fbl;


    }
    public void AddFormControl(Utility.Control control,FlexboxLayout container){
        container.addView(control.GenerateView(Context, control));
    }


    public  static abstract  class  onFormPopupFormListener extends  Popup.onFormPopupListener{

        public abstract boolean onSuccess(int statusCode, Header[] headers, byte[] responseBody,String result);

        public abstract String getUrl();

        @Override
        public boolean onDoOk() {
            return true;
        }
    }


}
