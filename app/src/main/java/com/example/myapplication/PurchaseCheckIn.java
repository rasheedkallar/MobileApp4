package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.model.DataService;
import com.example.myapplication.model.FormEditor;
import com.example.myapplication.model.G;
import com.example.myapplication.model.PopupForm;
import com.example.myapplication.model.PopupLookup;

import java.util.ArrayList;
import java.util.List;

public class PurchaseCheckIn extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_check_in);
        Button button = (Button) findViewById(R.id.btn_new);
        final Context contxt = this;
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new DataService().getLookup(contxt, "Supplier", new DataService.LookupResponse() {
                    @Override
                    public void onSuccess(List<DataService.Lookup> lookup) {
                        final List<DataService.Lookup> suppliers = lookup;
                        new PopupLookup(contxt, "Supplier", lookup, new PopupLookup.onFormPopupLookupListener() {
                            @Override
                            public void onPick(DialogInterface dialog, DataService.Lookup lookup) {
                                final DataService.Lookup suppler = lookup;
                                new DataService().getLookup(contxt, "Employee", new DataService.LookupResponse() {
                                    @Override
                                    public void onSuccess(List<DataService.Lookup> lookup) {

                                        final List<DataService.Lookup> employees = lookup;
                                        new PopupLookup(contxt, "Employee", lookup, new PopupLookup.onFormPopupLookupListener() {
                                            @Override
                                            public void onPick(DialogInterface dialog,DataService.Lookup lookup) {
                                                final DataService.Lookup employee = lookup;

                                                ArrayList<PopupForm.Control> controls = new ArrayList<PopupForm.Control>();
                                                controls.add(new PopupForm.Control(PopupForm.ControlType.Text,"RefNum","Ref Number"));
                                                controls.add(new PopupForm.Control(PopupForm.ControlType.Lookup,"Supplier","Supplier",suppler,suppliers));
                                                controls.add(new PopupForm.Control(PopupForm.ControlType.Lookup,"Employee","Employee",employee,employees));


                                                new PopupForm(contxt, "Purchase Check In", controls, "CheckIn", new PopupForm.onFormPopupFormListener() {
                                                    @Override
                                                    public PopupForm getPopup() {
                                                        return super.getPopup();
                                                    }
                                                });







                                            }
                                        });
                                    }
                                });

                            }
                        });

                    }
                });







            }
        });


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