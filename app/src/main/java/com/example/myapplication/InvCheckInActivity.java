package com.example.myapplication;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputFilter;
import android.text.InputType;
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
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;

public  class InvCheckInActivity extends BaseActivity {
    public InvCheckInActivity(){
        Controls.add(new InvCheckInDetailedControl());
    }





    public static  class ItemSearchControl extends Control.SearchControlBase {

        private static String ItemFormula = "{0}.InvItemUnit == null ? null : {0}.InvItemUnit.ItemNumber.ToString() + \" \" + {0}.InvItemUnit.Code + \" \" + {0}.InvItemUnit.Fraction.ToString() + \"\r\n\" + {0}.InvItemUnit.InvItem.Description";
        public ItemSearchControl() {
            super("InvItemUnit", "Item",null ,"FullDescription");
            setFormula(ItemFormula);
            ArrayList<Control.ControlBase> searchControls = new ArrayList<Control.ControlBase>();
            searchControls.add(Control.getEditTextControl("Unit","Unit").setColumnWeight(3));
            searchControls.add(Control.getEditDecimalControl("Fraction","Frac").setDecimalPlaces(3).setColumnWeight(4));
            searchControls.add(Control.getEditTextControl("Description","Description").setColumnWeight(12));
            setControls(searchControls);
            getButtons().add(new Control.ActionButton(Control.ACTION_ADD));
            getButtons().add(new Control.ActionButton(Control.ACTION_ADD_SUB).setEnabled(false));
            getButtons().add(new Control.ActionButton(Control.ACTION_INBOX).setEnabled(false));
        }
        @Override
        protected void textChange(EditText editor, int keyCode) {
            if(keyCode == 10){
                if( editor.getText() !=null){
                    String barcode = editor.getText().toString().trim();
                    if(barcode != null && barcode.length() !=0 && barcode.indexOf(' ') <0){

                        new DataService().getObject("InvItem/Get?barcode=" + barcode, new Function<JSONObject, Void>() {
                            @Override
                            public Void apply(JSONObject jsonObject) {
                                if(jsonObject == null){
                                    editor.setText(barcode);
                                    editor.selectAll();
                                    editor.requestFocus();
                                }
                                else{
                                    try {
                                        setValue(new DataService.Lookup(Long.parseLong(jsonObject.get("Id").toString()),jsonObject.get("FullDescription").toString()));
                                        //OnItemSelected.invoke(jsonObject.get("Description").toString(),editor.getText().toString().trim());
                                        Popup.dismiss();
                                    }
                                    catch (JSONException e)
                                    {
                                        System.out.println(e.getMessage());
                                        Toast.makeText(getRootActivity(), "GetListData Failed," + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                }

                                return null;
                            }
                        }, editor.getContext());


                    }
                }
            }
            else{
                super.textChange(editor,  keyCode) ;
            }
        }

        @Override
        public void valueChange(DataService.Lookup oldValue, DataService.Lookup newValue) {
            super.valueChange(oldValue,newValue);
            getActionButton(Control.ACTION_ADD_SUB).setEnabled(getValue() != null);
            getActionButton(Control.ACTION_INBOX).setEnabled(getValue() != null);
        }

        private ArrayList<String> getUnits(){
            ArrayList<String> units = new ArrayList<>();
            units.add("PCS");
            units.add("BAG");
            units.add("BOX");
            units.add("CMR");
            units.add("CTN");
            units.add("KG");
            units.add("MTR");
            units.add("PKT");
            units.add("OFR");
            return units;
        }

        DataService.Lookup itemLookup = null;

        @Override
        protected void onButtonClick(Control.ActionButton button) {
            if(button.getName() == Control.ACTION_ADD){
                ArrayList<Control.ControlBase> controls = new Item.PopupItemForm("",getFullPath()).getArgs().getControls();
                addNewRecord(getFullPath(),controls, getFullPath());
            }
            else if(button.getName() == Control.ACTION_ADD_SUB){
                new DataService().postForSelect(DataService.Lookup.class, "InvItemUnits[" + getValue().getId() + "]", "new {InvItem.Id,InvItem.Description as Name}", lookup -> {
                    ArrayList<Control.ControlBase> controls = new ArrayList<>();
                    controls.add(0,Control.getLookupForeignControl("InvItem","Item","Description").setValue(lookup));
                    controls.add(Control.getEditTextPickerControl("Code","Unit",getUnits(),null).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS));
                    controls.add(Control.getEditDecimalControl("Fraction","Fraction").setDecimalPlaces(3));
                    controls.add(Control.getEditDecimalControl("SalesRate","Sales Rate").setIsRequired(false));
                    controls.add(Control.getEditDecimalControl("SalesRate1","Rate2").setColumnWidth(200).setIsRequired(false));
                    addNewRecord(getFullPath(),controls, getFullPath());
                    return null;
                }, getRootActivity());
            }
            else if(button.getName() == Control.ACTION_INBOX){
                new DataService().postForList(DataService.Lookup.class,
                    "InvItemUnits[" + getValue().getId() + "].InvItem.InvItemUnits[]",
                    "new {Id, Code + \" \" + Fraction.ToString() as Name}",
                    "", "Code + \" \" + Fraction.ToString()",
                lookups -> {
                    PopupLookup pl = PopupLookup.create(getCaption(),lookups,0L,(unit)->{
                        if(unit == null){
                            onButtonClick(getActionButton(Control.ACTION_ADD_SUB));
                        }
                        else {
                            readValueObject(unit.getId());
                        }
                        return true;
                    });
                    pl.getArgs().setNullCaption("Add New Unit");
                    pl.show(getRootActivity().getSupportFragmentManager(),null);
                    return null;
                }, getRootActivity());
            }
            else
            {
                super.onButtonClick(button);
            }
        }
        @Override
        protected void refreshDetailedView(String keywords, Function<JSONArray, Void> callBack) {
            new DataService().getList("InvItem/Get?keyWords=" + keywords, array -> {
                callBack.apply(array);
                return null;
            }, getRootActivity());
        }
    }
    public static class InvCheckInLineDetailedControl extends Control.DetailedControl {
        public InvCheckInLineDetailedControl() {
            super("InvCheckInLines", "Items");
            getButtons().add(0,new Control.ActionButton(Control.ACTION_SEARCH));
            getButtons().add(3,new Control.ActionButton(Control.ACTION_BARCODE).setEnabled(false));
            getButtons().remove(getButton(Control.ACTION_REFRESH));
        }
        @Override
        protected void selectRow(TableRow row, TableRow header, TableLayout tableLayout) {
            super.selectRow(row, header, tableLayout);
            getActionButton(Control.ACTION_BARCODE).setEnabled(true);
        }

        public String AddDescription;
        public String AddBarcode;

        private boolean clickAdd = true;
        @Override
        public void onButtonClick(Control.ActionButton action) {
            BaseActivity activity = (BaseActivity)action.getButton().getContext();

            AddDescription = null;
            AddBarcode  = null;
            clickAdd = true;
            if(action.getName() == Control.ACTION_SEARCH){
                clickAdd = false;
                super.onButtonClick(getButton(Control.ACTION_ADD));
            }
            else if(action.getName() == Control.ACTION_BARCODE){
                Item.PopupItemBarcode pib = new Item.PopupItemBarcode();
                pib.setArgs(new Item.PopupItemBarcode.PopupItemBarcodeArgs("Barcode", "InvCheckInLines[" + getValue() + "].InvItemUnit.InvItem"    ).setEnableScroll(true));
                pib.show(((BaseActivity)action.getButton().getContext()).getSupportFragmentManager(),null);
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
                    controls.add(new ItemSearchControl().setPopupIndex(clickAdd?-1:0));
                    controls.add(Control.getEditTextControl("Description","Description").setControlSize(Control.CONTROL_SIZE_DOUBLE).setIsRequired(false).setValue(AddDescription));
                }
                controls.add(Control.getEditDecimalControl("Qty","Qty").setDecimalPlaces(3).setColumnWeight(3));
                controls.add(Control.getEditDecimalControl("Amount","Amt +VAT").setAggregate(Control.AGGREGATE_SUM).setIsRequired(false).setColumnWeight(3).addButton(Control.ACTION_PERCENT, new Function<View, Boolean>() {
                    @Override
                    public Boolean apply(View view) {
                        Control.EditDecimalControl amt = getControl("Amount");
                        if(amt.getValue() != null){
                            Double v  = amt.getValue() + (amt.getValue() * 5 /100);
                            amt.setValue(v);
                        }
                        return null;
                    }
                }));
                if(action == Control.ACTION_REFRESH) {
                    controls.add(Control.getEditTextControl("FullDescription","Description").setColumnWeight(8).setFormula(ItemSearchControl.ItemFormula));
                }
                else{
                    controls.add(Control.getImageControl("Images", "Item Images", "InvCheckInLine").setIsRequired(false));
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
            super("InvCheckIns", "Stock Receive");
            setEnableScroll(true);
            getButtons().add(new Control.ActionButton(Control.ACTION_STATUS).setEnabled(false));
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
            getActionButton(Control.ACTION_STATUS).setEnabled(true);
        }
        @Override
        public void onButtonClick(Control.ActionButton action) {
            if(action.getName() == Control.ACTION_STATUS){
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
            else {
                super.onButtonClick(action);
            }
        }
        private String SelectedStatus = null;
        @Override
        protected void onRowSelected(TableRow row) {
            super.onRowSelected(row);
            try {
                JSONObject obj = (JSONObject)row.getTag();
                SelectedStatus = obj.get("Status").toString();
            }catch (JSONException e){
                System.out.println(e.getMessage());
                SelectedStatus = null;
            }
        }
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
                controls.add(Control.getDateTimeControl("CheckInTime","Date").setColumnWeight(4));
                controls.add(Control.getEditTextControl("RefNum","Ref#").setColumnWeight(3));
                controls.add(Control.getEditTextControl("BusEmployee.Code","Em").setColumnWeight(2));
                controls.add(Control.getLookupForeignControl("BusSupplier","Supplier","Name").setColumnWeight(9));
                controls.add(Control.getHiddenControl( "Status", null));
                return controls;
            }
            else if(action.equals(Control.ACTION_ADD ) || action.equals(Control.ACTION_EDIT)){
                controls.add(Control.getDateTimeControl("CheckInTime", "Check In Date").setValue(new Date()));
                controls.add(Control.getEditTextControl("RefNum", "Ref Number"));
                if(action.equals(Control.ACTION_ADD)){
                    controls.add(Control.getHiddenControl( "Status", "Draft"));
                 }
                controls.add(Control.getLookupForeignControl( "BusSupplier", "Supplier","Name"));
                controls.add(Control.getLookupForeignControl( "BusEmployee", "Employee","ShortName"));
                controls.add(new InvCheckInLineDetailedControl());
                controls.add(Control.getImageControl( "Images", "Invoice Images","InvCheckIn").setIsRequired(false));
                return controls;
            }
            else{
                return null;
            }
        }
    }

}
