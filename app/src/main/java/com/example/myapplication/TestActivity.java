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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupHtml;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class TestActivity extends BaseActivity {

    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ImageView imageView;

    private  void  ImageCapture(){
        // Camera permission is granted, you can proceed with camera-related operations
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            takePictureLauncher.launch(takePictureIntent);
        }
    }
    private  void  ImagePick(){
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(pickImageIntent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        imageView = findViewById(R.id.imageView);
        Button btnCamera = findViewById(R.id.btnCamera);
        final BaseActivity activity = this;
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Drawable drawable = getResources().getDrawable(R.drawable.baseline_add_24,null);
                Canvas canvas = new Canvas();
                Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                canvas.setBitmap(bitmap);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                drawable.draw(canvas);
                imageView.setImageBitmap(bitmap);



            }
        });

        Button btnGallery = findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int galleryPermission = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE);
                if (galleryPermission == PackageManager.PERMISSION_GRANTED) {
                    ImagePick();
                } else {

                    //ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_REQUEST_CODE);
                }
            }
        });


        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Handle the image capture result here
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        imageView.setImageBitmap(imageBitmap);
                    }
                });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Handle the image pick result here
                        Uri selectedImageUri = result.getData().getData();
                        imageView.setImageURI(selectedImageUri);
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
