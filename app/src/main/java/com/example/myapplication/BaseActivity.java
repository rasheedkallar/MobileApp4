package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.model.DataService;
import com.example.myapplication.model.Popup;
import com.example.myapplication.model.PopupHtml;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Permission;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public abstract class BaseActivity extends AppCompatActivity {
    public static final int GALLERY_PERMISSION_REQUEST_CODE = 1;
    public static final int CAMERA_PERMISSION_REQUEST_CODE  = 2;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;
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

        Header.setText(getHeaderText());
        final BaseActivity activity = this;

        takePictureLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Bundle extras = result.getData().getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                File myDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                myDir.mkdirs();
                String fileName = "Image-AbuNaser.jpg";
                /*

                File file = new File(myDir, fileName);
                if (file.exists()){
                    //new SecurityManager().checkDelete(file.getAbsolutePath());


                    try {


                        file.delete();
                    }catch (Exception e){
                        Toast.makeText(activity,"deleted",Toast.LENGTH_SHORT).show();
                    }



                }
                else{
                     new PopupHtml(getBaseContext(),"file not exist","message");
                }

                //Toast.makeText(activity,"hi",Toast.LENGTH_LONG).show();
                //Toast.makeText(getBaseContext(), "Reason can not be blank", Toast.LENGTH_SHORT).show();
                //boolean e1 = file.exists();

                 */
                File file;
                try {
                    file = File.createTempFile("Image-AbuNaser","jpg");
                }
                catch (IOException e){
                    Toast.makeText(activity,e.getMessage(),Toast.LENGTH_LONG).show();
                    return;
                }



                try {
                    FileOutputStream out = new FileOutputStream(file);
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
                String newFileName = dateFormat.format(new Date());

                new DataService().upload(activity,file,newFileName,Entity,Id, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                        String result = new String(responseBody);
                        System.out.println(result);
                        if(imageListener != null)imageListener.getImage(imageBitmap,result);
                    }

                    @Override
                    public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                        String result = "Error found";
                        if(responseBody == null && error != null){
                            result = error.getMessage();
                        }
                        else if(responseBody != null){
                            result = new String(responseBody);
                        }
                        System.out.println(result);
                        if(imageListener != null)imageListener.getImage(imageBitmap,result);
                    }
                });


            }
        });

        pickImageLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Uri uri = result.getData().getData();

                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    /*

                    File myDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    myDir.mkdirs();
                    String fileName = "Image-AbuNaser.jpg";
                    File file = new File(myDir, fileName);
                    if (file.exists()) file.delete();

                     */


                    File file;
                    try {
                        file = File.createTempFile("Image-AbuNaser","jpg");
                    }
                    catch (IOException e){
                        Toast.makeText(activity,e.getMessage(),Toast.LENGTH_LONG).show();
                        return;
                    }
                    try {
                        FileOutputStream out = new FileOutputStream(file);
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
                    String newFileName = dateFormat.format(new Date());
                    new DataService().upload(activity,file,newFileName,Entity,Id, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                            String result = new String(responseBody);
                            System.out.println(result);
                            if(imageListener != null)imageListener.getImage(imageBitmap,result);
                        }

                        @Override
                        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                            String result = new String(responseBody);
                            System.out.println(result);
                            if(imageListener != null)imageListener.getImage(imageBitmap,result);
                        }
                    });
                }catch (IOException e){

                }
            }
        });
    }
    private    void  ImageCapture(){
        // Camera permission is granted, you can proceed with camera-related operations
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            takePictureLauncher.launch(takePictureIntent);


            //File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
            //intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));


        }
    }
    private    void  ImagePick(){
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(pickImageIntent);
    }


    public void  captureImage(String entity,Long id,onGetImage listener){

        Entity = entity;
        Id = id;
        imageListener  = listener;
        int cameraPermission = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        if (cameraPermission == PackageManager.PERMISSION_GRANTED) {
            ImageCapture();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    private String Entity;
    private long Id;

    private onGetImage imageListener = null;

    public void pickImage(String entity,long id,onGetImage listener){

        Entity = entity;
        Id = id;
        imageListener  = listener;
        int galleryPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (galleryPermission == PackageManager.PERMISSION_GRANTED) {
            ImagePick();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    ImageCapture();
                }
            }
        }
        else if (requestCode == GALLERY_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ImagePick();
            }
        }
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

            case "Test":
                intent = new Intent(this,TestActivity.class);
                break;
            default:
                super.onOptionsItemSelected(item);
                return  false;
        }
        startActivity(intent);
        return  true;
    }
    public static abstract class onGetImage{
        public abstract void getImage(Bitmap image,String message) ;

    }


}
