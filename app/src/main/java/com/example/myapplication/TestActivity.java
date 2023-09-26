package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupHtml;
import com.example.myapplication.model.PopupLookup;
import com.example.myapplication.model.Utility;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import cz.msebera.android.httpclient.Header;

public class TestActivity extends BaseActivity {
    public TestActivity(){
        Controls.add(new InvCheckInDetailedControl());
    }
    public static class InvCheckInDetailedControl extends Control.DetailedControl {
        public InvCheckInDetailedControl() {
            super("InvCheckIns", "Stock Receive","InvCheckIn",null);
        }
        @Override
        public void addDetailedView(ViewGroup container) {
            super.addDetailedView(container);
            if(suppliers == null){
                add_button.setEnabled( false);
                refresh_button.setEnabled( false);
            }
        }
        @Override
        protected void onButtonClick(String action, RadioButton button) {
            BaseActivity activity = (BaseActivity)button.getContext();
            if(action == "Add"){
                PopupLookup.create("SupplierPicker",suppliers,0L,(supplier)->{
                    supplierId = supplier.getId();
                    PopupLookup.create("EmployeePicker",employees,0L,(employee)->{
                        employeeId = employee.getId();
                        super.onButtonClick(action, button);
                        return true;
                    }).show(activity.getSupportFragmentManager(),null);
                    return true;

                }).show(activity.getSupportFragmentManager(),null);

            }
            else {
                super.onButtonClick(action, button);
            }
        }

        private Long supplierId;
        private Long employeeId;


        @Override
        protected void refreshGrid() {
            if(suppliers == null){
                new DataService().getLookups(getRootActivity(),  new String[] {"Supplier", "Employee"}, new DataService.LookupsResponse() {
                    @Override
                    public void onSuccess(List<DataService.Lookup>[] lookups) {
                        suppliers = lookups[0];
                        employees = lookups[1];
                        add_button.setEnabled( true);
                        refresh_button.setEnabled( true);
                        InvCheckInDetailedControl.super.refreshGrid();
                    }
                });
            }
            else{
                super.refreshGrid();
                add_button.setEnabled( true);
                refresh_button.setEnabled( true);
            }
        }

        private List<DataService.Lookup> suppliers = null;
        private List<DataService.Lookup> employees = null;

        @Override
        protected ArrayList<Control.ControlBase> getControls(String action) {
            ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();

            if(action == "Filter"){

                controls.add(Control.getDateControl("from","From").setValue(Utility.AddDay(new Date(),-10)).setControlSize(310));
                controls.add(Control.getDateControl("to","To").setValue(Utility.AddDay(new Date(),1)).setControlSize(310));
                return controls;
            }
            else if(action == "List"){
                ArrayList<Control.ControlBase> list = new ArrayList<Control.ControlBase>();
                controls.add(Control.getDateTimeControl("CheckInTime","Date"));
                controls.add(Control.getEditTextControl("RefNum","Ref#"));
                controls.add(Control.getEditTextControl("EmpName","Emp"));
                controls.add(Control.getLookupControl("SupId","Supplier",suppliers));
                controls.add(Control.getEditTextControl( "Status", "Status"));
                return controls;
            }
            else if(action == "Add" || action == "Edit"){
                controls.add(Control.getDateTimeControl("CheckInTime", "Check In Date").setValue(new Date()));
                controls.add(Control.getEditTextControl("RefNum", "Ref Number"));
                if(action == "Add"){
                    controls.add(Control.getLookupControl( "SupId", "Supplier", suppliers).setValue(supplierId));
                    controls.add(Control.getLookupControl( "EmpId", "Employee", employees).setValue(employeeId));
                }
                else{
                    controls.add(Control.getLookupControl( "SupId", "Supplier", suppliers));
                    controls.add(Control.getLookupControl( "EmpId", "Employee", employees));
                }
                controls.add(Control.getImageControl( "Images", "Images","InvCheckIn"));
                return controls;
            }
            else{
                return null;
            }
        }
    }
    @Override
    public void onButtonClick(String action, RadioButton button) {

    }

    @Override
    public String getHeaderText() {
        return null;
    }

}
