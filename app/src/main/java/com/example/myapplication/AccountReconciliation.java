package com.example.myapplication;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Activity.Item;
import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupConfirmation;
import com.example.myapplication.model.PopupLookup;
import com.example.myapplication.model.Utility;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

public  class AccountReconciliation extends BaseActivity {
    public AccountReconciliation(){
        setEnableScroll(true);
    }







    private static String Select = "AccTransactionLines.Where(ReconDate == null && AccTransaction.IsDraft == false).OrderByDescending(AccTransaction.TranDate).Select(it1 => new {it1.Id, it1.AccTransaction.TranNumber + \" \" + it1.RefNumber + \" \"  +  it1.Naration as Naration, it1.Debit - it1.Credit as Amount, new {it1.AccTransaction.Id,it1.AccTransaction.TranDate} as AccTransaction})";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null){
            new DataService().postForList("AccDefaults[]",
            "it0=> new{it0.AccLedger.Id,it0.AccLedger.Name,it0.AccLedger.Balance, it0.AccLedger." + Select + " as AccTransactionLines}",
            "Name = \"Ledger Monitor\" or Name = \"Ledger Monitor_" + User + "\"",
            "AccLedger.Name",
            jsonArray -> {
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        AccTransactionLineControl src = new AccTransactionLineControl(obj.getString("Name"),obj.getLong("Id"),obj.getDouble("Balance"));
                        src.refreshDetailedView(obj.getJSONArray("AccTransactionLines"));
                        src.addView(Container);
                        Controls.add(src);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                return null;
            },this);
        }
        else{

        }
    }
    public static class AccTransactionLineControl extends Control.DetailedControl {
        private long Id;
        private Double Balance;
        public AccTransactionLineControl(String caption,Long id,Double balance) {
            super("AccTransactionLines", caption);

            Id = id;
            Balance =balance;
            getButtons().clear();
            addButton(Control.ACTION_CHECKED, view -> {
                    PopupConfirmation.create("Transaction Verification", "Are you sure you want to verify transaction?", unused -> {
                        JSONObject obj = new JSONObject();
                        try {
                            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                            obj.put("ReconDate",format.format(new Date()));
                            new DataService().postForSave("AccTransactionLines[" + getValue() + "]", obj, aLong -> {
                                RefreshData();
                                return null;
                            }, s -> null);

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        return true;
                    }).show(getRootActivity().getSupportFragmentManager(),null);
                return null;
            });
            getButtons().get(0).setEnabled(false);
            addButton(Control.ACTION_REFRESH);
        }
        private transient TextView txtBalance;
        @Override
        protected void addContentView(ViewGroup container) {
            super.addContentView(container);
            txtBalance = new TextView(container.getContext());
            txtBalance.setPadding(10,0,10,0);
            txtBalance.setTextSize(20);
            RelativeLayout.LayoutParams llValueP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            txtBalance.setLayoutParams(llValueP);
            setBalance(Balance);
            container.addView(txtBalance,1);
        }
        private void setBalance(Double balance){
            txtBalance.setText("Balance : " + balance.toString());
            int colour = Color.parseColor("#80FFEB3B");
            if(balance < 0){
                colour = Color.parseColor("#80EE1506");
            }
            else {
                colour = Color.parseColor("#8048BE09");
            }
            txtBalance.setBackgroundColor(colour);
        }
        private void  RefreshData(){
            new DataService().postForSelect("AccLedgers[" + Id + "]", "it0=> new{it0.Id,it0.Name,it0.Balance,it0." + Select + " as AccTransactionLines}", new Function<JSONObject, Void>() {
                @Override
                public Void apply(JSONObject jsonObject) {
                    try {
                        setBalance(jsonObject.getDouble("Balance"));
                        setCaption(jsonObject.getString("Name"));
                        refreshDetailedView(jsonObject.getJSONArray("AccTransactionLines"));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                }
            }, getRootActivity());
        }
        @Override
        protected void selectRow(TableRow row, TableRow header, TableLayout tableLayout) {
            super.selectRow(row, header, tableLayout);
            getActionButton(Control.ACTION_CHECKED).setEnabled(true);
        }
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
                controls.add(Control.getDateTimeControl("AccTransaction.TranDate","Date").setColumnWeight(6));
                controls.add(Control.getEditTextControl("Naration","Narration").setColumnWeight(8));
                controls.add(Control.getEditDecimalControl("Amount","Amount").setFormula("{0}.Debit - {0}.Credit").setColumnWeight(4));
                return controls;
            }
            else{
                return null;
            }
        }
        @Override
        protected void rowAdded(ArrayList<Control.ControlBase> controls,JSONObject data) {
            super.rowAdded(controls,data);
            Optional<Control.ControlBase> control = controls.stream().filter(i-> i.getName().equals("Amount")).findFirst();
            if(control.isPresent() && control.get().getListTextView() != null){
                TextView tv = control.get().getListTextView();
                int colour = Color.parseColor("#80FFEB3B");
                try{
                    if(data.getDouble("Amount") < 0){
                        colour = Color.parseColor("#80EE1506");
                    }
                    else {
                        colour = Color.parseColor("#8048BE09");
                    }
                }
                catch (JSONException e){
                    System.out.println(e.getMessage());
                }
                tv.setBackgroundColor(colour);
            }
        }
    }
}
