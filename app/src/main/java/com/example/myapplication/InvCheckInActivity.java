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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupForm;
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
        public ItemSearchControl(Function2<String,String,Boolean> onItemSelected) {
            super("InvItemUnit", "Item",null ,"InvCheckInLine","FullDescription");
            setFormula("{0}.InvItemUnit.ItemNumber.ToString() + \" \" + {0}.InvItemUnit.Code + \" \" + {0}.InvItemUnit.Fraction.ToString() + \"\r\n\" + {0}.InvItemUnit.InvItem.Description");

            ArrayList<Control.ControlBase> searchControls = new ArrayList<Control.ControlBase>();
            searchControls.add(Control.getEditTextControl("Description","Description"));
            searchControls.add(Control.getEditTextControl("Unit","Unit"));
            searchControls.add(Control.getEditDecimalControl("Fraction","Frac").setDecimalPlaces(3));
            setControls(searchControls);
            getButtons().add(new Control.ActionButton(Control.ACTION_ADD));
            getButtons().add(new Control.ActionButton(Control.ACTION_ADD_SUB).setEnabled(false));
            getButtons().add(new Control.ActionButton(Control.ACTION_INBOX).setEnabled(false));
            OnItemSelected  = onItemSelected;
            //setAction(ItemFormPopup.SET_ITEM_ACTION);
        }

        private transient Function2<String,String,Boolean> OnItemSelected;

        @Override
        protected void textChange(EditText editor, int keyCode) {

            if(keyCode == 10){
                if( editor.getText() !=null){
                    String barcode = editor.getText().toString().trim();
                    if(barcode != null && barcode.length() !=0 && barcode.indexOf(' ') <0){
                        new DataService().getJObject("InvItem/Get?barcode=" + barcode, new Function<JSONObject, Void>() {
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
                                        OnItemSelected.invoke(jsonObject.get("Description").toString(),editor.getText().toString().trim());
                                        Popup.dismiss();
                                    }
                                    catch (JSONException e)
                                    {
                                        Toast.makeText(getRootActivity(), "GetListData Failed," + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                }
                                return null;
                            }
                        },editor.getContext());

                    }
                }
            }
            else{
                super.textChange(editor,  keyCode) ;
            }
        }
        @Override
        protected boolean itemSelected(TableRow row, JSONObject data, DataService.Lookup lookup) {
            super.itemSelected(row, data, lookup);
            try{
                OnItemSelected.invoke(data.get("Description").toString(),null);
            }catch (JSONException e){
            }
            return true;
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
        public class EditTextControlBarcode extends Control.EditTextControl {
            public EditTextControlBarcode() {
                super("Barcode", "Barcode");
                setIsRequired(false);
                setFormula("null");
            }

            @Override
            protected void updateValueToJSONObject(JSONObject data, String field, Serializable value) {

                if(value !=null && value.toString().length() !=0){
                    try{
                        JSONArray arra = new JSONArray();
                        JSONObject ob = new JSONObject();
                        ob.put("Code",value.toString());
                        arra.put(ob);
                        data.put("InvItemBarcodes",arra);

                    }catch (JSONException e){

                    }



                }
                //super.updateValueToJSONObject(data, field, value);
            }
        }

        DataService.Lookup itemLookup = null;

        @Override
        protected void onButtonClick(Control.ActionButton button) {
            if(button.getName() == Control.ACTION_ADD){
                ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
                if(itemLookup == null){
                    controls.add(Control.getEditTextControl("InvItem.Description","Description").setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS).setControlSize(Control.CONTROL_SIZE_DOUBLE));
                }
                else{
                    controls.add(Control.getLookupForeignControl("InvItem","Item","InvItemUnit","Description").setValue(itemLookup));

                    controls.get(0).getButtons().clear();
                }
                controls.add(Control.getEditTextPickerControl("Code","Unit",getUnits(),null).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS).setValue(itemLookup == null? "PCS" : null));
                controls.add(Control.getEditDecimalControl("Fraction","Fraction").setDecimalPlaces(3).setValue(1.0));
                controls.add(new EditTextControlBarcode());
                if(itemLookup == null) {
                    controls.add(Control.getLookupForeignControl("InvItem.InvItemGroup", "Item Group", "InvItemUnit", "Code").setEnabled(false));
                    controls.add(Control.getHiddenControl("InvItem.ItemTaxId", 1));
                }
                itemLookup = null;
                addNewRecord(getFullPath(),controls,"InvItemUnit",3003);

            }
            else if(button.getName() == Control.ACTION_ADD_SUB){

                new DataService().postForSelect(DataService.Lookup.class, getPath(), "new {InvItemUnit.InvItem.Id,InvItemUnit.InvItem.Description as Name}", new Function<DataService.Lookup, Void>() {
                    @Override
                    public Void apply(DataService.Lookup lookup) {
                        itemLookup  = lookup;
                        onButtonClick(getButton(Control.ACTION_ADD));
                        return null;
                    }
                }, getRootActivity());



            }
            else if(button.getName() == Control.ACTION_INBOX){
                new DataService().getLookupList("InvItem/Get?unitId=" + getValue().getId().toString(), new Function<ArrayList<DataService.Lookup>, Void>() {
                    @Override
                    public Void apply(ArrayList<DataService.Lookup> lookups) {

                        String description = "Unit Picker";
                        if(getValue() !=null && getValue().getName() != null && getValue().getName().indexOf('\n') > 0)
                            description =  getValue().getName().substring(getValue().getName().indexOf('\n') +1);
                        PopupLookup pl = PopupLookup.create(description,lookups,0L,(unit)->{
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
                    }
                },getRootActivity());
            }
            else
            {
                super.onButtonClick(button);
            }
        }
        @Override
        protected void refreshDetailedView(String keywords, Function<JSONArray, Void> callBack) {
            new DataService().getJArray("InvItem/Get?keyWords=" + keywords, new Function<JSONArray, Void>() {
                @Override
                public Void apply(JSONArray array) {
                    callBack.apply(array);
                    return null;
                }
            }, getRootActivity());
        }
    }
    public static class InvCheckInLineDetailedControl extends Control.DetailedControl {
        public InvCheckInLineDetailedControl() {
            super("InvCheckInLines", "Items","InvCheckInLine","CheckInId");
            getButtons().add(0,new Control.ActionButton(Control.ACTION_SEARCH));
            getButtons().add(3,new Control.ActionButton(Control.ACTION_BARCODE).setEnabled(false));
            getButtons().remove(getButton(Control.ACTION_REFRESH));
        }
        @Override
        protected void selectRow(TableRow row, TableRow header, TableLayout tableLayout) {
            super.selectRow(row, header, tableLayout);
            getActionButton(Control.ACTION_BARCODE).setEnabled(true);
        }
        public DataService.Lookup AddLookup;
        public String AddDescription;
        public String AddBarcode;

        private boolean clickAdd = true;
        @Override
        public void onButtonClick(Control.ActionButton action) {
            BaseActivity activity = (BaseActivity)action.getButton().getContext();
            AddLookup = null;
            AddDescription = null;
            AddBarcode  = null;
            clickAdd = true;
            if(action.getName() == Control.ACTION_SEARCH){
                clickAdd = false;
                super.onButtonClick(getButton(Control.ACTION_ADD));
            }
            else if(action.getName() == Control.ACTION_BARCODE){
                new DataService().getLookupList("InvItem/Get?unitId=" + getSelectedId(), new Function<ArrayList<DataService.Lookup>, Void>() {
                    @Override
                    public Void apply(ArrayList<DataService.Lookup> lookups) {

                        try{
                            JSONObject obj = (JSONObject) getSelectedRow().getTag();
                            String description = obj.get("Description").toString();
                            String fullDescription = obj.get("FullDescription").toString();
                            PopupLookup pl = PopupLookup.create(description,lookups,0L,(unit)->{
                                if(unit == null){
                                    onButtonClick(getActionButton(Control.ACTION_ADD_SUB));
                                }
                                else {
                                    readValueObject(unit.getId());
                                }
                                return true;
                            });
                            pl.show(getRootActivity().getSupportFragmentManager(),null);
                        }catch (JSONException e){

                        }
                        return null;
                    }
                },getRootActivity());


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
                    controls.add(new ItemSearchControl(
                        new Function2<String, String, Boolean>()
                        {
                            @Override
                            public Boolean invoke(String description, String barcode) {
                                getControl("Description").setValue(description);
                                if(barcode != null  && barcode.length() != 0)getControl("Barcode").setValue(barcode);
                              return null;
                            }
                        }
                    ).setValue(AddLookup).setPopupIndex(clickAdd?-1:0)
                    .setValueChangedListener(
                            new Function2<DataService.Lookup, DataService.Lookup, Void>()
                            {
                                @Override
                                public Void invoke(DataService.Lookup lookup, DataService.Lookup lookup2) {

                                    String[] values = lookup2.getName().split("\r\n");
                                    if(values.length>1)
                                        getControl("Description").setValue(values[1]);
                                    else
                                        getControl("Description").setValue(lookup2.getName());
                                    return null;
                                }
                            }
                        )
                    );
                }
                if(action == Control.ACTION_REFRESH)
                    controls.add(Control.getEditTextControl("FullDescription","Description").setFormula("{0}.InvItemUnit == null ? {0}.Description : {0}.InvItemUnit.ItemNumber.ToString() + \" \" + {0}.InvItemUnit.Code + \" \" + {0}.InvItemUnit.Fraction.ToString() + \"\r\n\" + {0}.InvItemUnit.InvItem.Description"));
                else
                    controls.add(Control.getEditTextControl("Description","Description").setControlSize(Control.CONTROL_SIZE_DOUBLE).setValue(AddDescription));
                controls.add(Control.getEditDecimalControl("Qty","Qty").setDecimalPlaces(3));
                controls.add(Control.getEditTextControl("Barcode","Barcode").setValue(AddBarcode).setIsRequired(false));
                if(action != Control.ACTION_REFRESH) {
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
            super("InvCheckIns", "Stock Receive","InvCheckIn",null);
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
            return "Status == \"Draft\" ? 1 : 2, Id Desc";
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
                PopupLookup.create(getCaption(), stats, null, new Function<DataService.Lookup, Boolean>() {
                    @Override
                    public Boolean apply(DataService.Lookup lookup) {
                        new DataService().postForLong("InvCheckIn/UpdateStatus?id=" + getSelectedId() + "&status=" + lookup.getName(),  new RequestParams(), new Function<Long, Void>() {
                            @Override
                            public Void apply(Long aLong) {
                                refreshGrid(table_layout);
                                return null;
                            }
                        },getRootActivity());
                        return true;
                    }
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
                controls.add(Control.getDateTimeControl("CheckInTime","Date").setColumnWidth(225));
                controls.add(Control.getEditTextControl("RefNum","Ref#").setColumnWidth(200));
                controls.add(Control.getEditTextControl("BusEmployee.Code","Em").setColumnWidth(125));
                controls.add(Control.getLookupForeignControl("BusSupplier","Supplier","InvCheckIn","Name").setColumnWidth(500));
                controls.add(Control.getHiddenControl( "Status", null));
                return controls;
            }
            else if(action.equals(Control.ACTION_ADD ) || action.equals(Control.ACTION_EDIT)){
                controls.add(Control.getDateTimeControl("CheckInTime", "Check In Date").setValue(new Date()));
                controls.add(Control.getEditTextControl("RefNum", "Ref Number"));
                if(action.equals(Control.ACTION_ADD)){
                    controls.add(Control.getHiddenControl( "Status", "Draft"));
                 }
                controls.add(Control.getLookupForeignControl( "BusSupplier", "Supplier","InvCheckIn","Name"));
                controls.add(Control.getLookupForeignControl( "BusEmployee", "Employee","InvCheckIn","ShortName"));
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
