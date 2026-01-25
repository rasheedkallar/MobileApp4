package com.example.myapplication;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.TypedValue;
import android.view.Gravity;
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

import androidx.core.graphics.ColorUtils;

import com.example.myapplication.Activity.Item;
import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupForm;
import com.example.myapplication.model.PopupLookup;
import com.example.myapplication.model.Utility;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

import kotlin.jvm.functions.Function2;

public  class InvCheckInDetailsActivity extends BaseActivity {
    private final  Control.HeaderControl headerControl =  new Control.HeaderControl("Header","Header").setControlSize(ViewGroup.LayoutParams.MATCH_PARENT);
    private final  BalanceControl balance1Control =  new BalanceControl("Total","Total");
    private final  BalanceControl balance2Control =  new BalanceControl("Added","Added");
    private final  BalanceControl balance3Control =  new BalanceControl("Balance","Balance");


    private final   InvCheckInDetailsActivity.InvCheckInLineDetailedControl itemControl = new InvCheckInDetailsActivity.InvCheckInLineDetailedControl();
    public static long  checkInId =0;
    public static String header;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        header = getIntent().getStringExtra("header");
        checkInId = getIntent().getLongExtra("Id", 0); // Get the passed ID
        headerControl.setValue(header);

        itemControl.setPath("InvCheckIns[" + checkInId + "]");
        itemControl.setVirtualDelete(true);
        itemControl.setParentId(checkInId);
        itemControl.setEnableScroll(true);
        super.onCreate(savedInstanceState);
        itemControl.refreshGrid();


    }
    public  static  class BalanceControl extends Control.EditDecimalControl {
        public BalanceControl(String name, String caption) {
            super(name, caption);
            setControlSize(ViewGroup.LayoutParams.MATCH_PARENT);
            setIsRequired(false);
        }
    }

    public  static  class BarcodeControl extends Control.EditTextControl {
        public BarcodeControl() {
            super("Item", null);
            getButtons().add(new Control.ActionButton(Control.ACTION_KEYBOARD));
            setControlSize(ViewGroup.LayoutParams.MATCH_PARENT);
            setIsRequired(false);
        }

        public interface OnBarcodeScannedListener {
            void onBarcodeScanned(String barcode, DataService.Lookup lookup);
        }
        private OnBarcodeScannedListener listener;
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
                        controls.add(Control.getEditTextControl("Barcode", "Barcode").setIsRequired(false).setValue(AddBarcode));
                    }
                    qtyControl = Control.getEditDecimalControl("Qty", "Qty");
                    qtyControl.setDecimalPlaces(3).setColumnWeight(3);
                    controls.add(qtyControl);
                    Control.EditDecimalControl amountControl = Control.getEditDecimalControl("Amount", "Amt +VAT");
                    amountControl.setAggregate(Control.AGGREGATE_SUM);
                    amountControl.setIsRequired(false).setColumnWeight(3);
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
                    /*


                    if (action == Control.ACTION_ADD || action == Control.ACTION_EDIT) {
                        amountControl.setValueChangedListener(new Function2<Double, Double, Void>() {
                            @Override
                            public Void invoke(Double aDouble, Double aDouble2) {
                                if (priceListControl != null)
                                    priceListControl.updatePurchaseAmount(aDouble2);
                                return null;
                            }
                        });
                        qtyControl.setValueChangedListener(new Function2<Double, Double, Void>() {
                            @Override
                            public Void invoke(Double aDouble, Double aDouble2) {
                                if (priceListControl != null)
                                    priceListControl.updatePurchaseQty(aDouble2);
                                return null;
                            }
                        });
                    }
                    */

                    controls.add(amountControl);
                    if (action.equals(Control.ACTION_REFRESH)) {
                        controls.add(Control.getHiddenControl("ItemFound").setFormula("{0}.UnitId != null"));
                        controls.add(Control.getEditTextControl("FullDescription", "Description").setColumnWeight(8).setFormula(ItemSearchControl.ItemFormula));
                    }
                    //else {
                    //    controls.add(priceListControl);
                    //}
                }
                return controls;
            }
            else{
                return null;
            }
        }

        /*

        public static class InvCheckInPriceDetailedControl extends Control.DetailedControl {
            private static String ItemUnitFormula = "{0}.ItemNumber + \" \" + {0}.Code + \" \" + {0}.Fraction";
            public InvCheckInPriceDetailedControl() {
                super("InvItemUnits", "Price List");
            }
            @Override
            public void refreshGrid(TableLayout table) {
                if(ItemUnitId != 0L){
                    FieldList fields = new FieldList(0);
                    fields.Fields.put("Id","it0.Id");
                    addForSelectQuery(fields);
                    new DataService().postForSelect("InvItemUnits[" + ItemUnitId + "]","it0 => " + fields.getSelectString(), jsonObject -> {
                        readValueJSONObject(jsonObject,getName());
                        return null;
                    },table.getContext());
                }
                else{
                    super.refreshGrid(table);
                }
            }
            @Override
            public void addForSelectQuery(FieldList list) {
                FieldList fields = new FieldList(0);
                fields.Index = 1;
                super.addForSelectQuery(fields);
                String query = fields.getSelectString();
                query = query.substring(query.indexOf('(') + 1,query.lastIndexOf(')'));
                query =  query.replace("it1.InvItemUnits","it1.InvItem.InvItemUnits");
                System.out.println(query);
                String itemUnitIdQuery = "it0.InvItemUnit.Id";
                if(ItemUnitId != 0)itemUnitIdQuery = String.valueOf(ItemUnitId);
                list.addForSelectQuery("InvItemUnits","InvItemUnits","@0.InvItemUnits.Where(it1=> it1.Id = " + itemUnitIdQuery + ").SelectMany(it1=> " + query + ")");
            }
            private  long ItemUnitId = 0;
            private  Double _Qty = 0.0;
            private DecimalFormat df = new DecimalFormat( "0.00");
            @Override
            public void refreshDetailedView(JSONArray data) {
                setVisible(true);
                super.refreshDetailedView(data);
                updatePurchaseRate();
            }
            public void updatePurchaseRate() {
                Double purchaseRate = null;
                if(getTable() != null){
                    for (int i = 0; i < getTable().getChildCount(); i++) {
                        TableRow row = (TableRow) getTable().getChildAt(i);
                        if(row.getTag() != null){
                            JSONObject data = (JSONObject)row.getTag();
                            TextView CurrentRateView = row.findViewWithTag("CurrentRate");
                            Double lastRate = null;
                            Double unitFraction = 0.0;
                            Double fraction = 0.00;
                            try {
                                unitFraction = data.getDouble("UnitFraction");
                                fraction = data.getDouble("Fraction");
                                lastRate = data.getDouble("LastRate");
                            }
                            catch (JSONException e) {

                            }
                            if(_PurchaseAmount != null && _PurchaseQty != null)purchaseRate = _PurchaseAmount/_PurchaseQty /unitFraction * fraction;
                            CurrentRateView.setText(purchaseRate==null?"":df.format(purchaseRate));
                            int c = Color.TRANSPARENT;
                            if(purchaseRate != null && lastRate != null){
                                if(purchaseRate >= lastRate ){
                                    c = ColorUtils.blendARGB(Color.RED,Color.WHITE, (float) (lastRate/purchaseRate));
                                }
                                else {
                                    c = ColorUtils.blendARGB(Color.GREEN,Color.WHITE, (float) (purchaseRate/lastRate));
                                }
                            }
                            c = ColorUtils.blendARGB(c, Color.TRANSPARENT, 0.1F);
                            CurrentRateView.setBackgroundColor(c);
                            updateFinalPercentage(row);
                        }
                    }
                }
            }
            public static void  updateFinalPercentage(TableRow row){
                try{
                    JSONObject data = (JSONObject)row.getTag();
                    TextView CurrentRateView = row.findViewWithTag("CurrentRate");
                    Double currentRate = null;
                    try{
                        currentRate = Double.parseDouble(CurrentRateView.getText().toString());
                    }catch (Exception e){
                    }
                    if(currentRate == null){
                        currentRate = data.getDouble("LastRate");
                    }
                    Double salesRate = null;
                    TextView salesRateView =  row.findViewWithTag("SalesRate");
                    try{
                        salesRate = Double.parseDouble(salesRateView.getText().toString());
                    }
                    catch (Exception e){
                    }
                    TextView marginPerFinalView =  row.findViewWithTag("MarginPerFinal");
                    int c =Color.WHITE;
                    if(salesRate != null && currentRate != null){
                        double marginPer = data.getDouble("MarginPer");
                        double marginPerFinal = (salesRate-currentRate)/ currentRate * 100;
                        if(marginPerFinal>marginPer)  c = ColorUtils.blendARGB(Color.GREEN, Color.WHITE,(float) (marginPer/marginPerFinal));
                        else  c = ColorUtils.blendARGB(Color.RED, Color.WHITE,(float) (marginPerFinal/marginPer));
                        marginPerFinalView.setText(String.valueOf((int) marginPerFinal));
                    }else{
                        marginPerFinalView.setText("");
                    }
                    marginPerFinalView.setBackgroundColor(c);
                }
                catch (Exception e){
                }
            }
            private Double _PurchaseQty;
            public void updatePurchaseQty(Double purchaseQty) {
                if((_PurchaseQty == null && purchaseQty != null) || (_PurchaseQty != null && purchaseQty == null) || !_PurchaseQty.equals(purchaseQty)){
                    _PurchaseQty = purchaseQty;
                    updatePurchaseRate();
                }
            }
            private Double _PurchaseAmount;
            public void updatePurchaseAmount(Double purchaseAmount) {
                if((_PurchaseAmount == null && purchaseAmount != null) || (_PurchaseAmount != null && purchaseAmount == null) || !_PurchaseAmount.equals(purchaseAmount)){
                    _PurchaseAmount = purchaseAmount;
                    updatePurchaseRate();
                }
            }
            @Override
            public String getDataPath(String action) {
                if( action.equals(Control.ACTION_ADD) || action.equals(Control.ACTION_EDIT) || action.equals(Control.ACTION_DELETE)){
                    if(action.equals(Control.ACTION_ADD))return "InvItemUnits[]";
                    else return "InvItemUnits[" + getValue() + "]";
                }
                else{
                    return super.getDataPath(action);
                }
            }
            private int reqCount = -1;
            public void save(){
                reqCount = 0;
                for (int i = 0; i < getTable().getChildCount(); i++) {
                    TableRow row = (TableRow)getTable().getChildAt(i);
                    EditText tv = (EditText)row.findViewWithTag("SalesRate");
                    if(tv != null && tv.getText() !=null && tv.getText().toString().length() != 0){
                        JSONObject obj = (JSONObject)row.getTag();
                        try {
                            long id = obj.getLong("Id");
                            Double currentValue = null;
                            if(tv.getText() != null && tv.getText().length() != 0)currentValue= Double.parseDouble(tv.getText().toString());
                            Double oldValue = null;
                            try{
                                oldValue = obj.getDouble("SalesRate");
                            }
                            catch (Exception e){
                            }
                            if(currentValue == null || !currentValue.equals(oldValue)){
                                JSONObject args = new JSONObject();
                                args.put("SalesRate",currentValue);
                                reqCount++;
                                new DataService().postForSave("InvItemUnits[" + id + "]", args, aLong -> {
                                    tv.setBackgroundColor(Color.TRANSPARENT);
                                    try {
                                        if(tv.getText() != null && tv.getText().length() != 0)obj.put("SalesRate",null);
                                        else obj.put("SalesRate",Double.parseDouble(tv.getText().toString()));
                                        reqCount--;
                                        if(reqCount ==0){
                                            reqCount = -1;
                                            refreshGrid();
                                        }
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                    return null;
                                }, s -> null);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else{
                        System.out.println(i);
                    }
                }
                if(reqCount ==0){
                    reqCount = -1;
                    refreshGrid();
                }
            }
            DataService.Lookup ItemLookup = null;
            @Override
            public void onButtonClick(Control.ActionButton action) {
                if(action.getName().equals(Control.ACTION_ADD)){
                    new DataService().postForSelect(DataService.Lookup.class, "InvItemUnits[" + ItemUnitId + "]", "new {InvItem.Id,InvItem.Description as Name}", lookup -> {
                        ItemLookup = lookup;
                        super.onButtonClick(action);
                        return null;
                    }, getRootActivity());
                }
                else{
                    super.onButtonClick(action);
                }
            }
            @Override
            protected String getOrderBy(String action) {
                return  "Fraction,Code==\"PCS\"?1:2";
            }
            @Override
            protected ArrayList<Control.ControlBase> getControls(String action) {
                ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
                if(action == Control.ACTION_REFRESH) {
                    controls.add(Control.getEditTextControl("Unit","Unit").setColumnWeight(6F).setFormula(ItemUnitFormula));
                    controls.add(Control.getEditDecimalControl("MarginPer","%").setDecimalPlaces(0).setColumnWeight(3F).setFormula("{0}.MarginPer??{0}.InvItem.MarginPer??{0}.InvItem.InvItemGroup.MarginPer"));
                    controls.add(Control.getEditDecimalControl("LastRate","Last").setFormula("{0}.InvItem.PurchaseRate == null?null:{0}.InvItem.PurchaseRate * {0}.Fraction").setColumnWeight(3F));
                    controls.add(Control.getEditDecimalControl("CurrentRate","Now").setColumnWeight(4F).setFormula("0.00"));
                    controls.add(Control.getHiddenControl("Fraction",null).setFormula("{0}.Fraction"));
                    controls.add(Control.getHiddenControl("UnitFraction",null).setFormula("{1}.Fraction").setColumnWeight(4F));
                    controls.add(new SalesRateDecimalControl().setColumnWeight(6F));
                    controls.add(Control.getEditDecimalControl("MarginPerFinal","%").setDecimalPlaces(0).setColumnWeight(3F).setFormula("0.00"));
                }
                else if (action != Control.ACTION_FILTER){
                    if(action == Control.ACTION_ADD){
                        Control.LookupForeignControl item = Control.getLookupForeignControl("InvItem","Item","Description").setValue(ItemLookup);
                        item.getButtons().clear();
                        controls.add(item);
                    }
                    if(action == Control.ACTION_EDIT)controls.add(Control.getLookupForeignControl("InvItem","Item","Description").setVisible(false));
                    if(action == Control.ACTION_EDIT)controls.add(Control.getEditTextControl("InvItem.Description","Item").setControlSize(Control.CONTROL_SIZE_DOUBLE));
                    controls.add(Control.getEditTextPickerControl("Code", "Unit", Item.getUnits(), null).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS));
                    controls.add(Control.getEditDecimalControl("Fraction", "Fraction").setDecimalPlaces(3).setValue(1.0));
                    controls.add(Control.getEditDecimalControl("SalesRate","Rate1").setColumnWidth(200).setIsRequired(false));
                    controls.add(Control.getEditDecimalControl("SalesRate1","Rate2").setColumnWidth(200).setIsRequired(false));
                    if(action == Control.ACTION_EDIT)controls.add(Control.getLookupForeignControl("InvItem.InvItemGroup", "Item Group",  "Code"));
                    if(action == Control.ACTION_EDIT)controls.add(Control.getLookupForeignControl("InvItem.InvItemTax", "Item Tax",  "Code"));
                }
                return controls;
            }
            public static  class SalesRateDecimalControl extends  Control.EditDecimalControl
            {
                public SalesRateDecimalControl() {
                    super("SalesRate", "S Rate");
                }
                private EditText ListText;
                private JSONObject Data;
                @Override
                public void addListDetails(TableRow row) {
                    Data = (JSONObject) row.getTag();
                    ListText = new EditText(row.getContext());
                    ListText.setPadding(0,0,0,0);
                    ListText.setTag(getName());
                    ListText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    ListText.setKeyListener(DigitsKeyListener.getInstance(getDigits()));
                    TableRow.LayoutParams hcP = new TableRow.LayoutParams(getColumnWidth(), TableRow.LayoutParams.MATCH_PARENT,getColumnWidth());
                    ListText.setLayoutParams(hcP);
                    ListText.setPadding(0,0,0,0);
                    ListText.setGravity(Gravity.CENTER_VERTICAL);
                    ListText.setTextAlignment(getTextAlignment());
                    ListText.setText(getFormatValue(getValue()));
                    ListText.setSelectAllOnFocus(getSelectAllOnFocus());
                    ListText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }
                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }
                        @Override
                        public void afterTextChanged(Editable editable) {
                            updateFinalPercentage(row);
                        }
                    });
                    row.addView(ListText);
                }
            }
        }


        */
    }
}
