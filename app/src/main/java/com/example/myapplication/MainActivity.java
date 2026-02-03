package com.example.myapplication;

import android.graphics.Color;
import android.widget.TableLayout;
import android.widget.TextView;

import com.example.myapplication.model.Control;
import com.example.myapplication.Data.DataService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;


public class MainActivity extends BaseActivity {
    public MainActivity()
    {
        //Controls.add(new MainActivity.MonitorStatusControl());
    }
    public static class MonitorStatusControl extends Control.DetailedControl {
        public MonitorStatusControl() {
            super("sp_UpdateMonitorStatus", "Monitor Preview");
            setEnableScroll(true);
            getButtons().clear();
            addButton(Control.ACTION_REFRESH);
        }
        @Override
        public void  refreshGrid(TableLayout table){
            new DataService(getRootActivity()).postForExecuteList("sp_UpdateMonitorStatus", new JSONObject(), new Function<JSONArray, Void>() {
                @Override
                public Void apply(JSONArray jsonArray) {

                    refreshDetailedView(jsonArray);
                    return null;
                }
            }, getRootActivity());
        }


        @Override
        protected ArrayList<Control.ControlBase> getControls(String action) {
            ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
            if(action == null)return controls;
            if(action.equals(Control.ACTION_REFRESH)){
                controls.add(Control.getEditTextControl("Code","Name").setColumnWeight(5));
                controls.add(Control.getDateTimeControl("ExpiryDate","Expiry Date").setColumnWeight(3));
                controls.add(Control.getEditTextControl("FinalStatus","Status").setColumnWeight(2));
                return controls;
            }
            else{
                return null;
            }
        }
        @Override
        protected void rowAdded(ArrayList<Control.ControlBase> controls,JSONObject data) {
            super.rowAdded(controls,data);
            Optional<Control.ControlBase> control = controls.stream().filter(i-> i.getName().equals("FinalStatus")).findFirst();
            if(control.isPresent() && control.get().getListTextView() != null){
                TextView tv = control.get().getListTextView();
                int colour = Color.parseColor("#80FFEB3B");
                try{
                    if(data.getString("FinalStatus").equals("Active")){
                        colour = Color.parseColor("#8048BE09");
                    }
                    else {
                        colour = Color.parseColor("#80EE1506");
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