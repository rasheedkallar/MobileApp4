package com.example.myapplication.model;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import cz.msebera.android.httpclient.Header;
import kotlin.text.Charsets;


public class PopupSearch extends PopupBase<PopupSearch, PopupSearch.PopupSearchArgs> {
    public static interface PopupSearchListener{

        boolean onItemSelected(TableRow row, JSONObject data, DataService.Lookup lookup);
        void onTextChange(EditText editor,int keyCode);
        boolean onPressOk();

    }

    public static PopupSearch create(String header, List<Control.ControlBase> controls, String displayField){
        PopupSearch search = new PopupSearch();
        search.setArgs(new PopupSearch.PopupSearchArgs(header,controls,displayField));


        return search;
    }
    public static PopupSearch create(PopupSearch.PopupSearchArgs args){
        PopupSearch search = new PopupSearch();
        search.setArgs(args);


        return search;
    }

    public PopupSearchListener Listener;


    public PopupSearch setListener(PopupSearchListener listener) {
        Listener = listener;
        return this;
    }

    public PopupSearchListener getListener() {
        return Listener;
    }
    private FlexboxLayout FieldsContainer;
    public FlexboxLayout getFieldsContainer() {
        return FieldsContainer;
    }
    private  TableLayout table_layout;
    private Control.DetailedControl detailed_control;
    protected EditText SearchEditText;

    @Override
    public void doOk() {
        if(getListener().onPressOk())PopupSearch.super.doOk();
    }
    public void refreshDetailedView(JSONArray data){
        detailed_control.refreshDetailedView(data);
    }
    @Override
    public void AddControls(LinearLayout container) {

        detailed_control = new Control.DetailedControl("",getArgs().getHeader(),null,null) {
            @Override
            protected ArrayList<Control.ControlBase> getControls(String action) {

                if(action == Control.ACTION_REFRESH){
                    return (ArrayList<Control.ControlBase>)getArgs().getControls();
                }
                else{
                    return null;
                }
            }
            @Override
            protected void onRowSelected(TableRow row) {
                super.onRowSelected(row);
                String display = "[Unknown]";
                JSONObject obj = null;
                try {
                    obj = (JSONObject)row.getTag();
                    display = obj.get(getArgs().getDisplayField()).toString();
                }
                catch (JSONException e){

                }
                DataService.Lookup l = new DataService.Lookup();
                l.setId(getValue());
                l.setName(display);
                if(getListener().onItemSelected(row,obj,l))PopupSearch.super.doOk();
                //if(OnItemSelected.apply(l))PopupSearch.super.doOk();
            }
        };
        detailed_control.setEnableScroll(true);
        SearchEditText = new EditText(getContext());
        TableLayout.LayoutParams txtP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        SearchEditText.setLayoutParams(txtP);

        SearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                if(s.length() >= start + after && start + after >1) {
                    int ascii = (int) s.charAt(start + after -1);
                    getListener().onTextChange(SearchEditText,ascii);
                    //onKeyPress(SearchEditText, ascii);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });




        container.addView(SearchEditText);
        //ScrollView sv = new ScrollView(getContext());
        //ScrollView.LayoutParams scP= new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        //scP.setLayoutDirection(LinearLayout.HORIZONTAL);
        //sv.setLayoutParams(scP);

        /*
        table_layout = new TableLayout(getContext());
        ScrollView.LayoutParams tlP= new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        table_layout.setLayoutParams(tlP);

         */
        //sv.addView(table_layout);
        //container.addView(sv);

        detailed_control.getButtons().clear();

        detailed_control.addView(container);

    }
    //protected void onKeyPress(View view, int keycode){
    //    if(keycode == getArgs().getSearchKey()){
    //         OnTextChange.apply(SearchEditText.getText().toString());
    //    }
    // }

    public static class  PopupSearchArgs extends PopupArgs<PopupSearchArgs> {
        public PopupSearchArgs(String header, List<Control.ControlBase> controls, String displayField){
            super(header);
            setControls(controls);
            setCancelButton("Cancel");

            setIdField("Id");
            setKeywordsField("keyWords");
            setDisplayField(displayField);
            if(controls == null)setControls(new ArrayList<Control.ControlBase>());
            else setControls(controls);
        }
        private boolean AllowNull= false;
        public PopupSearchArgs setAllowNull(boolean allowNull) {
            AllowNull = allowNull;
            if(allowNull)setOkButton("Clear");
            else setOkButton(null);
            return this;
        }

        private List<Control.ControlBase> Controls;
        public List<Control.ControlBase> getControls() {
            return Controls;
        }

        private String KeywordsField;
        public String getKeywordsField() {
            return KeywordsField;
        }
        public PopupSearchArgs setKeywordsField(String keywordsField) {
            KeywordsField = keywordsField;
            return this;
        }
        private String IdField;
        public String getIdField() {
            return IdField;
        }
        public PopupSearchArgs setIdField(String idField) {
            IdField = idField;
            return this;
        }




        private String DisplayField;
        public String getDisplayField() {
            return DisplayField;
        }
        public PopupSearchArgs setDisplayField(String displayField) {
            DisplayField = displayField;
            return this;
        }


        public PopupSearchArgs setControls(List<Control.ControlBase> controls) {
            Controls = controls;
            return this;
        }
    }
}
