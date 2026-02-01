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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import kotlin.jvm.functions.Function2;


public class PopupForm extends PopupBase<PopupForm, PopupForm.PopupFormArgs> {

    @Override
    public PopupForm setArgs(PopupFormArgs args) {
        super.setArgs(args);
        for (int i = 0; i < args.getControls().size(); i++) {
            if(Control.DetailedControl.class.isAssignableFrom( args.getControls().get(i).getClass())){
                Control.DetailedControl ic = (Control.DetailedControl)args.getControls().get(i);
                if(ic.getParentId() == null ||  ic.getParentId() == 0L) {
                    ic.setParentId(args.getValue());
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
        if(!validate()){
            getPopup().getButton(android.app.AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }
        else {
            List<Control.ControlBase> controls = getArgs().getControls();
            if(controls != null && controls.size() != 0){
                JSONObject obj = new JSONObject();
                for (Control.ControlBase control : getArgs().getControls()) {
                    control.updateValueToJSONObject(obj);
                }
                new DataService(getRootActivity()).postForSave(getArgs().getPath(), obj, aLong -> {
                    doAfterSaved(aLong);
                    return null;
                }, s -> {
                    PopupHtml.create("Save Error",s).show(getRootActivity().getSupportFragmentManager(),null);
                    getPopup().getButton(android.app.AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    return null;
                });
            }
        }
    }

    private String getFullPathNew(){
        String path = getArgs().getPath();
        if(path.endsWith("[]"))return path;
        else if(path.endsWith("]")){
            return path.substring(0,path.lastIndexOf("[")) + "[]";
        }
        else return path;
    }


    public Function<Long,Boolean> SaveListener;
    public PopupForm setSaveListener(Function<Long,Boolean> saveListener) {
        SaveListener = saveListener;
        return this;
    }
    public Function<Long,Boolean> getSaveListener() {
        return SaveListener;
    }

    protected void doAfterSaved(Long id){

        Long detailedCount = getArgs().getControls().stream().filter(i-> Control.DetailedControlBase.class.isAssignableFrom(i.getClass())).count();
        boolean defaultClose = (getArgs().getValue() != null && getArgs().getValue() != 0L) || detailedCount == null || detailedCount == 0L;
        if(getArgs().getValue() == null || getArgs().getValue() == 0L){
            getArgs().setValue(id);
            String path = getArgs().getPath();
            if(path.endsWith("[]")){
                path = path.substring(0,path.length() -1) + id + "]";
                getArgs().setPath(path);
            }
            for (int j = 0; j < getArgs().getControls().size(); j++) {
                getArgs().getControls().get(j).setPath(path);
                if(Control.DetailedControlBase.class.isAssignableFrom(getArgs().getControls().get(j).getClass())){
                    Control.DetailedControlBase dc = (Control.DetailedControlBase)getArgs().getControls().get(j);
                    dc.setParentId(id);
                    dc.setVisible(true);
                }
            }
        }
        boolean returnVal = defaultClose;
        for (int i = 0; i < getRootActivity().Controls.size(); i++) {
            Control.ControlBase control = getRootActivity().Controls.get(i);
            if(Control.DetailedControl.class.isAssignableFrom(control.getClass())){
                Control.DetailedControl dc = (Control.DetailedControl)control;
                if(dc.getFullPathNew().equals(getFullPathNew())){
                    boolean rv = dc.doAfterSaved(id,defaultClose,getArgs());
                    if(rv != defaultClose)returnVal = rv;
                }
            }
            if( control.getFullPath().equals(getArgs().getActionPath())){
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
                        if(dc.getFullPathNew().equals(getFullPathNew())){
                            boolean rv = dc.doAfterSaved(id,defaultClose,getArgs());
                            if(rv != defaultClose)returnVal = rv;
                        }
                    }
                    if(control.getFullPath().equals(getArgs().getActionPath())){
                        control.readValueObject(id);
                    }
                }
            }
        }
        getPopup().getButton(android.app.AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        if(getSaveListener() != null){
            returnVal = getSaveListener().apply(id);
        }
        if(returnVal)super.doOk();
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
                    dc.setVisible(false);
                }
            }
        }
        container.addView(sv);
        if(getArgs().getControls() != null && getArgs().getControls().size() > 0)getArgs().getControls().get(0).requestFocus();

    }
    public void onCapturedImage(int action,Bitmap image,String entityName,String fileGroup,Long entityId,Long id){
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
        public PopupFormArgs( String header,ArrayList<Control.ControlBase> controls,String path,Long value){
            super(header);
            setCanceledOnTouchOutside(false);
            setCancelOnDestroyView(false);
            setCancelButton("Cancel");
            setOkButton("Save");
            setValue(value);
            setPath(path);
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

        private String ActionPath;

        public String getActionPath() {
            return ActionPath;
        }

        public PopupFormArgs setActionPath(String actionPath) {
            ActionPath = actionPath;
            return  this;
        }

        private ArrayList<Control.ControlBase> Controls;
        public ArrayList<Control.ControlBase> getControls() {
            return Controls;
        }

        private String Path;
        public String getPath() {
            return Path;
        }
        public PopupFormArgs setPath(String path) {
            Path = path;
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

        public PopupFormArgs setControls(ArrayList<Control.ControlBase> controls) {
            Controls = controls;
            return this;
        }
    }





}
