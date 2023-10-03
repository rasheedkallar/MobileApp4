package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TableLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupHtml;
import com.example.myapplication.model.PopupLookup;
import com.example.myapplication.model.Utility;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import cz.msebera.android.httpclient.Header;

public class TestActivity extends BaseActivity {
    public TestActivity(){
        Controls.add(new InvCheckInActivity.InvCheckInDetailedControl());
    }
    public static class InvCheckInDetailedControl extends Control.DetailedControl {
        public InvCheckInDetailedControl() {
            super("InvCheckIns", "Stock Receive","InvCheckIn",null);
        }

        @Override
        protected ArrayList<Control.ControlBase> getControls(String action) {
            ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
            if(action == null)return controls;
            if(action.equals("Filter")){

                controls.add(Control.getDateControl("from","From").setValue(Utility.AddDay(new Date(),-10)).setControlSize(310));
                controls.add(Control.getDateControl("to","To").setValue(Utility.AddDay(new Date(),1)).setControlSize(310));
                return controls;
            }
            else if(action.equals("Refresh")){
                ArrayList<Control.ControlBase> list = new ArrayList<Control.ControlBase>();
                controls.add(Control.getDateTimeControl("CheckInTime","Date"));
                //controls.add(Control.getEditTextControl("RefNum","Ref#"));
                //controls.add(Control.getEditTextControl("EmpName","Emp"));
                //controls.add(Control.getLookupControl("SupId","Supplier",null));
                //controls.add(Control.getEditTextControl( "Status", "Status"));

            }
            return controls;
        }
    }

}
