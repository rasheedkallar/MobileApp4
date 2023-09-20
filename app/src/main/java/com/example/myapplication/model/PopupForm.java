package com.example.myapplication.model;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.location.GnssAntennaInfo;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.example.myapplication.R;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.internal.FlowLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;

import cz.msebera.android.httpclient.Header;

public class PopupForm extends PopupBase<PopupForm, PopupForm.PopupFormArgs> {
    private FlexboxLayout FieldsContainer;
    public FlexboxLayout getFieldsContainer() {
        return FieldsContainer;
    }


    private Function<Long,Boolean> onAfterSaved;

    public Function<Long, Boolean> getOnAfterSaved() {
        return onAfterSaved;
    }

    public PopupForm setOnAfterSaved(Function<Long, Boolean> onAfterSaved) {
        this.onAfterSaved = onAfterSaved;
        return this;
    }
    public void doAfterSaved(Long id){
        if(onAfterSaved == null)super.doOk();
        else if(onAfterSaved.apply(id))super.doOk();
    }
    @Override
    public void doOk() {

        PopupFormArgs args = this.getArgs();
        String entityName = args.getEntityName();
        if(entityName == null){
            entityName = getRootActivity().getLocalClassName();
            if(entityName.endsWith("Activity"))entityName = entityName.substring(0,entityName.length() - 8);
        }


        if(!Utility.validate(args.getControls())){
            getPopup().getButton(android.app.AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }
        else {
            new DataService().post(entityName, getPostRequestParams(), new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String result = new String(responseBody);
                    Long id = Long.parseLong(result);
                    doAfterSaved(id);
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String result = new String(responseBody);
                    System.out.println("Response Error: " + result);
                    getPopup().getButton(android.app.AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            });
        }
    }
    public RequestParams getPostRequestParams(){
        //2023-01-01T00:00:00
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
        RequestParams params = new RequestParams();

        PopupFormArgs args = this.getArgs();
        if(args.getValue() != null && args.getIdName() != null && args.getIdName().length() != 0){
            params.put(args.getIdName(),args.getValue());
        }


        for (Control.ControlBase control : getArgs().getControls()) {
            //System.out.println(control.Name);

            Object value = control.getValue();
            if (value != null && value instanceof Date) {
                String formattedDate = dateFormat.format(value);
                value = formattedDate;
                //params.put(control.getName(), formattedDate);
            }
            //if(params.has(control.getName())){
            //    params.p
            //}
            //else {

                params.put(control.getName(), value);
            //}

        }

        return params;
    }


    @Override
    public void AddControls(LinearLayout container) {

        ScrollView sv = new ScrollView(getContext());
        ScrollView.LayoutParams scP= new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        scP.setLayoutDirection(LinearLayout.HORIZONTAL);
        sv.setLayoutParams(scP);


        FieldsContainer = new FlexboxLayout(getContext());
        TableLayout.LayoutParams fblP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        FieldsContainer.setLayoutParams(fblP);
        FieldsContainer.setFlexWrap(FlexWrap.WRAP);
        sv.addView(FieldsContainer);



        EditText txt = new EditText(getContext());
        TableLayout.LayoutParams txtP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        txt.setLayoutParams(txtP);
        txt.setVisibility(View.GONE);
        FieldsContainer.addView(txt);



        if(getArgs().getControls() != null) {
            for (Control.ControlBase control : getArgs().getControls()) {

                control.addView(FieldsContainer);


                //View view = control.generateFillView(getContext());

                //View view = Control.getView(getContext(),control);

                //if(view != null){
                //    FieldsContainer.addView(view);
                //}

            }
        }


        container.addView(sv);
    }

    //public ScrollView sv;


    //private TableLayout table = null;



    public static class  PopupFormArgs extends PopupArgs<PopupFormArgs> {
        public PopupFormArgs( String header,List<Control.ControlBase> controls,Long value){
            super(header);
            setCanceledOnTouchOutside(false);
            setCancelOnDestroyView(false);
            setCancelButton("Cancel");
            setOkButton("Save");
            setValue(value);
            //setEntityName(entityName);
            setIdName("Id");
            if(controls == null)setControls(new ArrayList<Control.ControlBase>());
            else setControls(controls);
        }
        private List<Control.ControlBase> Controls;
        public List<Control.ControlBase> getControls() {
            return Controls;
        }

        private String EntityName;
        public String getEntityName() {
            return EntityName;
        }
        public PopupFormArgs setEntityName(String entityName) {
            EntityName = entityName;
            return this;
        }


        private String IdName;
        public String getIdName() {
            return IdName;
        }
        public PopupFormArgs setIdName(String idName) {
            IdName = idName;
            return this;
        }


        private Long Value;
        public Long getValue() {
            return Value;
        }
        public PopupFormArgs setValue(Long value) {
            Value = value;
            return this;
        }

        public PopupFormArgs setControls(List<Control.ControlBase> controls) {
            Controls = controls;
            return this;
        }
    }





}
