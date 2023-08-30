package com.example.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;

import android.Manifest;

import com.example.myapplication.model.PopupHtml;
import com.example.myapplication.model.UploadPhoto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class MainActivity extends BaseActivity {
   private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        Button btnCamera = findViewById(R.id.btnCamera);
        final BaseActivity activity = this;
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage("InvCheckIn",0L,new onGetImage() {
                    @Override
                    public void getImage(Bitmap image, long id) {
                        imageView.setImageBitmap(image);

                    }
                });
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