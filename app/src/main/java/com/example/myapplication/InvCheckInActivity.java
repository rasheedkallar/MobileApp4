package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupConfirmation;
import com.example.myapplication.model.PopupForm;
import com.example.myapplication.model.PopupLookup;
import com.example.myapplication.model.Utility;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

public  class InvCheckInActivity extends BaseActivity {

    public InvCheckInActivity(){
        Controls.add(new InvCheckInDetailedControl());
    }
    public static class InvCheckInLineDetailedControl extends Control.DetailedControl {
        public InvCheckInLineDetailedControl() {
            super("InvCheckInLines", "Items","InvCheckInLine","CheckInId");
        }
        @Override
        protected ArrayList<Control.ControlBase> getControls(String action) {
            ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
            if(action == Control.ACTION_ADD || action == Control.ACTION_EDIT || action == Control.ACTION_REFRESH){
                ArrayList<Control.ControlBase> list = new ArrayList<Control.ControlBase>();
                controls.add(Control.getEditTextControl("Barcode","Barcode"));
                controls.add(Control.getEditDecimalControl("Qty","Qty"));
                controls.add(Control.getEditTextControl("Description","Description").setControlSize(Control.CONTROL_SIZE_DOUBLE));
                if(action != Control.ACTION_REFRESH)
                    controls.add(Control.getImageControl( "Images", "Item Images","InvCheckInLine"));

                return controls;
            }
            else{
                return null;
            }
        }
    }
    public static class InvCheckInDetailedControl extends Control.DetailedControl {
        public InvCheckInDetailedControl() {
            super("InvCheckIns", "Stock Receive","InvCheckIn",null);
        }
        @Override
        public void addValueView(ViewGroup container) {
            super.addValueView(container);
            if(suppliers == null){
                getActionButton(Control.ACTION_ADD).setEnabled( false);
                getActionButton(Control.ACTION_REFRESH).setEnabled( false);
            }
        }
        @Override
        public void doAction(Control.ActionButton action) {
            BaseActivity activity = (BaseActivity)action.getButton().getContext();
            if(action.getName() == Control.ACTION_ADD){
                PopupLookup.create("SupplierPicker",suppliers,0L,(supplier)->{
                    supplierId = supplier.getId();
                    PopupLookup.create("EmployeePicker",employees,0L,(employee)->{
                        employeeId = employee.getId();
                        super.doAction(action);
                        return true;
                    }).show(activity.getSupportFragmentManager(),null);
                    return true;

                }).show(activity.getSupportFragmentManager(),null);

            }
            else {
                super.doAction(action);
            }
        }

        private Long supplierId;
        private Long employeeId;


        @Override
        public void refreshGrid(TableLayout table) {
            if(suppliers == null){
                new DataService().getLookups(table.getContext(),  new String[] {"Supplier", "Employee"}, new DataService.LookupsResponse() {
                    @Override
                    public void onSuccess(List<DataService.Lookup>[] lookups) {
                        suppliers = lookups[0];
                        employees = lookups[1];
                        getActionButton(Control.ACTION_ADD).setEnabled( true);
                        getActionButton(Control.ACTION_REFRESH).setEnabled( true);
                        InvCheckInDetailedControl.super.refreshGrid(table);
                    }
                });
            }
            else{
                super.refreshGrid(table);
                getActionButton(Control.ACTION_ADD).setEnabled( true);
                getActionButton(Control.ACTION_REFRESH).setEnabled( true);
            }
        }

        private List<DataService.Lookup> suppliers = null;
        private List<DataService.Lookup> employees = null;

        @Override
        protected ArrayList<Control.ControlBase> getControls(String action) {
            ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
            if(action == null)return controls;
            if(action.equals(Control.ACTION_FILTER)){

                controls.add(Control.getDateControl("from","From").setValue(Utility.AddDay(new Date(),-10)).setControlSize(310));
                controls.add(Control.getDateControl("to","To").setValue(Utility.AddDay(new Date(),1)).setControlSize(310));
                return controls;
            }
            else if(action.equals(Control.ACTION_REFRESH)){
                ArrayList<Control.ControlBase> list = new ArrayList<Control.ControlBase>();
                controls.add(Control.getDateTimeControl("CheckInTime","Date"));
                controls.add(Control.getEditTextControl("RefNum","Ref#"));
                controls.add(Control.getEditTextControl("EmpName","Emp"));
                controls.add(Control.getLookupControl("SupId","Supplier",suppliers));
                controls.add(Control.getEditTextControl( "Status", "Status"));
                return controls;
            }
            else if(action.equals(Control.ACTION_ADD ) || action.equals(Control.ACTION_EDIT)){
                controls.add(Control.getDateTimeControl("CheckInTime", "Check In Date").setValue(new Date()));
                controls.add(Control.getEditTextControl("RefNum", "Ref Number"));
                if(action.equals(Control.ACTION_ADD)){
                    controls.add(Control.getLookupControl( "SupId", "Supplier", suppliers).setValue(supplierId));
                    controls.add(Control.getLookupControl( "EmpId", "Employee", employees).setValue(employeeId));
                }
                else{
                    controls.add(Control.getLookupControl( "SupId", "Supplier", suppliers));
                    controls.add(Control.getLookupControl( "EmpId", "Employee", employees));
                }
                controls.add(new InvCheckInLineDetailedControl());
                controls.add(Control.getImageControl( "Images", "Invoice Images","InvCheckIn"));
                return controls;
            }
            else{
                return null;
            }
        }
    }
}
