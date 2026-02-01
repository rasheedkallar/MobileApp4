package com.example.myapplication;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

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
            SalesPreviewControl dc = (SalesPreviewControl)Controls.get(0);
            if(dc.Data == null)dc.refreshGrid();
            else dc.refreshDetailedView(dc.Data);
        }
    }
    public static class SalesPreviewControl extends Control.DetailedControl {
        public SalesPreviewControl() {
            super(null, "Sales Preview");
            setEnableScroll(true);
            getButtons().clear();
            addButton(Control.ACTION_REFRESH);
        }
        public void  RefreshData(){
            new DataService(getRootActivity()).postForExecuteList("sp_SalesPreview", new JSONObject(), new Function<JSONArray, Void>() {
                @Override
                public Void apply(JSONArray jsonArray) {

                    refreshDetailedView(jsonArray);
                    return null;
                }
            }, getRootActivity());
        }

        @Override
        public void refreshDetailedView(JSONArray data) {
            Data = data;
            super.refreshDetailedView(data);
        }
        public transient  JSONArray Data = null;
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
                controls.add(Control.getDateControl("TransDate","Date").setColumnWeight(3));
                controls.add(new PercentageDecimalControl("Cash","Cas"));
                controls.add(new PercentageDecimalControl("Card","Crd"));
                controls.add(new PercentageDecimalControl("Credit","Cdt"));
                controls.add(new PercentageDecimalControl("Unsettiled","Ust"));
                controls.add(new PercentageDecimalControl("WalkIn","Win"));
                controls.add(new PercentageDecimalControl("Delivery","Del"));
                controls.add(new PercentageDecimalControl("WalkinCar","Car"));
                controls.add(Control.getEditDecimalControl("Total","Total").setAggregate(Control.AGGREGATE_AVERAGE).setColumnWeight(3));
                return controls;
            }
            else{
                return null;
            }
        }
        public static class PercentageDecimalControl extends Control.EditDecimalControl
        {
            public PercentageDecimalControl(String name, String caption) {
                super(name, caption);
                setAggregate(Control.AGGREGATE_AVERAGE);
                setDecimalPlaces(0);
                setColumnWeight(1);
            }
            @Override
            public void addListDetails(TableRow row) {
                super.addListDetails(row);
                double d = getValue()/100;
                float f = (float)d;
                int resultColor = ColorUtils.blendARGB(Color.WHITE, Color.GREEN,f);
                resultColor = ColorUtils.blendARGB(resultColor, Color.TRANSPARENT,0.2F);
                getListTextView().setBackgroundColor(resultColor);
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
