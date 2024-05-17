package com.example.myapplication;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import com.example.myapplication.Activity.Item;
import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupBase;
import com.example.myapplication.model.PopupConfirmation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

public  class TransactionMonitor extends BaseActivity {
    public TransactionMonitor() {
        Controls.add(new TransactionMonitorControl());
    }

    public static class  PopupCheckListAccArgs extends PopupBase.PopupArgs<PopupCheckListAccArgs> {
        public PopupCheckListAccArgs(long  tranId){
            super("Transactions");
            setOkButton("Approve");
            setEnableScroll(true);
            TranId = tranId;
        }
        private long TranId;
        public long getTranId() {
            return TranId;
        }
        public PopupCheckListAccArgs setTranId(long tranId) {
            TranId = tranId;
            return this;
        }
    }

    public static class PopupCheckListAcc extends PopupBase<PopupCheckListAcc,PopupCheckListAccArgs> {

        public PopupCheckListAcc(long tranId) {
            setArgs(new PopupCheckListAccArgs(tranId));
        }




        @Override
        public void AddControls(LinearLayout container) {
            HashMap<String,JSONArray> map = new HashMap<>();
            JSONObject param = new JSONObject();
            try {
                param.put("accTranId",getArgs().getTranId());
                new DataService().postForExecuteList("sp_CheckListAcc", param, jsonArray -> {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String group = obj.getString("LineGroupName");
                            if(!map.containsKey(group))map.put(group,new JSONArray());
                            map.get(group).put(obj);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    for (String key:map.keySet()) {
                        CheckListAccControl det = new CheckListAccControl(key,map.get(key));
                        det.addView(container);
                    }
                    return null;
                }, getRootActivity());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        public static class CheckListAccControl extends Control.DetailedControl {
            private JSONArray Data;
            public CheckListAccControl(String header,JSONArray data) {
                super("sp_CheckListAcc", header);
                Data = data;
                getButtons().clear();
            }

            @Override
            public Object getAggregateValue(Control.ControlBase control, JSONObject obj) {
                try {
                    if(obj.getInt("SubOrder") == 1 && obj.getBoolean("Deleted") == false){
                        return super.getAggregateValue(control, obj);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }

            @Override
            public void  refreshGrid(TableLayout table){
                refreshDetailedView(Data);
            }
            @Override
            protected void rowAdded(ArrayList<Control.ControlBase> controls,JSONObject data) {
                super.rowAdded(controls, data);
                for (int i = 0; i < controls.size(); i++) {
                    TextView tv = controls.get(i).getListTextView();
                    try {
                        if (data.getInt("SubOrder") == 1 && data.getBoolean("Deleted") == true) {
                            tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        }
                        else if (data.getInt("SubOrder") > 1) {
                            int colour = Color.WHITE;
                            colour = ColorUtils.blendARGB(colour, Color.TRANSPARENT,0.2F);
                            tv.setBackgroundColor(colour);
                            tv.setTextColor(Color.GRAY);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            @Override
            protected ArrayList<Control.ControlBase> getControls(String action) {
                ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();

                //OrderNo	SubOrder	LogCreated	LogCreator	LogId	AccLedgerId	Debit	Credit	AccTranLineId
                // Amount	AlertId	AlertType	AlertDesc	AlertStatus	Code	Name	RefNumber
                // Narration	Deleted	TranLineId	TranLineType	ReconDate	TranStaus
                // TranDate	TranNumber	TranType	LineGroupName


                if(action.equals(Control.ACTION_REFRESH)){
                    controls.add(Control.getDateTimeControl("LogCreated","Changed").setColumnWeight(6));
                    controls.add(Control.getEditTextControl("LogCreator","").setColumnWeight(2));
                    controls.add(Control.getEditTextControl("Name","Ledger").setColumnWeight(5));
                    controls.add(Control.getEditTextControl("AlertDesc","Desc").setColumnWeight(5));
                    controls.add(Control.getEditTextControl("Narration","Narration").setColumnWeight(5));
                    controls.add(Control.getEditDecimalControl("Amount","Amount").setDecimalFormat("0.00Dr;0.00Cr").setAggregate(Control.AGGREGATE_SUM).setColumnWeight(6));
                    return controls;
                }
                return controls;
            }
        }
    }
    public static class TransactionMonitorControl extends Control.DetailedControl {
        public TransactionMonitorControl() {
            super("sp_TransMonitor", "Transaction Monitor");
            setEnableScroll(true);
            getButtons().clear();
            addButton(Control.ACTION_CHECKED, view -> {
                PopupCheckListAcc popup =  new PopupCheckListAcc(getValue());
                popup.setOnDoOk(unused -> {
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("id",getValue());
                        RefreshData(obj);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    return true;
                });
                popup.show(getRootActivity().getSupportFragmentManager(),null);
                return null;
            });
            getButtons().get(0).setEnabled(false);
            addButton(Control.ACTION_REFRESH);
        }
        private  void  RefreshData(JSONObject param){
            new DataService().postForExecuteList("sp_TransMonitor", param, jsonArray -> {
                refreshDetailedView(jsonArray);
                return null;
            }, getRootActivity());
        }
        @Override
        public void  refreshGrid(TableLayout table){
            RefreshData(new JSONObject());
        }
        @Override
        protected void rowAdded(ArrayList<Control.ControlBase> controls,JSONObject data) {
            super.rowAdded(controls,data);
            try {
                if(Boolean.parseBoolean(data.get("Deleted").toString())){
                    int colour = Color.parseColor("#80EE1506");
                    Optional<Control.ControlBase> control = controls.stream().filter(i-> i.getName().equals("Amount")).findFirst();
                    TextView tv = control.get().getListTextView();
                    tv.setBackgroundColor(colour);
                }
                Control.DateTimeControl changed = (Control.DateTimeControl)controls.stream().filter(i-> i.getName().equals("Changed")).findFirst().get();
                Control.DateTimeControl tanCreated = (Control.DateTimeControl)controls.stream().filter(i-> i.getName().equals("TanCreated")).findFirst().get();
                int c = Color.RED;
                long difference_In_Time = changed.getValue().getTime() - tanCreated.getValue().getTime();
                float difference_In_Hours = (float)difference_In_Time / (1000 * 60 * 60);
                System.out.println(difference_In_Hours);
                if(difference_In_Hours < 24.0F * 60.0F){
                    System.out.println(1.0F- (difference_In_Hours/(24.0F * 60.0F)));
                    c= ColorUtils.blendARGB(Color.WHITE, c, 1.0F- (difference_In_Hours/(24.0F * 60.0F)));
                }
                c = ColorUtils.blendARGB(c, Color.TRANSPARENT,0.2F);
                TextView tvChanged = changed.getListTextView();
                tvChanged.setBackgroundColor(c);
            } catch (JSONException e) {
            }
        }

        @Override
        protected void selectRow(TableRow row, TableRow header, TableLayout tableLayout) {
            super.selectRow(row, header, tableLayout);
            getActionButton(Control.ACTION_CHECKED).setEnabled(true);
        }

        @Override
        protected ArrayList<Control.ControlBase> getControls(String action) {
            ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
            if(action.equals(Control.ACTION_REFRESH)){
                controls.add(Control.getEditTextControl("TranNumber","Number").setColumnWeight(5));
                controls.add(Control.getDateTimeControl("Changed","Changed").setColumnWeight(6));
                controls.add(Control.getDateTimeControl("TanCreated","Created").setColumnWeight(6));
                controls.add(Control.getEditTextControl("Changer","").setColumnWeight(2));
                controls.add(Control.getDateTimeControl("TranDate","TranDate").setColumnWeight(6));
                controls.add(Control.getEditTextControl("Ledger","Ledger").setColumnWeight(8));
                controls.add(Control.getEditDecimalControl("Amount","Amount").setDecimalFormat("0.00Dr;0.00Cr").setColumnWeight(6));
                return controls;
            }
            return controls;
        }
    }
}
