package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.ColorUtils;

import com.example.myapplication.Activity.Item;
import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupForm;
import com.example.myapplication.model.PopupHtml;
import com.example.myapplication.model.PopupInput;
import com.example.myapplication.model.PopupSearch;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InspectUnitActivity extends BaseActivity {
    //protected transient EditText SearchEditText;
    protected transient LinearLayout RootLayout;
    protected transient ScrollView Scroll;
    protected static String Select = "it0=> new {it0.InvItemUnits.OrderByDescending(Id).Select(it1=> new {it1.Id, it1.ItemNumber + \" \" + it1.Code +  it1.Fraction as Description,it1.SalesRate,it1.SalesRate1, it1.InvItem.PurchaseRate * it1.Fraction as Cost, it0.Stock/it1.Fraction as Stock }) as InvItemUnits,it0.Id,it0.Description + \"(\" + it0.InvItemGroup.Code + \")\" as Description}";



    public static class PurchaseHistoryControl extends Control.DetailedControl {
        public PurchaseHistoryControl(Long itemId) {
            super("InvTransactionLines", "Purchase History");
            ItemId = itemId;
            setEnableScroll(true);
            getButtons().clear();
            addButton(Control.ACTION_REFRESH);
        }
        private Long ItemId;

        @Override
        protected String getWhere(String action) {
            return "IsDeleted = false and AccTransaction.RefLookup.Attr1 = \"Purchase\" and AccTransaction.Status = \"Final\" and InvItemUnit.ItemId=" + ItemId;
        }

        @Override
        protected int getTake() {
            return 20;
        }

        @Override
        protected String getOrderBy(String action) {
            return "AccTransaction.TranDate desc, Id desc";
        }

        @Override
        protected ArrayList<Control.ControlBase> getControls(String action) {
            ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
            if(action == null)return controls;
            if(action.equals(Control.ACTION_REFRESH)){
                controls.add(Control.getDateControl("AccTransaction.TranDate","Date").setColumnWeight(3));
                controls.add(Control.getEditTextControl("AccTransaction.BusSupplier.Name","Supplier").setColumnWeight(6));
                controls.add(Control.getEditDecimalControl("Qty","Qty").setFormula("{0}.Qty * {0}.Fraction").setColumnWeight(3));
                controls.add(Control.getEditDecimalControl("Rate","Rate").setFormula("({0}.Rate + ISNULL({0}.TaxRate,0)) / {0}.Fraction").setColumnWeight(2));

                return controls;
            }
            else{
                return null;
            }
        }

    }

    public static class ItemInfoControl extends Control.DetailedControl {
        public ItemInfoControl(Long itemId) {
            super(null, "Item Information");
            setEnableScroll(false);
            getButtons().clear();
            addButton(Control.ACTION_REFRESH);
            ItemId = itemId;
        }
        private Long ItemId;
        public void  RefreshData(){

            JSONObject param = new JSONObject();
            try {
                param.put("Id1",ItemId);
                param.put("Param1","ItemInfo");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            new DataService().postForExecuteList("sp_DataInspection", param, new Function<JSONArray, Void>() {
                @Override
                public Void apply(JSONArray jsonArray) {

                    refreshDetailedView(jsonArray);
                    return null;
                }
            }, getRootActivity());
        }

        @Override
        public void refreshDetailedView(JSONArray data) {
            Data = data;
            super.refreshDetailedView(data);
        }
        public transient  JSONArray Data = null;
        @Override
        public void onButtonClick(Control.ActionButton action) {
            if(action.getName() == Control.ACTION_REFRESH){
                RefreshData();
            }
            else {
                super.onButtonClick(action);
            }
        }
        @Override
        protected ArrayList<Control.ControlBase> getControls(String action) {
            ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
            if(action == null)return controls;
            if(action.equals(Control.ACTION_REFRESH)){
                controls.add(Control.getEditTextControl("Description","Description"));
                controls.add(Control.getEditDecimalControl("Value1","ThisYear"));
                controls.add(Control.getEditDecimalControl("Value2","LastYear"));
                return controls;
            }
            else{
                return null;
            }
        }

    }



    public static class PopupItemNewForm extends Item.PopupItemForm
    {
        public PopupItemNewForm() {
            super("New Item","InvItemUnits[]");

            getArgs().getControls().add(new Item.BarcodeDetailedControl("InvItemBarcodes","Barcode").setIsRequired(false));
        }
        @Override
        protected void doAfterSaved(Long id) {
            super.doAfterSaved(id);
            new DataService().postForList("InvItems[]", Select, "it0=> it0 = @0.InvItemUnits.Where(Id=" + id + ").Select(InvItem).FirstOrDefault()", null, array -> {
                try {
                    ((InspectUnitActivity)getRootActivity()).SearchItem(array.getJSONObject(0),id);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                ((InspectUnitActivity)getRootActivity()).SearchControl.setValue(null);
                return null;
            }, getRootActivity());
        }
    }
    public static class PopupItemStockForm extends PopupForm
    {
        public PopupItemStockForm(String header,Long itemUnitId,Double stock,Function<Double,Boolean> callBack) {
            ArrayList<Control.ControlBase> controls = new ArrayList<>();
            controls.add(new Control.EditDecimalControl("CurrentStock","Current Stock").setDecimalPlaces(3).setEnabled(false).setValue(stock).setControlSize(LinearLayout.LayoutParams.MATCH_PARENT));
            controls.add(new Control.EditDecimalControl("NewStock","New Stock").setDecimalPlaces(3).setControlSize(LinearLayout.LayoutParams.MATCH_PARENT));
            PopupForm.PopupFormArgs ara = new PopupFormArgs(header,controls,"InvItemUnits[]",0L);
            ara.setCanceledOnTouchOutside(true);
            ara.setCancelOnDestroyView(true);
            setArgs(ara);
            CallBack = callBack;
            ItemUnitId = itemUnitId;
        }
        private Function<Double,Boolean> CallBack;
        private Long ItemUnitId;
        @Override
        public void doOk() {

            if(!validate()){
                getPopup().getButton(android.app.AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
            else {

                JSONObject obj = new JSONObject();
                try {
                    obj.put("itemUnitId",ItemUnitId);
                    obj.put("stock",getArgs().getControls().get(1).getValue());
                    obj.put("user","mobile");
                    new DataService().postForExecute(Integer.class, "sp_UpdateStock", obj, new Function<Integer, Void>() {
                        @Override
                        public Void apply(Integer integer) {
                            if(CallBack.apply((Double)getArgs().getControls().get(0).getValue()))dismiss();
                            return null;
                        }
                    }, getRootActivity());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    private static Control.LookupForeignControl ItemControl;
    private void  SearchItem(JSONObject obj,Long selectId) {
        RootLayout.removeAllViews();
        //Controls.clear();
        try {
            Long id = obj.getLong("Id");
            DataService.Lookup item = new DataService.Lookup(id, obj.getString("Description"));
            ItemControl = new Control.LookupForeignControl(".", null, "Description").setFormula("{0}.Description + \"(\" + {0}.InvItemGroup.Code + \")\"").setControlSize(ViewGroup.LayoutParams.MATCH_PARENT);
            ItemControl.setPath("InvItems[" + id + "]");
            ItemControl.setValue(item);
            ItemControl.getButtons().clear();
            ItemControl.addButton(Control.ACTION_EDIT, view -> {
                ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
                controls.add(Control.getEditTextControl("Description", "Description").setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS).setControlSize(Control.CONTROL_SIZE_DOUBLE));
                controls.add(Control.getLookupForeignControl("InvItemGroup", "Item Group", "Code"));
                ItemControl.editRecord("InvItems[" + id + "]", controls, ItemControl.getFullPath());
                return null;
            });
            ItemControl.addView(RootLayout);
            //Controls.add(ItemControl);
            Item.InvItemUnitDetails dc = new Item.InvItemUnitDetails(ItemControl.getValue());
            dc.addButton(Control.ACTION_STOCK, view -> {
                JSONObject data1 = (JSONObject) dc.getSelectedRow().getTag();
                try {
                    new PopupItemStockForm(data1.getString("Description"), dc.getValue(), data1.getDouble("Stock"), new Function<Double, Boolean>() {
                        @Override
                        public Boolean apply(Double aDouble) {
                            dc.refreshGrid();
                            return true;
                        }
                    }).show(getSupportFragmentManager(), null);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
            dc.setPath("InvItems[" + id + "]");
            dc.setValue(selectId);
            dc.refreshDetailedView(obj.getJSONArray("InvItemUnits"));
            dc.addView(RootLayout);
            //Controls.add(dc);
            PurchaseHistoryControl pc = new PurchaseHistoryControl(id);
            pc.addView(RootLayout);
            //Controls.add(pc);
            ItemInfoControl info = new ItemInfoControl(id);
            info.addView(RootLayout);
            //Controls.add(info);
            info.RefreshData();

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    public Control.EditTextControl SearchControl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SearchControl = new Control.EditTextControl("Barcode","Search");
        SearchControl.setControlSize(ViewGroup.LayoutParams.MATCH_PARENT);
        SearchControl.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        SearchControl.addButton(Control.ACTION_ADD, view -> {

            PopupItemNewForm item = new PopupItemNewForm();
            item.show(this);
            return null;
        });
        SearchControl.addButton(Control.ACTION_SEARCH, view -> {
            ArrayList<Control.ControlBase> searchControls = new ArrayList<Control.ControlBase>();
            searchControls.add(Control.getEditTextControl("Unit","Unit").setColumnWeight(3));
            searchControls.add(Control.getEditDecimalControl("Fraction","Frac").setDecimalPlaces(3).setColumnWeight(4));
            searchControls.add(Control.getEditTextControl("Description","Description").setColumnWeight(12));
            PopupSearch Popup = PopupSearch.create("Item Search", searchControls, "Description");
            Popup.setListener(new PopupSearch.PopupSearchListener() {
                @Override
                public boolean onItemSelected(TableRow row, JSONObject data, DataService.Lookup lookup) {

                    if(lookup != null){
                        new DataService().postForList("InvItems[]", Select, "it0=> it0 = @0.InvItemUnits.Where(it1=> it1.Id = " + lookup.getId() + ").Select(it1=> it1.InvItem).FirstOrDefault()", null, array -> {
                            try {
                                SearchItem(array.getJSONObject(0),lookup.getId());
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            SearchControl.setValue(null);
                            return null;
                        }, getBaseContext());
                    }
                    return true;
                }
                @Override
                public void onTextChange(EditText editor, int keyCode) {
                    if(keyCode== 32){
                        String text = editor.getText().toString().trim();
                        if(text != null && text.length() !=0){

                            new DataService().getList("InvItem/Get?keyWords=" + text, array -> {
                                Popup.refreshDetailedView(array);
                                return null;
                            }, getBaseContext());
                        }
                    }
                }
                @Override
                public boolean onPressOk() {
                    return false;
                }
            });
            Popup.show(this.getSupportFragmentManager(),null);
            return null;
        });
        SearchControl.addView(Container);
        Scroll = new ScrollView(this);
        ScrollView.LayoutParams scP = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        scP.setLayoutDirection(LinearLayout.HORIZONTAL);
        Scroll.setLayoutParams(scP);
        Container.addView(Scroll);
        RootLayout = new LinearLayout(this);
        LinearLayout.LayoutParams RootLayoutP = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RootLayout.setLayoutParams(RootLayoutP);

        RootLayout.setOrientation(LinearLayout.VERTICAL);
        Scroll.addView(RootLayout);

        SearchControl.getEditTextInput().setMaxLines(2);
        SearchControl.getEditTextInput().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                if(s.length() >= start + after && start + after >1) {
                    int ascii = (int) s.charAt(start + after -1);
                    if(ascii == 10){
                        String barcode = null;
                        if(SearchControl.getEditTextInput().getText() != null)barcode = SearchControl.getEditTextInput().getText().toString().trim();
                        SearchControl.getEditTextInput().setText(barcode);
                        SearchControl.getEditTextInput().selectAll();
                        if(barcode.length() != 0) {
                            RequestParams rp = new RequestParams();
                            rp.add("Barcode",barcode);
                            rp.add("Select",Select);
                            new DataService().postForObject("InvItem/GetItemByBarcode", rp, jsonObject -> {
                                Long unitId = null;
                                try {
                                    if(jsonObject == null || jsonObject.equals(JSONObject.NULL)){
                                        RootLayout.removeAllViews();
                                        return  null;
                                    }
                                    else{
                                        unitId = jsonObject.getLong("UnitId");
                                        SearchItem(jsonObject,unitId);
                                        return null;
                                    }
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }, new Function<String, Void>() {
                                @Override
                                public Void apply(String s) {
                                    return null;
                                }
                            });
                        }
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        SearchControl.getEditTextInput().requestFocus();
    }
}