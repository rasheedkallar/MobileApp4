package com.example.myapplication;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.Toast;

import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupLookup;
import com.example.myapplication.model.Utility;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

public  class InvCheckInActivity extends BaseActivity {




    public InvCheckInActivity(){
        Controls.add(new InvCheckInDetailedControl());
    }


    public static class BarcodeTextControl extends Control.EditTextControl{
        public BarcodeTextControl() {
            super("Barcode", "Barcode");
        }
        @Override
        public void addValueView(ViewGroup container) {
            super.addValueView(container);
            getEditTextInput().setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if(i == KeyEvent.KEYCODE_ENTER){
                        if(getEditTextInput().getText() != null && getEditTextInput().toString().length() !=0){
                            String barcode = getEditTextInput().toString();
                            new DataService().getObject("InvItem?barcode=" + barcode, new Function<JSONObject, Void>() {
                                @Override
                                public Void apply(JSONObject jsonObject) {

                                    return null;
                                }
                            }, new Function<String, Void>() {
                                @Override
                                public Void apply(String s) {
                                    if(s.equals("null")){


                                    }
                                    else{
                                        Toast.makeText(container.getContext(),s,Toast.LENGTH_SHORT);
                                    }
                                    return null;
                                }
                            });
                        }
                    }
                    return false;
                }
            });

        }
    }

    public static  class SearchControl extends Control.SearchControl {

        public SearchControl(List<Control.ControlBase> controls) {
            super("UnitId","Item",controls,"InvItem","Description");
            setIsRequired(false);
        }
    }

    private static SearchControl UnitIdSearchControl;

    public static class InvCheckInLineDetailedControl extends Control.DetailedControl {
        public InvCheckInLineDetailedControl() {
            super("InvCheckInLines", "Items","InvCheckInLine","CheckInId");
        }
        @Override
        protected ArrayList<Control.ControlBase> getControls(String action) {
            ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
            if(action == Control.ACTION_ADD || action == Control.ACTION_EDIT || action == Control.ACTION_REFRESH){
                ArrayList<Control.ControlBase> list = new ArrayList<Control.ControlBase>();
                controls.add(new BarcodeTextControl());
                controls.add(Control.getEditDecimalControl("Qty","Qty").setDecimalPlaces(3));
                controls.add(Control.getEditTextControl("Description","Description").setControlSize(Control.CONTROL_SIZE_DOUBLE));
                if(action != Control.ACTION_REFRESH) {
                    ArrayList<Control.ControlBase> searchControls = new ArrayList<Control.ControlBase>();
                    searchControls.add(Control.getEditTextControl("Description","Description"));
                    searchControls.add(Control.getEditTextControl("Unit","Unit"));
                    searchControls.add(Control.getEditDecimalControl("Fraction","Frac").setDecimalPlaces(3));
                    UnitIdSearchControl = new SearchControl(searchControls);
                    controls.add(UnitIdSearchControl);
                    controls.add(Control.getImageControl("Images", "Item Images", "InvCheckInLine"));
                }
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
        public void onButtonClick(Control.ActionButton action) {
            BaseActivity activity = (BaseActivity)action.getButton().getContext();
            if(action.getName() == Control.ACTION_ADD){
                PopupLookup.create("SupplierPicker",suppliers,0L,(supplier)->{
                    supplierId = supplier.getId();
                    PopupLookup.create("EmployeePicker",employees,0L,(employee)->{
                        employeeId = employee.getId();
                        super.onButtonClick(action);
                        return true;
                    }).show(activity.getSupportFragmentManager(),null);
                    return true;

                }).show(activity.getSupportFragmentManager(),null);

            }
            else {
                super.onButtonClick(action);
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
                controls.add(Control.getLookupListControl("SupId","Supplier","Supplier",suppliers));
                controls.add(Control.getEditTextControl( "Status", "Status"));
                return controls;
            }
            else if(action.equals(Control.ACTION_ADD ) || action.equals(Control.ACTION_EDIT)){
                controls.add(Control.getDateTimeControl("CheckInTime", "Check In Date").setValue(new Date()));
                controls.add(Control.getEditTextControl("RefNum", "Ref Number"));
                if(action.equals(Control.ACTION_ADD)){
                    controls.add(Control.getLookupListControl( "SupId", "Supplier", "Supplier",suppliers).readValue(supplierId));
                    controls.add(Control.getLookupListControl( "EmpId", "Employee", "Employee",employees).readValue(employeeId));
                }
                else{
                    controls.add(Control.getLookupListControl( "SupId", "Supplier","Supplier", suppliers));
                    controls.add(Control.getLookupListControl( "EmpId", "Employee","Employee", employees));
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
