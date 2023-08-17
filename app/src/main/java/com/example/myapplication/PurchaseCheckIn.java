package com.example.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupForm;
import com.example.myapplication.model.PopupLookup;
import com.example.myapplication.model.Utility;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.AlignSelf;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.internal.FlowLayout;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PurchaseCheckIn extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FlexboxLayout fbl = new FlexboxLayout(this  );
        FlexboxLayout.LayoutParams fblP= new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        fbl.setLayoutParams(fblP);
        fbl.setFlexWrap(FlexWrap.WRAP);
        Container.addView(fbl);
        fbl.addView(Utility.GenerateView(this,new Utility.Control(Utility.ControlType.Date,"FromDate","From", Utility.AddDay(new Date(),1),null,false),310));
        fbl.addView(Utility.GenerateView(this,new Utility.Control(Utility.ControlType.Date,"ToDate","To", Utility.AddDay(new Date(),10),null,false),310));

        Button btn = new Button(this);
        FlexboxLayout.LayoutParams btlP= new FlexboxLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 100);
        btlP.setMargins(0,75,0,0);

        btn.setLayoutParams(btlP);
        btn.setText("Refresh");

        btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        fbl.addView(btn);

        /*

        RelativeLayout layout = findViewById(R.id.relativeLayout);
        View viewAbove = findViewById(R.id.viewAbove);
        View viewBelow = findViewById(R.id.viewBelow);

        // Get the existing layout parameters of viewBelow
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewBelow.getLayoutParams();

        // Set the alignment to alignBottom with viewAbove
        layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, viewAbove.getId());

        */




    }
    @Override
    public void onNewClick(View view) {

        final Context contxt = this;
        new DataService().getLookup(contxt, "Supplier", new DataService.LookupResponse() {
            @Override
            public void onSuccess(List<DataService.Lookup> lookup) {
                final List<DataService.Lookup> suppliers = lookup;
                new PopupLookup(contxt, "Supplier", lookup, new PopupLookup.onFormPopupLookupListener() {
                    @Override
                    public boolean onPick(DataService.Lookup lookup) {
                        final DataService.Lookup suppler = lookup;
                        new DataService().getLookup(contxt, "Employee", new DataService.LookupResponse() {
                            @Override
                            public void onSuccess(List<DataService.Lookup> lookup) {

                                final List<DataService.Lookup> employees = lookup;
                                new PopupLookup(contxt, "Employee", lookup, new PopupLookup.onFormPopupLookupListener() {
                                    @Override
                                    public boolean onPick(DataService.Lookup lookup) {
                                        final DataService.Lookup employee = lookup;

                                        ArrayList<Utility.Control> controls = new ArrayList<Utility.Control>();
                                        controls.add(new Utility.Control(Utility.ControlType.DateTime,"CheckInDate","Check In Date", new Date(),null,false));
                                        controls.add(new Utility.Control(Utility.ControlType.Text,"RefNum","Ref Number",null,null,false));
                                        controls.add(new Utility.Control(Utility.ControlType.Lookup,"Supplier","Supplier",suppler,suppliers,true));
                                        controls.add(new Utility.Control(Utility.ControlType.Lookup,"Employee","Employee",employee,employees,true));
                                        new PopupForm(contxt, "Purchase Check In", controls, "CheckIn", new PopupForm.onFormPopupFormListener() {
                                            @Override
                                            public PopupForm getPopup() {
                                                return super.getPopup();
                                            }
                                        });
                                        return true;
                                    }
                                });
                            }
                        });
                        return true;
                    }
                });
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