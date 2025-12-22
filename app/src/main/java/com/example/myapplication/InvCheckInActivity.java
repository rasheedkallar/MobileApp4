package com.example.myapplication;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
    public InvCheckInActivity(){
        Controls.add(new InvCheckInDetailedControl());
    }
    public static  class ItemSearchControl extends Control.SearchControlBase {
        private static String ItemFormula = "{0}.InvItemUnit == null ? null : {0}.InvItemUnit.ItemNumber + \" \" + {0}.InvItemUnit.Code + \" \" + {0}.InvItemUnit.Fraction + \"\r\n\" + {0}.InvItemUnit.InvItem.Description";
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
        @Override
        public boolean doAfterSaved(Long id, boolean defaultClose, PopupForm.PopupFormArgs args) {
            boolean saved = super.doAfterSaved(id, defaultClose, args);
            InvCheckInPriceDetailedControl currentRate = ( InvCheckInPriceDetailedControl)args.getControls().stream().filter(i-> i.getName().equals("InvItemUnits")).findFirst().get();
            currentRate.save();
            return saved;
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
        private InvCheckInPriceDetailedControl priceListControl = null;
        private Control.EditDecimalControl amountControl = null;
        private Control.EditDecimalControl qtyControl = null;
        @Override
        protected ArrayList<Control.ControlBase> getControls(String action) {
            ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
            if(action == Control.ACTION_ADD || action == Control.ACTION_EDIT || action == Control.ACTION_REFRESH){
                ArrayList<Control.ControlBase> list = new ArrayList<Control.ControlBase>();
                if(action != Control.ACTION_REFRESH) {
                    priceListControl = new InvCheckInPriceDetailedControl();
                    ItemSearchControl isc = new ItemSearchControl();
                    isc.setPopupIndex(clickAdd?-1:0);
                    isc.setValueChangedListener((lookup, lookup2) -> {
                        if (priceListControl != null) {
                            priceListControl.ItemUnitId = lookup2.getId();
                            priceListControl.setPath(null);
                            if (priceListControl.getTable() != null) {
                                priceListControl.setVisible(true);
                                priceListControl.refreshGrid();
                            }
                        }
                        return null;
                    });
                    controls.add(isc);
                    controls.add(Control.getEditTextControl("Description","Description").setControlSize(Control.CONTROL_SIZE_DOUBLE).setIsRequired(false).setValue(AddDescription));
                }
                qtyControl = Control.getEditDecimalControl("Qty","Qty");
                qtyControl.setDecimalPlaces(3).setColumnWeight(3);
                controls.add(qtyControl);
                amountControl = Control.getEditDecimalControl("Amount","Amt +VAT");
                amountControl.setAggregate(Control.AGGREGATE_SUM);
                amountControl.setIsRequired(false).setColumnWeight(3);
                amountControl.addButton(Control.ACTION_PERCENT, new Function<View, Boolean>() {
                    @Override
                    public Boolean apply(View view) {
                        Control.EditDecimalControl amt = getControl("Amount");
                        if(amt.getValue() != null){
                            Double v  = amt.getValue() + (amt.getValue() * 5 /100);
                            amt.setValue(v);
                        }
                        return null;
                    }
                });
                if(action == Control.ACTION_ADD || action == Control.ACTION_EDIT) {
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
                controls.add(amountControl);
                if(action == Control.ACTION_REFRESH) {
                    controls.add(Control.getEditTextControl("FullDescription","Description").setColumnWeight(8).setFormula(ItemSearchControl.ItemFormula));
                }
                else{
                    controls.add(priceListControl);
                    controls.add(Control.getImageControl("Images", "Item Images", "InvCheckInLine").setIsRequired(false));
                }
                return controls;
            }
            else{
                return null;
            }
        }
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
                controls.add(Control.getDateControl("from","From").setValue(Utility.AddDay(new Date(),-10)));
                controls.add(Control.getDateControl("to","To").setValue(Utility.AddDay(new Date(),1)));
                return controls;
            }
            else if(action.equals(Control.ACTION_REFRESH)){
                ArrayList<Control.ControlBase> list = new ArrayList<Control.ControlBase>();
                controls.add(Control.getDateTimeControl("CheckInTime","Date").setColumnWeight(4));
                controls.add(Control.getEditTextControl("RefNum","Ref#").setColumnWeight(3));
                controls.add(Control.getEditTextControl("BusEmployee.Code","Em").setColumnWeight(2));
                controls.add(Control.getLookupForeignControl("BusParty","Supplier","Name").setColumnWeight(9));
                controls.add(Control.getHiddenControl( "Status", null));
                return controls;

            }
            else if(action.equals(Control.ACTION_ADD ) || action.equals(Control.ACTION_EDIT)){
                controls.add(Control.getDateTimeControl("CheckInTime", "Check In Date").setValue(new Date()));
                controls.add(Control.getEditTextControl("RefNum", "Ref Number"));
                if(action.equals(Control.ACTION_ADD)){
                    controls.add(Control.getHiddenControl( "Status", "Draft"));
                 }

                controls.add(Control.getLookupForeignControl( "BusParty", "Supplier","Name"));
                controls.add(Control.getLookupForeignControl( "BusEmployee", "Employee","BusParty.Name"));

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
