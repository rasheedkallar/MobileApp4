package com.example.myapplication.model;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
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

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.example.myapplication.BaseActivity;
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
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import cz.msebera.android.httpclient.Header;


public class PopupForm extends PopupBase<PopupForm, PopupForm.PopupFormArgs> {

    @Override
    public PopupForm setArgs(PopupFormArgs args) {
        super.setArgs(args);
        for (int i = 0; i < args.getControls().size(); i++) {
            if(args.getControls().get(i).getClass().isAssignableFrom(Control.ImageControl.class)){
                Control.ImageControl ic = (Control.ImageControl)args.getControls().get(i);
                if(ic.getParentId() == null ||  ic.getParentId() == 0L) {
                    ic.setParentId(args.getValue());
                    ic.setEntityName(args.getEntityName());
                }
            }
        }
        return this;
    }
    private FlexboxLayout FieldsContainer;
    public FlexboxLayout getFieldsContainer() {
        return FieldsContainer;
    }

    public   boolean validate(){
        boolean valid = true;
        for (Control.ControlBase control: getArgs().getControls()) {
            if(!control.validate())valid = false;
        }
        return valid;
    }


    @Override
    public void doOk() {

        PopupFormArgs args = this.getArgs();
        String entityName = args.getEntityName();
        if(entityName == null){
            entityName = getRootActivity().getLocalClassName();
            if(entityName.endsWith("Activity"))entityName = entityName.substring(0,entityName.length() - 8);
        }


        if(!validate()){
            getPopup().getButton(android.app.AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }
        else {
            new DataService().postForLong(entityName, getPostRequestParams(), new Function<Long, Void>() {
                @Override
                public Void apply(Long aLong) {
                    doAfterSaved(aLong);
                    return null;
                }
            },getContext());
        }
    }
    protected void doAfterSaved(Long id){
        Long detailedCount = getArgs().getControls().stream().filter(i-> Control.DetailedControlBase.class.isAssignableFrom(i.getClass())).count();
        boolean defaultClose = (getArgs().getValue() != null && getArgs().getValue() != 0L) || detailedCount == null || detailedCount == 0L;

        if(getArgs().getValue() == null || getArgs().getValue() == 0L){
            for (int j = 0; j < getArgs().getControls().size(); j++) {
                if(Control.DetailedControlBase.class.isAssignableFrom(getArgs().getControls().get(j).getClass())){
                    Control.DetailedControlBase dc = (Control.DetailedControlBase)getArgs().getControls().get(j);
                    dc.setParentId(id);
                    dc.changeVisibility(true);
                }
            }
        }
        getArgs().setValue(id);

        boolean returnVal = defaultClose;
        for (int i = 0; i < getRootActivity().Controls.size(); i++) {
            Control.ControlBase control = getRootActivity().Controls.get(i);
            if(Control.DetailedControl.class.isAssignableFrom(control.getClass())){
                Control.DetailedControl dc = (Control.DetailedControl)control;
                if(dc.getEntityName() != null && dc.getEntityName().equals(getArgs().getEntityName())){
                    boolean rv = dc.doAfterSaved(id,defaultClose);
                    if(rv != defaultClose)returnVal = rv;
                }
            }
            if(getArgs().getAction() != 0 && control.getAction() == getArgs().getAction()){
                control.readValueObject(id);
            }
        }
        for (int i = 0; i < getRootActivity().Popups.size(); i++) {
            if(PopupForm.class.isAssignableFrom(getRootActivity().Popups.get(i).getClass())){
                PopupForm p = (PopupForm)getRootActivity().Popups.get(i);
                for (int j = 0; j < p.getArgs().getControls().size(); j++) {
                    Control.ControlBase control = p.getArgs().getControls().get(j);
                    if(Control.DetailedControl.class.isAssignableFrom(control.getClass())){
                        Control.DetailedControl dc = (Control.DetailedControl)control;
                        if(dc.getEntityName() != null && dc.getEntityName().equals(getArgs().getEntityName())){
                            boolean rv = dc.doAfterSaved(id,defaultClose);
                            if(rv != defaultClose)returnVal = rv;
                        }
                    }
                    if(getArgs().getAction() != 0 && control.getAction() == getArgs().getAction()){
                        control.readValueObject(id);
                    }
                }
            }
        }
        getPopup().getButton(android.app.AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        if(returnVal)super.doOk();
    }


    public RequestParams getPostRequestParams(){
        RequestParams params = new RequestParams();
        PopupFormArgs args = this.getArgs();
        if(args.getValue() != null && args.getIdName() != null && args.getIdName().length() != 0){
            params.put(args.getIdName(),args.getValue());
        }
        for (Control.ControlBase control : getArgs().getControls()) {
            control.updateSaveParameters(params);
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
            }
        }
        if(getArgs().getValue() == null || getArgs().getValue() == 0L){
            for (int j = 0; j < getArgs().getControls().size(); j++) {
                if(Control.DetailedControlBase.class.isAssignableFrom(getArgs().getControls().get(j).getClass())){
                    Control.DetailedControlBase dc = (Control.DetailedControlBase)getArgs().getControls().get(j);
                    dc.changeVisibility(false);
                }
            }
        }
        container.addView(sv);
        if(getArgs().getControls() != null && getArgs().getControls().size() > 0)getArgs().getControls().get(0).requestFocus();

    }
    public void onCapturedImage(int action,Bitmap image,String entityName,Long entityId,Long id){
        for (int i = 0; i < getArgs().getControls().size(); i++) {
            if(getArgs().getControls().get(i).getClass().isAssignableFrom(Control.ImageControl.class)){
                Control.ImageControl ic = (Control.ImageControl)getArgs().getControls().get(i);
                if(ic.getEntityName() != null && ic.getEntityName().equals(entityName) && ic.getParentId() != null && ic.getParentId().equals(entityId))
                    ic.onCapturedImage(action,image,id);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        for (int i = 0; i < getArgs().getControls().size(); i++) {
            getArgs().getControls().get(i).setRootActivity(getRootActivity());
        }
    }

    public <A extends Control.ControlBase> A getControl(String name){
        return getArgs().getControl(name);
    }


    public static class  PopupFormArgs extends PopupArgs<PopupFormArgs> {
        public PopupFormArgs( String header,List<Control.ControlBase> controls,String entityName,Long value){
            super(header);
            setCanceledOnTouchOutside(false);
            setCancelOnDestroyView(false);
            setCancelButton("Cancel");
            setOkButton("Save");
            setValue(value);
            setEntityName(entityName);
            setIdName("Id");
            if(controls == null)setControls(new ArrayList<Control.ControlBase>());
            else setControls(controls);
        }
        public <A extends Control.ControlBase> A getControl(String name){
            if(Controls == null)return null;
            Optional<Control.ControlBase> control = Controls.stream().filter(i-> i.getName().equals(name)).findFirst();
            if(control.isPresent())return (A)control.get();
            else return null;
        }

        private int Action=0;

        public int getAction() {
            return Action;
        }

        public PopupFormArgs setAction(int action) {
            Action = action;
            return  this;
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
