package com.example.myapplication.model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.security.AppUriAuthenticationPolicy;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.unusedapprestrictions.IUnusedAppRestrictionsBackportService;

import com.example.myapplication.R;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class FormEditor {
    public FormEditor(Context context, List<Control> controls, onFormEditorListener listner){
        LayoutInflater inflater = LayoutInflater.from(context);
        View popupView = inflater.inflate(R.layout.editor_form, null);
        FlexboxLayout flexboxLayout = popupView.findViewById(R.id.flexboxLayout);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        for (Control c : controls) {
            View view;
            if(c.Type == ControlType.Date){
                EditText txt = new EditText(context);
                view = txt;
            }
            else{
                EditText txt = new EditText(context);
                view = txt;
            }
            flexboxLayout.addView(view);
        }
        alertDialogBuilder.setView(popupView);
        alertDialogBuilder
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Close the dialog
                    }
                });
        final AlertDialog alertDialog = alertDialogBuilder.create();
        for (int i = 0; i < flexboxLayout.getChildCount(); i++) {
            Button button = (Button)flexboxLayout.getChildAt(i);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(view.getTag() == null){
                        listner.onPick(null);
                    }
                    else{
                        DataService.Lookup l = (DataService.Lookup)view.getTag();
                        listner.onPick(l);
                    }
                    alertDialog.dismiss();
                }
            });
        }
        alertDialog.setTitle("My Title");
        alertDialog.setCancelable(false);
        alertDialog.show();
    }
    public abstract static class onFormEditorListener {
        public abstract void onPick(DataService.Lookup lookup);
    }
    public enum ControlType{
        Text,
        Date,
        Time,
        DateTime,
        Lookup,
        Int,
        Decimal,
        Password
    }
    public class  Control {
        public Control() {
            Type = ControlType.Text;
            AllowNull = false;
            DecimalPlace = 2;
            DefaultValue = null;
            Lookup = new ArrayList<DataService.Lookup>();
        }
        public ControlType Type;
        public String Name;
        public String Caption;
        public Boolean AllowNull;
        public int DecimalPlace;
        public List<DataService.Lookup> Lookup;
        public Object DefaultValue;
    }

}
