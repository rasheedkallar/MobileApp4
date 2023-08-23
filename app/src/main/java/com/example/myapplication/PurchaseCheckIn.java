package com.example.myapplication;

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
        ArrayList<Utility.Control> controls = new ArrayList<Utility.Control>();
        //Utility.Control id = new Utility.Control(Utility.ControlType.HiddenValue,"header_id","Id", null,null,false);
        //controls.add(id);
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
                            SelectedItem = data;

                            return false;
                        }
                    });

                } catch (JSONException | ParseException e) {
                }
            }
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {

            }
        });


    }

    private JSONObject SelectedItem= null;

    @Override
    public void onButtonClick(String action, RadioButton button) {

        final Context contxt = this;

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

                        new PopupForm(contxt, "Purchase Check In", controls, new PopupForm.onFormPopupFormListener() {
                            @Override
                            public boolean onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, String result) {
                                id.DefaultValue = Long.valueOf(result);
                                RefreshList();
                                return false;
                            }

                            @Override
                            public String getUrl() {
                                return "InvCheckIn";
                            }
                        });
                        return true;
                    }
                });
                return true;
            }
        });
    }



    @Override
    public String getHeaderText() {
        return "Stock Receive";
    }


}

/*

                        G.lookupPicker(contxt, suppliers, new G.onLookupPickistener() {
                            @Override
                            public void onPick(DataService.Lookup lookup) {
                                final DataService.Lookup suppler = lookup;
                                new DataService().getLookup(contxt, "Employee", new DataService.LookupResponse() {
                                    @Override
                                    public void onSuccess(List<DataService.Lookup> lookup) {
                                        final List<DataService.Lookup> employees = lookup;
                                        G.lookupPicker(contxt, employees, new G.onLookupPickistener() {
                                            @Override
                                            public void onPick(DataService.Lookup lookup) {
                                                final DataService.Lookup employee = lookup;
                                                ArrayList<FormEditor.Control> controls = new ArrayList<FormEditor.Control>();
                                                controls.add(new FormEditor.Control(FormEditor.ControlType.Text,"RefNum","Ref Number"));
                                                controls.add(new FormEditor.Control(FormEditor.ControlType.Lookup,"Supplier","Supplier",suppler,suppliers));
                                                controls.add(new FormEditor.Control(FormEditor.ControlType.Lookup,"Employee","Employee",employee,employees));

                                                new FormEditor(contxt, controls, "PurchaseCheckIn", new FormEditor.onFormEditorListener() {


                                                });
                                            }
                                        });
                                    }
                                });

                            }
                        });
                        */