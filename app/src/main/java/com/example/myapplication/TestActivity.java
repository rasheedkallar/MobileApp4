package com.example.myapplication;

import com.example.myapplication.model.Control;

import java.util.ArrayList;

public class TestActivity extends BaseActivity {
    public TestActivity(){
        Controls.add(new InvCheckInActivity.InvCheckInDetailedControl());
    }
    public static class InvCheckInDetailedControl extends Control.DetailedControl {
        public InvCheckInDetailedControl() {
            super("InvCheckIns", "Stock Receive");
        }




        @Override
        protected ArrayList<Control.ControlBase> getControls(String action) {
            return null;
        }
    }

}
