package com.example.myapplication;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.example.myapplication.Activity.Item;
import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupBase;
import com.example.myapplication.model.PopupConfirmation;
import com.example.myapplication.model.PopupForm;
import com.example.myapplication.model.PopupHtml;
import com.example.myapplication.model.PopupInput;
import com.example.myapplication.model.PopupLookup;
import com.example.myapplication.model.PopupSearch;
import com.example.myapplication.model.Utility;
import com.google.gson.JsonNull;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;

public  class InvCheckInActivity extends BaseActivity {

    private final InvCheckInDetailedControl itemControl = new InvCheckInDetailedControl();

    public InvCheckInActivity(){
        Controls.add(itemControl);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        long checkInId = getIntent().getLongExtra("Id", 0); // Get the passed ID
        itemControl.setValue(checkInId);
        super.onCreate(savedInstanceState);
    }


    public static class InvCheckInDetailedControl extends Control.DetailedControl {
        public InvCheckInDetailedControl() {
            super("InvCheckIns", "Stock Receive");
            setVirtualDelete(true);
            setEnableScroll(true);
            getButtons().add(new Control.ActionButton(Control.ACTION_STATUS).setEnabled(false));
            getButtons().add(new Control.ActionButton(Control.ACTION_ADD_SUB).setEnabled(false));
            getButtons().remove(getActionButton(Control.ACTION_DELETE));
        }
        @Override
        protected String getWhere(String action){
            return "Status == \"Draft\" or (CheckInTime >= " +  FilterControls.get(0).getQueryValue() + " and CheckInTime < " +  FilterControls.get(1).getQueryValue() + ")" ;
        }
        @Override
        protected String getOrderBy(String action) {
            return "Status == \"Draft\" ? 1 : Status == \"Final\" ? 2 : 3, Id Desc";
        }



        @Override
        protected void rowAdded(ArrayList<Control.ControlBase> controls,JSONObject data) {
            super.rowAdded(controls,data);
            Optional<Control.ControlBase> control = controls.stream().filter(i-> i.getName().equals("CheckInTime")).findFirst();
            if(control.isPresent() && control.get().getListTextView() != null){
                TextView tv = control.get().getListTextView();
                int colour = Color.parseColor("#80FFEB3B");
                try{
                    if(data.get("Status").toString().equals("Draft")){
                        colour = Color.parseColor("#80EE1506");
                    }
                    else if(data.get("Status").toString().equals("Final")){
                        colour = Color.parseColor("#8048BE09");
                    }
                }
                catch (JSONException e){
                    System.out.println(e.getMessage());
                }
                tv.setBackgroundColor(colour);
            }
        }
        @Override
        protected void selectRow(TableRow row, TableRow header, TableLayout tableLayout) {
            super.selectRow(row, header, tableLayout);
            try {
                JSONObject obj = (JSONObject)row.getTag();
                SelectedStatus = obj.get("Status").toString();
            }catch (JSONException e){
                System.out.println(e.getMessage());
                SelectedStatus = null;
            }
            getActionButton(Control.ACTION_STATUS).setEnabled(true);
            getActionButton(Control.ACTION_ADD_SUB).setEnabled(true);
        }
        @Override
        public void onButtonClick(Control.ActionButton action) {
            if(action.getName().equals(Control.ACTION_STATUS)){
                ArrayList<String> stats = new ArrayList<>();
                if(SelectedStatus != null && (SelectedStatus.equals("Final") || SelectedStatus.equals("Cancel")))stats.add("Draft");
                if(SelectedStatus != null && SelectedStatus.equals("Draft"))stats.add("Final");
                if(SelectedStatus != null && SelectedStatus.equals("Draft"))stats.add("Cancel");
                PopupLookup.create(getCaption(), stats, null, lookup -> {
                    new DataService().postForObject(Long.class,"InvCheckIn/UpdateStatus?id=" + getValue() + "&status=" + lookup.getName(),  new RequestParams(), aLong -> {
                        refreshGrid(Table);
                        return null;
                    },getRootActivity());
                    return true;
                }).show(((BaseActivity)action.getButton().getContext()).getSupportFragmentManager(),null);
            }
            else if(action.getName().equals(Control.ACTION_ADD_SUB)){

                String header = "Items";
                var row = getSelectedRow();
                if (row != null && row.getTag() instanceof JSONObject) {
                    Intent intent = new Intent(getRootActivity(), InvCheckInDetailsActivity.class);
                    JSONObject obj = (JSONObject) row.getTag();
                    String data = obj.toString();

                    intent.putExtra("row", data);
                    getRootActivity().startActivity(intent);
                    /*
                    String refNum = obj.optString("RefNum", "");
                    String partyName = "";
                    JSONObject busParty = obj.optJSONObject("BusParty");
                    if (busParty != null) {
                        partyName = busParty.optString("Name", "");
                    }

                    if (!refNum.isEmpty() || !partyName.isEmpty()) {
                        header = refNum + "-" + partyName;
                    }
                    */

                }



            }
            else {
                super.onButtonClick(action);
            }
        }
        private String SelectedStatus = null;

        @Override
        protected ArrayList<Control.ControlBase> getControls(String action) {
            ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
            if(action == null)return controls;
            if(action.equals(Control.ACTION_FILTER)){
                controls.add(Control.getDateControl("from","From").setFlexBasisPercent(.5f).setValue(Utility.AddDay(new Date(),-10)));
                controls.add(Control.getDateControl("to","To").setFlexBasisPercent(.5f).setValue(Utility.AddDay(new Date(),1)));
                return controls;
            }
            else if(action.equals(Control.ACTION_REFRESH)){
                ArrayList<Control.ControlBase> list = new ArrayList<Control.ControlBase>();
                controls.add(Control.getDateTimeControl("CheckInTime","Date").setColumnWeight(4));
                controls.add(Control.getEditTextControl("RefNum","Ref#").setColumnWeight(3));
                controls.add(Control.getEditTextControl("BusEmployee.Code","Em").setColumnWeight(2));
                controls.add(Control.getLookupForeignControl("BusParty","Supplier","Name").setColumnWeight(9));
                controls.add(Control.getHiddenControl( "Status", null));
                controls.add(Control.getHiddenControl( "TotalAmount", null));
                return controls;
            }
            else if(action.equals(Control.ACTION_ADD ) || action.equals(Control.ACTION_EDIT)){
                controls.add(Control.getDateTimeControl("CheckInTime", "Check In Date").setValue(new Date()));
                controls.add(Control.getEditTextControl("RefNum", "Ref Number"));
                controls.add(Control.getEditDecimalControl("TotalAmount", "Amount").setDecimalPlaces(2));
                if(action.equals(Control.ACTION_ADD)){
                    controls.add(Control.getHiddenControl( "Status", "Draft"));
                }
                controls.add(Control.getLookupForeignControl( "BusParty", "Supplier","Name").setWhere("PartyType == \"SUP\" && Active"));
                controls.add(Control.getLookupForeignControl( "BusEmployee", "Employee","BusParty.Name").setWhere("BusParty.Active"));
                controls.add(Control.getImageControl("Images", "Invoice Images", "InvCheckIn").setIsRequired(false));

                return controls;
            }
            else{
                return null;
            }
        }
    }
}
