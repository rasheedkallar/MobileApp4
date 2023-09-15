package com.example.myapplication;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.myapplication.model.PopupConfirmation;
import com.example.myapplication.model.PopupDate;
import com.example.myapplication.model.PopupHtml;

import java.util.Date;


public class MainActivity extends BaseActivity {
   private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        PopupDate test1 = registerPopup(new PopupDate(),new PopupDate.PopupDateArgs("test","Delete Confirmation", new Date()), new PopupDate.PopupDateListener() {
            @Override
            public boolean onDateChanged(Date value) {
                Toast.makeText(getBaseContext(),value.toString(),Toast.LENGTH_SHORT).show();

                return true;
            }
        });

        /*


        PopupConfirmation.PopupConfirmationArgs deleteConfirmation = new PopupConfirmation.PopupConfirmationArgs("New Confirm Delete","Are you sure you want to delete").setOnAction(action->{
            PopupHtml.create("Header passed",action).show(getSupportFragmentManager(),null);
            return true;
        });



        deleteConfirmation.setKey("MainDeleteConfirmation");
        this.PopupArgs.add(deleteConfirmation);

        */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        Button btnCamera = findViewById(R.id.btnCamera);
        final BaseActivity activity = this;
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                test1.show(getSupportFragmentManager(),null);

                //PopupConfirmation.create(deleteConfirmation).show(getSupportFragmentManager(),null);;







            }
        });
        Button btnGallery = findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage("InvCheckIn",0L,new onGetImage() {
                    @Override
                    public void getImage(Bitmap image,long id) {
                        imageView.setImageBitmap(image);

                    }
                });
            }
        });
    }




    @Override
    public void onButtonClick(String action, RadioButton button) {

    }

    @Override
    public String getHeaderText() {
        return null;
    }



}