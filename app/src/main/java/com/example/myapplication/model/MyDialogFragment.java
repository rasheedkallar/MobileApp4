package com.example.myapplication.model;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class MyDialogFragment extends DialogFragment {


    public String Title = "hi";


    public String getCancelButton() {
        return "Cancel";
    }

    public String getOkButton() {
        return null;
    }


    public AlertDialog Popup;


    public MyDialogFragment() {
        //_listener = listener;
        //Title = title;
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LinearLayout linearLayout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams lllP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout.setPadding(2, 2, 2, 2);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(lllP);


        TextView caption = new TextView(getActivity());
        TableLayout.LayoutParams cParam = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500);
        caption.setPadding(10, 10, 10, 10);
        caption.setLayoutParams(cParam);
        caption.setText(Title);
        caption.setTextSize(30);
        caption.setTextColor(Color.parseColor("#8CD0E4"));
        caption.setBackgroundColor(Color.parseColor("#225C6E"));

        AlertDialog.Builder AlertDialogBuilder = new AlertDialog.Builder(getActivity());
        AlertDialogBuilder.setCustomTitle(caption);
        AlertDialogBuilder.setView(linearLayout);
        /*
        if (getOkButton() != null) {
            AlertDialogBuilder.setPositiveButton(getOkButton(), null);
        }
        if (getCancelButton() != null) {
            AlertDialogBuilder.setNegativeButton(getCancelButton(), null);
        }

        AddControls(linearLayout);

        */
        AlertDialog Popup2 = AlertDialogBuilder.create();
        Popup2.setTitle(Title);
        Popup2.setCancelable(false);
        /*
        if (getCancelButton() != null) {

            Popup2.setOnShowListener(dialog -> {
                Popup2.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
                    Popup2.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);

                    DoCancel();

                });
            });
        }

        if (getOkButton() != null) {

            Popup2.setOnShowListener(dialog -> {
                Popup2.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    Popup2.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    DoOk();

                });
            });
        }
        */

        return Popup2;
    }




}