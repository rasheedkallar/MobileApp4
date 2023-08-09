package com.example.myapplication.model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import com.example.myapplication.R;
import com.google.android.flexbox.FlexboxLayout;

import java.util.Date;
import java.util.List;


public class G {

    public static void showAlertDialog(Context context,String title,String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
        .setMessage(message)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }



    public static void lookupPicker(Context context, List<DataService.Lookup> lookup, onLookupPickistener listner) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View popupView = inflater.inflate(R.layout.popup_form, null);
        FlexboxLayout flexboxLayout = popupView.findViewById(R.id.flexboxLayout);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        for (DataService.Lookup l : lookup) {
            Button button = new Button(context);
            button.setWidth(100);
            button.setHeight(200);
            button.setText(l.Name);
            button.setTag(l);
            button.setPadding(0, 0, 0, 0);
            flexboxLayout.addView(button);
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

    public static void datetimePicker(Context context, onDateTimePickistener listner) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View popupView = inflater.inflate(R.layout.popup_form, null);
        FlexboxLayout flexboxLayout = popupView.findViewById(R.id.flexboxLayout);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        DatePicker datePicker = new DatePicker(context);
        flexboxLayout.addView(datePicker);
        alertDialogBuilder.setView(popupView);
        alertDialogBuilder
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Close the dialog
                    }
                });
        final AlertDialog alertDialog = alertDialogBuilder.create();

        datePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                listner.onPick(new Date(i,i1,i2));
            }
        });
        alertDialog.setTitle("My Title");
        alertDialog.setCancelable(false);
        alertDialog.show();
    }


    public abstract static class onLookupPickistener {
        public abstract void onPick(DataService.Lookup lookup);
    }
    public abstract static class onDateTimePickistener {
        public abstract void onPick(Date date);
    }

}
