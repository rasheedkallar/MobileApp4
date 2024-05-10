package com.example.myapplication;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

public  class TransactionMonitor extends BaseActivity {
    public TransactionMonitor() {
        Controls.add(new TransactionMonitorControl());
    }
    public static class TransactionMonitorControl extends Control.DetailedControl {
        public TransactionMonitorControl() {
            super("AccTransactionLines", "Transaction Monitor");
            setEnableScroll(true);
            getButtons().clear();
            addButton(Control.ACTION_CHECKED, view -> {
                    PopupConfirmation.create("Transaction Verification", "Are you sure you want to verify transaction?", unused -> {
                        JSONObject obj = new JSONObject();
                        try {
                            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                            obj.put("ReconDate",format.format(new Date()));
                            new DataService().postForSave("AccTransactionLines[" + getValue() + "]", obj, aLong -> {
                                getButton(Control.ACTION_CHECKED).setEnabled(false);
                                refreshGrid();
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

            //refreshGrid();
        }

        @Override
        protected String getWhere(String action) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.HOUR_OF_DAY, -24 * 30);
            Date oneMonth = calendar.getTime();

            calendar.setTime(new Date());
            calendar.add(Calendar.HOUR_OF_DAY, -24);
            Date oneDay = calendar.getTime();


            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");



            return "ReconDate == null && Changed > \"" + format.format(oneMonth) + "\" && Created < \"" + format.format(oneMonth) + "\" && Changed > SqlFunctions.DateAdd(\"day\", 1, Created) && Changer != \"Scheduler\" && !new String[]{\"Tax\",\"Sales\",\"Purchase\"}.Contains(TranLineType)";

            //select * from  AccTransactionLine where ReconDate is null and Changed > DATEADD(HOUR,-24 * 30, getdate()) and Created < DATEADD(HOUR,-24, getdate()) and Changed > DATEADD(DAY,1,Created) and Changer <> 'Scheduler' and TranLineType not in ('Tax','Sales','Purchase')
        }

        @Override
        protected String getOrderBy(String action) {
            return "AccTransaction.TranDate,AccTransaction.TranNumber";
        }

        @Override
        protected void rowAdded(ArrayList<Control.ControlBase> controls,JSONObject data) {
            super.rowAdded(controls,data);
            try {
                if(Boolean.parseBoolean(data.get("Deleted").toString())){
                    int colour = Color.parseColor("#80EE1506");
                    Optional<Control.ControlBase> control = controls.stream().filter(i-> i.getName().equals("AccTransaction.TranNumber")).findFirst();
                    TextView tv = control.get().getListTextView();
                    tv.setBackgroundColor(colour);
                }
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
                controls.add(Control.getDateTimeControl("AccTransaction.TranDate","Date").setColumnWeight(6));
                controls.add(Control.getEditTextControl("AccTransaction.TranNumber","Number").setColumnWeight(5));
                controls.add(Control.getEditTextControl("AccLedger.Name","Ledger").setColumnWeight(8));
                //controls.add(Control.getDateTimeControl("Changed","Changed").setColumnWeight(6));
                //controls.add(Control.getEditTextControl("Changer","Usr").setFormula("{0}.Changer.SubString(0,2)").setColumnWeight(2));
                controls.add(Control.getEditTextControl("Narration","Narration").setColumnWeight(9));
                controls.add(Control.getHiddenControl("Deleted","Narration"));
                controls.add(Control.getEditDecimalControl("Amount","Amount").setDecimalFormat("0.00Dr;0.00Cr").setFormula("{0}.Debit - {0}.Credit").setColumnWeight(6));
                return controls;
            }
            return controls;
        }
    }
}
