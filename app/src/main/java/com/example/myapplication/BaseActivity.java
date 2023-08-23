package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
public abstract class BaseActivity extends AppCompatActivity {
    public LinearLayout Container;
    public TextView Header;
    public RadioGroup ButtonGroup;

    public RadioButton AddButton;
    public RadioButton EditButton;
    public RadioButton DeleteButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        Container = (LinearLayout) findViewById(R.id.container);
        Header = (TextView) findViewById(R.id.header);
        ButtonGroup = (RadioGroup) findViewById(R.id.icon_button_group);

        AddButton = (RadioButton) findViewById(R.id.radio_add);
        EditButton = (RadioButton) findViewById(R.id.radio_edit);
        DeleteButton = (RadioButton) findViewById(R.id.radio_delete);

        AddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick("Add",(RadioButton) view);
            }
        });
        EditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick("Add",(RadioButton) view);
            }
        });

        DeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick("Add",(RadioButton) view);
            }
        });


        /*
        NewButton = (Button) findViewById(R.id.new_entry);
        NewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNewClick(view);
            }
        });

        */


        //if(viewNewButton())NewButton.setVisibility(Button.VISIBLE);
        //else NewButton.setVisibility(Button.INVISIBLE);
        Header.setText(getHeaderText());
    }
    public abstract void onButtonClick(String action, RadioButton button);


    public boolean viewNewButton(){
        return true;
    }
    public abstract String getHeaderText();
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch(item.getTitle().toString()){
            case "Home":
                intent = new Intent(this,MainActivity.class);
                break;
            case "Purchase Check In":
                intent = new Intent(this,PurchaseCheckIn.class);
                break;
            default:
                super.onOptionsItemSelected(item);
                return  false;
        }
        startActivity(intent);
        return  true;
    }
}
