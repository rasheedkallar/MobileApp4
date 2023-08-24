package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupForm;
import com.example.myapplication.model.PopupHtml;
import com.example.myapplication.model.PopupLookup;
import com.example.myapplication.model.Utility;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.AlignSelf;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.internal.FlowLayout;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class PurchaseCheckIn extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FlexboxLayout fbl = new FlexboxLayout(this  );
        FlexboxLayout.LayoutParams fblP= new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        fbl.setLayoutParams(fblP);
        fbl.setFlexWrap(FlexWrap.WRAP);
        Container.addView(fbl);

        fromControl = new Utility.Control(Utility.ControlType.Date,"from","From Date", Utility.AddDay(new Date(),-10),null,false);
        toControl  = new Utility.Control(Utility.ControlType.Date,"to","To Date", Utility.AddDay(new Date(),1),null,false);


        fbl.addView(fromControl.GenerateView(this,310));
        fbl.addView(toControl.GenerateView(this,310));


        Button btn = new Button(this);
        FlexboxLayout.LayoutParams btlP= new FlexboxLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 100);
        btlP.setMargins(0,75,0,0);

        btn.setLayoutParams(btlP);
        btn.setText("Refresh");

        btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RefreshList();
            }
        });
        fbl.addView(btn);

        ScrollView sv = new ScrollView(this);
        ScrollView.LayoutParams scP= new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        scP.setLayoutDirection(LinearLayout.HORIZONTAL);
        sv.setLayoutParams(scP);

        Container.addView(sv);


        table = new TableLayout(this);
        TableLayout.LayoutParams tableP= new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        table.setLayoutParams(tableP);

        sv.addView(table);

        final Context context = this;

        System.out.println("hi");

        AddButton.setEnabled(false);
        EditButton.setEnabled(false);
        DeleteButton.setEnabled(false);

        new DataService().getLookups(this,  new String[] {"Supplier", "Employee"}, new DataService.LookupsResponse() {
            @Override
            public void onSuccess(List<DataService.Lookup>[] lookups) {
                suppliers = lookups[0];
                employees = lookups[1];
                AddButton.setEnabled(true);
                RefreshList();
            }
        });
    }
    List<DataService.Lookup> suppliers;
    List<DataService.Lookup> employees;

    Utility.Control fromControl;
    Utility.Control toControl;

    TableLayout table;

    private void  RefreshList(){
        EditButton.setEnabled(false);
        DeleteButton.setEnabled(false);
        ArrayList<Utility.Control> controls = new ArrayList<Utility.Control>();
        controls.add(new Utility.Control(Utility.ControlType.DateTime,"header_date","Date", new Date(),null,false));
        controls.add(new Utility.Control(Utility.ControlType.Text,"header_number","Ref#",null,null,false));
        controls.add(new Utility.Control(Utility.ControlType.Text,"header_employee_name","Emp",null,null,false));
        controls.add(new Utility.Control(Utility.ControlType.Lookup,"header_supplier","Supplier",null,suppliers,true));
        controls.add(new Utility.Control(Utility.ControlType.Text, "header_status", "Status", null, null, false));

        new DataService().get("InvCheckIn?" + fromControl.GetUrlParam() + "&" + toControl.GetUrlParam(), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    JSONArray data = new JSONArray(result);
                    Utility.CreateGrid(getBaseContext(), table, controls, data, new Utility.onGridListener() {
                                @Override
                                public boolean onRowSelected(TableRow row, JSONObject data) {
                                    try {
                                        SelectedId = data.getLong("header_id");
                                          EditButton.setEnabled(true);
                                        DeleteButton.setEnabled(true);
                                        return true;
                                    }
                                    catch (JSONException ex){
                                        SelectedId = 0L;
                                        EditButton.setEnabled(false);
                                        DeleteButton.setEnabled(false);
                                        return false;
                                    }
                                }

                        @Override
                        public boolean isSelected(TableRow row, JSONObject data)   {
                            System.out.println(data.toString());

                            if(SelectedId == null || SelectedId == 0)return false;

                            try {
                                Long id = data.getLong("header_id");
                                if(id.equals(SelectedId)){
                                    EditButton.setEnabled(true);
                                    DeleteButton.setEnabled(true);
                                    return true;
                                }
                            }
                            catch (JSONException ex){
                            }
                            return false;
                        }
                    });
                }
                catch (JSONException | ParseException e)
                {

                }
            }
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {

            }

        });


    }

    private Long SelectedId= 0L;

    @Override
    public void onButtonClick(String action, RadioButton button) {

        final Context contxt = this;

        if(action == "Add"){
            new PopupLookup(contxt, "Supplier", suppliers, new PopupLookup.onFormPopupLookupListener() {
                @Override
                public boolean onPick(DataService.Lookup lookup) {
                    final DataService.Lookup suppler = lookup;
                    new PopupLookup(contxt, "Employee", employees, new PopupLookup.onFormPopupLookupListener() {
                        @Override
                        public boolean onPick(DataService.Lookup lookup) {
                            final DataService.Lookup employee = lookup;
                            ArrayList<Utility.Control> controls = new ArrayList<Utility.Control>();
                            Utility.Control id = new Utility.Control(Utility.ControlType.HiddenValue, "header_id", "Id", null, null, false);
                            controls.add(id);
                            controls.add(new Utility.Control(Utility.ControlType.DateTime, "header_date", "Check In Date", new Date(), null, false));
                            controls.add(new Utility.Control(Utility.ControlType.Text, "header_number", "Ref Number", null, null, false));
                            controls.add(new Utility.Control(Utility.ControlType.Lookup, "header_supplier", "Supplier", suppler, suppliers, true));
                            controls.add(new Utility.Control(Utility.ControlType.Lookup, "header_employee", "Employee", employee, employees, true));


                            PopupForm from = new PopupForm(contxt, "Purchase Check In New", controls, new PopupForm.onFormPopupFormListener() {
                                @Override
                                public boolean onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, String result) {
                                    Long selid = Long.valueOf(result);
                                    RefreshList();
                                    SelectedId = selid;
                                    if(id.DefaultValue == null || id.DefaultValue.equals(0)){
                                        id.DefaultValue = selid;
                                        this.getPopup().AlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                        return false;
                                    }
                                    else{
                                        return true;
                                    }
                                }

                                @Override
                                public String getUrl() {
                                    return "InvCheckIn";
                                }
                            });
                            return false;
                        }
                    });
                    return true;
                }
            });
        }
        else if(action == "Edit"){
            new DataService().getById(contxt, "InvCheckIn", SelectedId, new DataService.GetByIdResponse() {
                @Override
                public void onSuccess(JSONObject data) {
                    ArrayList<Utility.Control> controls = new ArrayList<Utility.Control>();
                    Utility.Control id = new Utility.Control(Utility.ControlType.HiddenValue, "header_id", "Id", null, null, false);
                    controls.add(id);
                    controls.add(new Utility.Control(Utility.ControlType.DateTime, "header_date", "Check In Date", new Date(), null, false));
                    controls.add(new Utility.Control(Utility.ControlType.Text, "header_number", "Ref Number", null, null, false));
                    controls.add(new Utility.Control(Utility.ControlType.Lookup, "header_supplier", "Supplier", null, suppliers, true));
                    controls.add(new Utility.Control(Utility.ControlType.Lookup, "header_employee", "Employee", null, employees, true));
                    try {
                        Utility.applyValues(data,controls);
                    }
                    catch ( JSONException ex){
                    }
                    new PopupForm(contxt, "Purchase Check In Edit", controls, new PopupForm.onFormPopupFormListener() {
                        @Override
                        public boolean onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, String result) {
                            RefreshList();
                            return true;
                        }
                        @Override
                        public String getUrl() {
                            return "InvCheckIn";
                        }
                    });

                }
            });
        }
        else if(action == "Delete"){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete Confirmation")
                    .setMessage("Are you sure you wnat to delete?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            new DataService().deleteById(contxt, "InvCheckIn", SelectedId, new DataService.DeleteByIdResponse() {
                                @Override
                                public void onSuccess(Boolean deleted) {
                                    RefreshList();
                                }
                            });
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();


        }
    }
    @Override
    public String getHeaderText() {
        return "Stock Receive";
    }
}
