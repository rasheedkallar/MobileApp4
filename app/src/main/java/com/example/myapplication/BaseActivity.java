package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.myapplication.model.Control;
import com.example.myapplication.model.DataService;
import com.example.myapplication.model.PopupBase;
import com.example.myapplication.model.PopupDate;
import com.example.myapplication.model.PopupForm;
import com.example.myapplication.model.PopupHtml;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class BaseActivity extends AppCompatActivity  {


    private boolean  EnableScroll = false;
    public boolean getEnableScroll() {
        return EnableScroll;
    }
    public void setEnableScroll(boolean enableScroll) {
        EnableScroll = enableScroll;
    }




    public static  final int TAKE_IMAGE_FROM_CAMERA = 1;
    public static  final int TAKE_IMAGE_FROM_GALLERY = -1;
    private static final int GALLERY_PERMISSION_REQUEST_CODE = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE  = 2;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    public LinearLayout Container;
    public ArrayList<Control.ControlBase> Controls = new ArrayList<>();

    public static String IpAddress;
    public static String User;
    public static Integer Port = 80;
    public static Integer ControlWidth = 470;
    public static Integer ButtonWidth = 223;

    public static Integer ActionButtonWidth = 75;

    public static class SettingsPopupForm extends PopupForm
    {
        public SettingsPopupForm(){
            ArrayList<Control.ControlBase> controls = new ArrayList<Control.ControlBase>();
            controls.add(Control.getEditTextControl("IpAddress","Ip Address").setValue(IpAddress));
            controls.add(Control.getEditIntegerControl("Port","Port").setValue(Port));
            controls.add(Control.getEditIntegerControl("ControlWidth","Control Width").setValue(ControlWidth));
            controls.add(Control.getEditIntegerControl("ButtonWidth","Button Width").setValue(ButtonWidth));

            controls.add(Control.getEditIntegerControl("ActionButtonWidth","Action Button Width").setValue(ActionButtonWidth));

            controls.add(Control.getEditTextControl("User","User").setValue(User));
            setArgs(new PopupFormArgs("Settings",controls,"Settings",null));
        }



        @Override
        public void doOk() {





            SharedPreferences sharedPref = getRootActivity().getSharedPreferences("Settings",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            Control.EditTextControl ipAddress = getControl("IpAddress");
            Control.EditTextControl user = getControl("User");
            Control.EditIntegerControl port = getControl("Port");
            Control.EditIntegerControl controlWidth  = getControl("ControlWidth");
            Control.EditIntegerControl buttonWidth = getControl("ButtonWidth");
            Control.EditIntegerControl actionButtonWidth = getControl("ActionButtonWidth");



            editor.putString(ipAddress.getName(),ipAddress.getValue());
            editor.putString(user.getName(),user.getValue());
            editor.putInt(port.getName(),port.getValue());
            editor.putInt(controlWidth.getName(),controlWidth.getValue());
            editor.putInt(buttonWidth.getName(),buttonWidth.getValue());
            editor.putInt(actionButtonWidth.getName(),actionButtonWidth.getValue());

            editor.apply();

            IpAddress = ipAddress.getValue();
            User = user.getValue();
            Port = port.getValue();
            ControlWidth = controlWidth.getValue();
            ButtonWidth = buttonWidth.getValue();
            ActionButtonWidth = actionButtonWidth.getValue();
            dismiss();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {





        if(savedInstanceState != null) {
            Controls = (ArrayList<Control.ControlBase>) savedInstanceState.getSerializable("Controls");
        }
        super.onCreate(savedInstanceState);
        Container = new LinearLayout(this);
        LinearLayout.LayoutParams llValueP = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        Container.setLayoutParams(llValueP);
        Container.setOrientation(LinearLayout.VERTICAL);
        if(EnableScroll){
            ScrollView sv = new ScrollView(this);
            RelativeLayout.LayoutParams svP= new  RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            svP.setLayoutDirection(LinearLayout.HORIZONTAL);
            sv.setLayoutParams(svP);
            sv.addView(Container);
            setContentView(sv);

        }else{
            setContentView(Container);
        }





        //setContentView(R.layout.activity_base);
        //Container = (LinearLayout) findViewById(R.id.container);

        final BaseActivity activity = this;

        takePictureLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
                String newFileName = dateFormat.format(new Date());



                new DataService().upload(image_file, newFileName, image_entityName, image_entity_id, image_fileGroup, null, new Function<Long, Void>() {
                    @Override
                    public Void apply(Long aLong) {
                        Bitmap imageBitmap = null;
                        try {
                            imageBitmap = MediaStore.Images.Media.getBitmap(getBaseContext().getContentResolver(), image_uri);
                        }
                        catch (IOException e){

                        }
                        onCapturedImage( image_action ,imageBitmap,image_entityName,image_fileGroup,image_entity_id,aLong);
                        return null;
                    }
                }, this);



            }
        });
        pickImageLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Uri uri = result.getData().getData();

                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    File file;
                    try {
                        file = File.createTempFile("Image-AbuNaser",".jpg");
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
                        Toast.makeText(activity,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
                    String newFileName = dateFormat.format(new Date());
                    new DataService().upload(file, newFileName, image_entityName, image_entity_id, image_fileGroup, null, new Function<Long, Void>() {
                        @Override
                        public Void apply(Long aLong) {
                            onCapturedImage( image_action ,imageBitmap,image_entityName,image_fileGroup,image_entity_id,aLong);
                            return null;
                        }
                    }, this);


                }catch (IOException e){

                }
            }
        });
        if(Controls != null){

            for (int i = 0; i < Controls.size(); i++) {
                Controls.get(i).addView(Container);
            }
        }
        SharedPreferences sharedPref = getSharedPreferences("Settings",Context.MODE_PRIVATE);
        IpAddress = sharedPref.getString("IpAddress",null);
        User = sharedPref.getString("User",null);
        Port = sharedPref.getInt("Port",80);
        ControlWidth = sharedPref.getInt("ControlWidth",470);
        ActionButtonWidth = sharedPref.getInt("ActionButtonWidth",75);



    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        String image_uri_string = null;
        if(image_uri != null)image_uri_string = image_uri.getPath();
        savedInstanceState.putString("image_uri_string", image_uri_string);

        String image_file_string = null;
        if(image_file != null)
            image_file_string =image_file.getAbsolutePath();
        savedInstanceState.putString("image_file_string", image_file_string);

        savedInstanceState.putLong("image_entity_id",image_entity_id);
        savedInstanceState.putInt("image_action",image_action);
        savedInstanceState.putString("image_entityName",image_entityName);
        savedInstanceState.putString("image_fileGroup",image_fileGroup);



        savedInstanceState.putSerializable("Controls",Controls);




    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String image_uri_string = savedInstanceState.getString("image_uri_string");
        image_uri = null;
        if(image_uri_string != null)
            image_uri = Uri.parse(image_uri_string);


        String image_file_string = savedInstanceState.getString("image_file_string");
        image_file = null;
        if(image_file_string != null)
            image_file = new File(image_file_string);

        image_entity_id = savedInstanceState.getLong("image_entity_id");
        image_action = savedInstanceState.getInt("image_action");
        image_entityName = savedInstanceState.getString("image_entityName");
        image_fileGroup = savedInstanceState.getString("image_fileGroup");



        Controls = (ArrayList<Control.ControlBase>) savedInstanceState.getSerializable("Controls");


    }


    private long image_entity_id ;
    private String image_entityName;
    private String image_fileGroup;
    private int image_action = 0;
    private  File image_file;
    private Uri image_uri;
    private    void  ImageCapture(){
        // Camera permission is granted, you can proceed with camera-related operations
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                image_file = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(getBaseContext(),"Error in image capture." + ex.getMessage(),Toast.LENGTH_LONG).show();
            }
            if (image_file != null) {

                image_uri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", image_file);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    takePictureLauncher.launch(takePictureIntent);
                }catch (Exception err){
                    int a = 0;
                }



            }
        }
    }
    private    void  ImagePick(){
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(pickImageIntent);
    }


    //private Uri photoThumbnailURI;
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }



    protected String getEntityName(){
        String name = this.getClass().getName();
        int dot = name.lastIndexOf('.');
        if(dot >0)name= name.substring(dot + 1);
        if(name.endsWith("Activity"))name = name.substring(0,name.length() - 8);
        return name;
    }



    public void  captureImage(int action,String entityName,String fileGroup,long entityId){
        image_action = action;
        image_entityName  = entityName;
        image_fileGroup = fileGroup;
        if(image_action <0){
            image_entity_id = entityId;
            int galleryPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (galleryPermission == PackageManager.PERMISSION_GRANTED) {
                ImagePick();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_REQUEST_CODE);
            }
        }
        else{
            image_entity_id = entityId;

            int cameraPermission = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
            if (cameraPermission == PackageManager.PERMISSION_GRANTED) {
                ImageCapture();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            }
        }
    }

    public ArrayList<PopupBase> Popups = new ArrayList<PopupBase>();
    public void onCapturedImage(int action,Bitmap image,String entityName,String fileGroup,Long entityId,Long id){
        for (int i = 0; i < Popups.size(); i++) {
            if(PopupForm.class.isAssignableFrom(Popups.get(i).getClass())){
                PopupForm form = (PopupForm)Popups.get(i);
                form.onCapturedImage(action,image,entityName,fileGroup,entityId,id);
            }



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
            case "Stock Receive":
                intent = new Intent(this,InvCheckInActivity.class);
                break;

            case "Inspect Unit":
                intent = new Intent(this,InspectUnitActivity.class);
                break;
            case "Account Reconciliation":
                intent = new Intent(this,AccountReconciliation.class);
                break;
            case "Sales Preview":
                intent = new Intent(this,SalesPreview.class);
                break;
            case "Transaction Monitor":
                intent = new Intent(this,TransactionMonitor.class);
                break;
            case "Test":
                intent = new Intent(this,TestActivity.class);
                break;
            case "Settings":
                SettingsPopupForm ps = new SettingsPopupForm();
                ps.show(getSupportFragmentManager(),null);
                return true;
            default:
                super.onOptionsItemSelected(item);
                return  false;
        }
        startActivity(intent);
        return  true;
    }



}
