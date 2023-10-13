package com.example.myapplication;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupLookup;
import com.example.myapplication.model.PopupSearch;
import com.example.myapplication.model.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;

public  class InvCheckInActivity extends BaseActivity {
    public InvCheckInActivity(){
        Controls.add(new InvCheckInDetailedControl());
    }
    public static  class ItemSearchPopup extends PopupSearch
    {
        public ItemSearchPopup(Function3<DataService.Lookup,String,String,Boolean> onItemSelected){
            OnItemSelected = onItemSelected;

            ArrayList<Control.ControlBase> searchControls = new ArrayList<Control.ControlBase>();
            searchControls.add(Control.getEditTextControl("Description","Description"));
            searchControls.add(Control.getEditTextControl("Unit","Unit"));
            searchControls.add(Control.getEditDecimalControl("Fraction","Frac").setDecimalPlaces(3));
            setArgs(new PopupSearch.PopupSearchArgs("Item Picker",searchControls,"InvItem","FullDescription"));
            setOnItemSelected(new Function<DataService.Lookup, Boolean>() {
                @Override
                public Boolean apply(DataService.Lookup lookup) {
                    String description = null;
                    if(lookup !=null && lookup.getName() != null && lookup.getName().indexOf('\n') > 0)
                        description =  lookup.getName().substring(lookup.getName().indexOf('\n') +1);
                    return onItemSelected.invoke(lookup,description,null);


                }
            });
        }

        private Function3<DataService.Lookup,String,String,Boolean> OnItemSelected;

        @Override
        protected void onKeyPress(View view, int keycode) {
            if(keycode == 10){
                if( SearchEditText.getText() !=null){
                    String barcode = SearchEditText.getText().toString().trim();
                    if(barcode != null && barcode.length() !=0 && barcode.indexOf(' ') <0){
                        new DataService().getObject("InvItem?barcode=" + barcode, new Function<JSONObject, Void>() {
                            @Override
                            public Void apply(JSONObject jsonObject) {
                                if(jsonObject == null){

                                    SearchEditText.setText(barcode);
                                    SearchEditText.selectAll();
                                    SearchEditText.requestFocus();

                                }
                                else{
                                    try {
                                        Long id = Long.parseLong(jsonObject.get(getArgs().getIdField()).toString());
                                        String display = "[Unknown]";
                                        try {
                                            display = jsonObject.get(getArgs().getDisplayField()).toString();
                                        }
                                        catch (JSONException e){

                                        }
                                        DataService.Lookup lookup = new DataService.Lookup();
                                        lookup.setId(id);
                                        lookup.setName(display);
                                        String description = null;
                                        if(display.indexOf('\n') > 0)
                                            description =  display.substring(display.indexOf('\n') +1);
                                        if(OnItemSelected.invoke(lookup,description,barcode))doCancel();
                                    }
                                    catch (JSONException e)
                                    {
                                        Toast.makeText(getRootActivity(), "GetListData Failed," + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                }
                                return null;
                            }
                        },view.getContext());


                    }
                }
            }
            else{
                super.onKeyPress(view, keycode);
            }
        }
    }
    public static  class ItemSearchControl extends Control.LookupControlBase {
        private transient  Function3<DataService.Lookup,String,String,Boolean> OnItemSelected;
        public ItemSearchControl(Function3<DataService.Lookup,String,String,Boolean> onItemSelected) {
            super("UnitId", "Item", "FullDescription");
            //setValue(ItemSearchPopup.AddLookup);
            getButtons().add(new Control.ActionButton(Control.ACTION_SEARCH));
            OnItemSelected  = onItemSelected;
        }
        @Override
        protected void onButtonClick(Control.ActionButton button) {
            if(button.getName() == Control.ACTION_SEARCH){
                PopupSearch ps = new ItemSearchPopup(new Function3<DataService.Lookup, String, String, Boolean>() {
                    @Override
                    public Boolean invoke(DataService.Lookup lookup, String s, String s2) {
                        setValue(lookup);
                        OnItemSelected.invoke(lookup,s,s2);
                        return true;
                    }
                });
                ps.show(((BaseActivity)button.getButton().getContext()).getSupportFragmentManager(),null);
            }
        }
    }



    public static class InvCheckInLineDetailedControl extends Control.DetailedControl {
        public InvCheckInLineDetailedControl() {
            super("InvCheckInLines", "Items","InvCheckInLine","CheckInId");
        }


        public DataService.Lookup AddLookup;
        public String AddDescription;
        public String AddBarcode;

        @Override
        public void onButtonClick(Control.ActionButton action) {
            BaseActivity activity = (BaseActivity)action.getButton().getContext();
            if(action.getName() == Control.ACTION_ADD){
                AddLookup = null;
                AddDescription = null;
                PopupSearch ps = new ItemSearchPopup(new Function3<DataService.Lookup, String, String, Boolean>() {
                    @Override
                    public Boolean invoke(DataService.Lookup lookup, String s, String s2) {
                        AddLookup = lookup;
                        AddDescription  = s;
                        AddBarcode  = s2;
                        InvCheckInLineDetailedControl.super.onButtonClick(action);
                        return true;
                    }
                });
                ps.show(((BaseActivity)action.getButton().getContext()).getSupportFragmentManager(),null);
            }
            else {
                super.onButtonClick(action);
            }
        }


        @Override
        protected ArrayList<Control.ControlBase> getControls(String action) {
            ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
            if(action == Control.ACTION_ADD || action == Control.ACTION_EDIT || action == Control.ACTION_REFRESH){
                ArrayList<Control.ControlBase> list = new ArrayList<Control.ControlBase>();
                if(action != Control.ACTION_REFRESH) {
                    controls.add(new ItemSearchControl(new Function3<DataService.Lookup, String, String, Boolean>() {
                        @Override
                        public Boolean invoke(DataService.Lookup lookup, String s, String s2) {
                            controls.get(2).setValue(s);
                            if(s2 != null && s2.length() != 0)controls.get(0).setValue(s2);
                            return null;
                        }
                    }).setValue(AddLookup));
                }
                controls.add(Control.getEditTextControl("Description","Description").setControlSize(Control.CONTROL_SIZE_DOUBLE).setValue(AddDescription));
                controls.add(Control.getEditDecimalControl("Qty","Qty").setDecimalPlaces(3));
                controls.add(Control.getEditTextControl("Barcode","Barcode").setValue(AddBarcode).setIsRequired(false));
                if(action != Control.ACTION_REFRESH) {
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
            setEnableScroll(true);
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
                    controls.add(Control.getLookupListControl( "SupId", "Supplier", "Supplier",suppliers).readValueObject(supplierId));
                    controls.add(Control.getLookupListControl( "EmpId", "Employee", "Employee",employees).readValueObject(employeeId));
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
