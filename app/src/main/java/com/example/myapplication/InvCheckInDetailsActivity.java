package com.example.myapplication;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Activity.Item;
import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupLookup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.function.Function;

public  class InvCheckInDetailsActivity extends BaseActivity {
    private final  Control.HeaderControl headerControl =  new Control.HeaderControl("Header","Header").setControlSize(ViewGroup.LayoutParams.MATCH_PARENT);
    private final  BalanceControl balance1Control =  new BalanceControl("Total","Total").setFlexBasisPercent(0.33f);
    private final  BalanceControl balance2Control =  new BalanceControl("Added","Added").setFlexBasisPercent(0.34f);;
    private final  BalanceControl balance3Control =  new BalanceControl("Balance","Balance").setFlexBasisPercent(0.33f);


    private final   InvCheckInDetailsActivity.InvCheckInLineDetailedControl itemControl = new InvCheckInDetailsActivity.InvCheckInLineDetailedControl();

    public InvCheckInDetailsActivity(){
        Controls.add(headerControl);
        BarcodeControl barcodeControl = new BarcodeControl();
        Controls.add(barcodeControl);
        Controls.add(balance1Control);
        Controls.add(balance2Control);
        Controls.add(balance3Control);
        Controls.add(itemControl);
        barcodeControl.listener = new BarcodeControl.OnBarcodeScannedListener() {
            @Override
            public void onBarcodeScanned(String barcode, DataService.Lookup lookup) {
                itemControl.onBarcodeScanned(barcode, lookup);

            }
        };
    }
    public static long  checkInId =0;
    public static JSONObject row = null;
    public static Double TotalAmount = 0d;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            row = new JSONObject(getIntent().getStringExtra("row"));
            checkInId = row.getLong("Id");
            TotalAmount = row.getDouble("TotalAmount");
            balance1Control.setValue(TotalAmount);

            // Now you can use row.getString("key") etc.
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String refNum = row.optString("RefNum", "");
        String partyName = "";
        JSONObject busParty = row.optJSONObject("BusParty");
        if (busParty != null) {
            partyName = busParty.optString("Name", "");
        }
        if (!refNum.isEmpty() || !partyName.isEmpty()) {
            headerControl.setValue(refNum + "-" + partyName);
        }
        itemControl.setPath("InvCheckIns[" + checkInId + "]");
        itemControl.setVirtualDelete(true);
        itemControl.setParentId(checkInId);
        itemControl.setEnableScroll(true);
        super.onCreate(savedInstanceState);


        itemControl.setOnRefreshListener(new InvCheckInLineDetailedControl.OnRefreshListener() {
            @Override
            public void onRefreshListener(JSONArray data, Double amount) {
                balance2Control.setValue(amount);
                balance3Control.setValue(TotalAmount - amount);
            }
        });


        itemControl.refreshGrid();
    }
    public  static  class BalanceControl extends Control.EditDecimalControl {
        public BalanceControl(String name, String caption) {
            super(name, caption);
            setControlSize(0);
            setIsRequired(false);
        }
        public BalanceControl setFlexBasisPercent(float controlWeight) {
            super.setFlexBasisPercent(controlWeight);
            return  this;
        }
        @Override
        public void addValueView(ViewGroup container) {
            super.addValueView(container);
            TextView editText = getEditTextInput();
            editText.setFocusable(false);
            editText.setClickable(false);
        }
    }

    public  static  class BarcodeControl extends Control.EditTextControl {
        public BarcodeControl() {
            super("Item", null);
            getButtons().add(new Control.ActionButton(Control.ACTION_KEYBOARD));
            setControlSize(ViewGroup.LayoutParams.MATCH_PARENT);
            setIsRequired(false);
        }

        public  interface OnBarcodeScannedListener {
            void onBarcodeScanned(String barcode, DataService.Lookup lookup);
        }
        private transient OnBarcodeScannedListener listener;
        // Method to set the callback
        public void setOnBarcodeScannedListener(OnBarcodeScannedListener listener) {
            this.listener = listener;
        }
        @Override
        public void addValueView(ViewGroup container) {
            super.addValueView(container);
            EditText editText = getEditTextInput();
            editText.setShowSoftInputOnFocus(false);
            editText.setSingleLine(true);
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
            editText.setImeOptions(EditorInfo.IME_ACTION_NONE);
            editText.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    String barcode = editText.getText().toString().trim();
                    if(barcode != null && barcode.length() !=0 && barcode.indexOf(' ') <0){

                        new DataService().getObject("InvItem/Get?barcode=" + barcode, new Function<JSONObject, Void>() {
                            @Override
                            public Void apply(JSONObject jsonObject) {
                                if(jsonObject == null){
                                    editText.setText(barcode);
                                    editText.selectAll();
                                    editText.requestFocus();
                                    if(listener !=null)listener.onBarcodeScanned(barcode,null);
                                }
                                else{
                                    try {

                                        var lookup = new DataService.Lookup(Long.parseLong(jsonObject.get("Id").toString()),jsonObject.get("FullDescription").toString());
                                        if(listener !=null)listener.onBarcodeScanned(barcode,lookup);
                                        editText.setText(null);
                                        editText.requestFocus();
                                    }
                                    catch (JSONException e)
                                    {
                                        System.out.println(e.getMessage());
                                        Toast.makeText(getRootActivity(), "GetListData Failed," + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                                return null;
                            }
                        }, editText.getContext());
                    }
                    return true; // consume event
                }
                return false;
            });
            // Request focus after a short delay to ensure the view is ready.
            new Handler().postDelayed(editText::requestFocus, 2000);
        }

        @Override
        protected void onButtonClick(Control.ActionButton button) {
            super.onButtonClick(button);
            EditText editText = getEditTextInput();
            if(button.getName().equals(Control.ACTION_KEYBOARD)){
                InputMethodManager imm = (InputMethodManager) getRootActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                // The button's job is to explicitly SHOW the keyboard for manual entry.
                // The user can hide it with the system back button.
                editText.requestFocus();
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }


    public static  class ItemSearchControl extends Control.SearchControlBase {





        private static final String ItemFormula = "{0}.InvItemUnit == null ? {0}.Description : {0}.InvItemUnit.ItemNumber + \" \" + {0}.InvItemUnit.Code + \" \" + {0}.InvItemUnit.Fraction + \"\r\n\" + {0}.InvItemUnit.InvItem.Description";
        public ItemSearchControl() {
            super("InvItemUnit", "Item",null ,"FullDescription");
            setFormula(ItemFormula);
            setButtonHeader(true);
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
            if(button.getName().equals(Control.ACTION_ADD)){

                ArrayList<Control.ControlBase> controls = new Item.PopupItemForm("",getFullPath()).getArgs().getControls();
                addNewRecord(getFullPath(),controls, getFullPath());
            }
            else if(button.getName().equals(Control.ACTION_ADD_SUB)){
                new DataService().postForSelect(DataService.Lookup.class, "InvItemUnits[" + getValue().getId() + "]", "new {InvItem.Id,InvItem.Description as Name}", lookup -> {
                    ArrayList<Control.ControlBase> controls = new ArrayList<>();
                    controls.add(0,Control.getLookupForeignControl("InvItem","Item Unit","Description").setValue(lookup));
                    controls.add(Control.getEditTextPickerControl("Code","Unit",getUnits(),null).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS));
                    controls.add(Control.getEditDecimalControl("Fraction","Fraction").setDecimalPlaces(3));
                    controls.add(Control.getEditDecimalControl("SalesRate","Sales Rate").setIsRequired(false));
                    //controls.add(Control.getEditDecimalControl("SalesRate1","Rate2").setColumnWidth(200).setIsRequired(false));
                    addNewRecord(getFullPath(),controls, getFullPath());
                    return null;
                }, getRootActivity());
            }
            else if(button.getName().equals(Control.ACTION_INBOX)){
                new DataService().postForList(DataService.Lookup.class,
                        "InvItemUnits[" + getValue().getId() + "].InvItem.InvItemUnits[]",
                        "new {Id, Code + \" \" + Fraction as Name}",
                        "", "Code + \" \" + Fraction",
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
                            pl.getArgs().setHeader("Change Unit");
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
            super("InvCheckInLines", "");
            getButtons().add(0,new Control.ActionButton(Control.ACTION_BACK));
            getButtons().add(1,new Control.ActionButton(Control.ACTION_SEARCH));
            getButtons().add(2,new Control.ActionButton(Control.ACTION_BARCODE).setEnabled(false));
            getButtons().add(3,new Control.ActionButton(Control.ACTION_CAMERA).setEnabled(false));
            getButtons().remove(getButton(Control.ACTION_REFRESH));
        }
        @Override
        public void refreshDetailedView(JSONArray data) {
            super.refreshDetailedView(data);
            if(listener != null) {
                var amount = getAggregateSum(data, "Amount");
                listener.onRefreshListener(data,amount);
            }
        }
        public interface OnRefreshListener {
            void onRefreshListener(JSONArray data, Double amount);
        }
        private transient InvCheckInLineDetailedControl.OnRefreshListener listener;
        // Method to set the callback
        public void setOnRefreshListener(InvCheckInLineDetailedControl.OnRefreshListener listener) {
            this.listener = listener;
        }


        public void onBarcodeScanned(String barcode,DataService.Lookup lookup)
        {
            AdditemLookup = lookup;
            AddBarcode = barcode;
            super.onButtonClick(getButton(Control.ACTION_ADD));
            AdditemLookup = null;
            AddBarcode = null;
        }

        @Override
        protected void selectRow(TableRow row, TableRow header, TableLayout tableLayout) {
            super.selectRow(row, header, tableLayout);
            JSONObject data = (JSONObject)row.getTag();
            try {
                getActionButton(Control.ACTION_BARCODE).setEnabled(data.getBoolean("ItemFound"));
            } catch (JSONException e) {
                getActionButton(Control.ACTION_BARCODE).setEnabled(false);
            }
            getActionButton(Control.ACTION_CAMERA).setEnabled(true);
        }
        public String AddDescription;
        public String AddBarcode = null;

        public DataService.Lookup AdditemLookup = null;
        private boolean clickAdd = true;
        private  String editMode = "Default";
        @Override
        public void onButtonClick(Control.ActionButton action) {
            //AddDescription = null;
            //AddBarcode  = null;
            clickAdd = true;
            editMode = "Default";

            if(action.getName().equals(Control.ACTION_SEARCH)){
                clickAdd = false;
                super.onButtonClick(getButton(Control.ACTION_ADD));
            }
            else if(action.getName().equals(Control.ACTION_BARCODE)){
                Item.PopupItemBarcode pib = new Item.PopupItemBarcode();
                pib.setArgs(new Item.PopupItemBarcode.PopupItemBarcodeArgs("Barcode", "InvCheckInLines[" + getValue() + "].InvItemUnit.InvItem"    ).setEnableScroll(true));
                pib.show(((BaseActivity)action.getButton().getContext()).getSupportFragmentManager(),null);
            }
            else if(action.getName().equals(Control.ACTION_BACK)){
                Intent intent = new Intent(getRootActivity(),InvCheckInActivity.class);
                intent.putExtra("Id", checkInId);
                getRootActivity().startActivity(intent);
            }
            else if(action.getName().equals(Control.ACTION_CAMERA)){
                editMode = "Camera";
                super.onButtonClick(getActionButton(Control.ACTION_EDIT));
            }
            else {
                super.onButtonClick(action);
            }
        }
        //private InvCheckInPriceDetailedControl priceListControl = null;
        private Control.EditDecimalControl qtyControl = null;
        private Control.EditTextControlBase descriptionControl = null;
        @Override
        protected ArrayList<Control.ControlBase> getControls(String action) {
            ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
            if(action.equals(Control.ACTION_ADD) || action.equals(Control.ACTION_EDIT) || action.equals(Control.ACTION_REFRESH)){
                if(editMode.equals("Camera") && action.equals(Control.ACTION_EDIT)){
                    controls.add(Control.getImageControl("Images", "Item Images", "InvCheckInLine").setIsRequired(false));
                }
                else {
                    ArrayList<Control.ControlBase> list = new ArrayList<Control.ControlBase>();
                    if (!action.equals(Control.ACTION_REFRESH)) {
                        ItemSearchControl isc = new ItemSearchControl();
                        isc.setValue(AdditemLookup);
                        isc.setPopupIndex(clickAdd ? -1 : 0).setIsRequired(false);
                        descriptionControl = Control.getEditTextControl("Description", "Description").setControlSize(Control.CONTROL_SIZE_DOUBLE).setIsRequired(true).setValue(AddDescription);
                        if(AdditemLookup !=null)descriptionControl.setIsRequired(false);
                        isc.setValueChangedListener((lookup, lookup2) -> {
                            descriptionControl.setIsRequired(lookup2 == null);
                            return null;
                        });
                        controls.add(isc);
                        controls.add(descriptionControl);
                    }
                    qtyControl = Control.getEditDecimalControl("Qty", "Qty");
                    qtyControl.setDecimalPlaces(3).setColumnWeight(3);
                    controls.add(qtyControl);
                    Control.EditDecimalControl amountControl = Control.getEditDecimalControl("Amount", "Amt +VAT");
                    amountControl.setAggregate(Control.AGGREGATE_SUM);
                    amountControl.setIsRequired(true).setColumnWeight(3);
                    amountControl.addButton(Control.ACTION_PERCENT, new Function<View, Boolean>() {
                        @Override
                        public Boolean apply(View view) {
                            Control.EditDecimalControl amt = getControl("Amount");
                            if (amt.getValue() != null) {
                                Double v = amt.getValue() + (amt.getValue() * 5 / 100);
                                amt.setValue(v);
                            }
                            return null;
                        }
                    });
                    controls.add(amountControl);
                    if (action.equals(Control.ACTION_REFRESH)) {
                        controls.add(Control.getHiddenControl("ItemFound").setFormula("{0}.UnitId != null"));
                        controls.add(Control.getEditTextControl("FullDescription", "Description").setColumnWeight(8).setFormula(ItemSearchControl.ItemFormula));
                    }
                    else{
                        controls.add(Control.getEditTextControl("Barcode", "Barcode").setIsRequired(false).setValue(AddBarcode));
                    }
                }
                return controls;
            }
            else{
                return null;
            }
        }
    }
}
