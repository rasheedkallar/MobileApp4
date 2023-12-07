package com.example.myapplication;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupConfirmation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

public  class SalesPreview extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null){
            SalesPreviewControl dc = new SalesPreviewControl();
            dc.addView(Container);
            Controls.add(dc);
            dc.RefreshData();
        }
        else{

        }
    }
    public static class SalesPreviewControl extends Control.DetailedControl {
        public SalesPreviewControl() {
            super("sp_SalesPrview", "Sales Preview");
            setEnableScroll(true);
            getButtons().clear();
            addButton(Control.ACTION_REFRESH);

        }
        public void  RefreshData(){
            new DataService().postForExecuteList("sp_SalesPrview", new JSONObject(), new Function<JSONArray, Void>() {
                @Override
                public Void apply(JSONArray jsonArray) {
                    refreshDetailedView(jsonArray);
                    return null;
                }
            }, getRootActivity());
        }

        @Override
        public void onButtonClick(Control.ActionButton action) {
            if(action.getName() == Control.ACTION_REFRESH){
                RefreshData();
            }
            else {
                super.onButtonClick(action);
            }
        }
        @Override
        protected ArrayList<Control.ControlBase> getControls(String action) {
            ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
            if(action == null)return controls;
            if(action.equals(Control.ACTION_REFRESH)){
                controls.add(Control.getDateControl("TransDate","Date"));
                controls.add(Control.getEditDecimalControl("Credit","Credit").setAggregate(Control.AGGREGATE_SUM));
                controls.add(Control.getEditDecimalControl("Card","Card").setAggregate(Control.AGGREGATE_SUM));
                controls.add(Control.getEditDecimalControl("Cash","Cash").setAggregate(Control.AGGREGATE_SUM));
                controls.add(Control.getEditDecimalControl("Total","Total").setAggregate(Control.AGGREGATE_SUM));
                return controls;
            }
            else{
                return null;
            }
        }
        @Override
        protected void rowAdded(ArrayList<Control.ControlBase> controls,JSONObject data) {
            super.rowAdded(controls,data);
            Optional<Control.ControlBase> control = controls.stream().filter(i-> i.getName().equals("Amount")).findFirst();
            if(control.isPresent() && control.get().getListTextView() != null){
                TextView tv = control.get().getListTextView();
                int colour = Color.parseColor("#80FFEB3B");
                try{
                    if(data.getDouble("Amount") < 0){
                        colour = Color.parseColor("#80EE1506");
                    }
                    else {
                        colour = Color.parseColor("#8048BE09");
                    }
                }
                catch (JSONException e){
                    System.out.println(e.getMessage());
                }
                tv.setBackgroundColor(colour);
            }
        }
    }
}
