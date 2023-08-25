package com.example.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
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

import com.example.myapplication.model.Popup;

import org.json.JSONException;

import java.security.Permission;

public abstract class BaseActivity extends AppCompatActivity {

    public static final int GALLERY_PERMISSION_REQUEST_CODE = 1;
    public static final int CAMERA_PERMISSION_REQUEST_CODE  = 2;


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
                onButtonClick("Edit",(RadioButton) view);
            }
        });

        DeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick("Delete",(RadioButton) view);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(permissionGrantedListener != null){
            permissionGrantedListener.onPermissionGranted( requestCode, permissions, grantResults);
        }

        /*

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    //takePictureLauncher.launch(takePictureIntent);
                }
            }
        }
        else if (requestCode == GALLERY_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //pickImageLauncher.launch(pickImageIntent);
            }
        }

         */
    }
    private onPermissionGrantedListener permissionGrantedListener;
    public void setPermissionGrantedListener(onPermissionGrantedListener listener){
        permissionGrantedListener = listener;
    }


    public  static abstract class onPermissionGrantedListener{
        public abstract void onPermissionGranted(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
    }


    public abstract void onButtonClick(String action, RadioButton button) ;


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
