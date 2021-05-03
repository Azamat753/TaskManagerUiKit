package com.lawlett.taskmanageruikit.idea;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.idea.data.model.QuickModel;
import com.lawlett.taskmanageruikit.utils.App;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class IdeaActivity extends AppCompatActivity {
    public static final int CAMERA_REQUEST = 500;
    public static final int GALLERY_REQUEST = 01;

    FloatingActionMenu materialDesignFAM;
    FloatingActionButton  floatingActionButtonCameraPicker, floatingActionButtonImagePicker;
    QuickModel quickModel;
    EditText e_title, e_description;
    ImageView back_view, done_view, image_title;
    String pickImage, textTitle, textDescription, captureImage, gallImage,
    defColor;
    boolean isGallery = false;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ideas);
      defColor = String.valueOf(getResources().getColor(R.color.myWhite));

        if (Build.VERSION.SDK_INT >= 21)
            getWindow().setNavigationBarColor(getResources().getColor(R.color.statusBarC));

        initView();
        getIncomingIntent();


        findViewById(R.id.back_view).setOnClickListener(v -> recordDataRoom());

        done_view.setOnClickListener(v -> {
            if (e_title.getText().toString().trim().isEmpty()) {
                Toast.makeText(IdeaActivity.this, R.string.add_title, Toast.LENGTH_LONG).show();
            } else {
                recordDataRoom();
            }
        });

    }

    private void getCurrentPhoto() {
        if (isGallery) {
            pickImage = gallImage;
        } else {
            pickImage = captureImage;
        }
    }

    @SuppressLint("ResourceAsColor")
    public void recordDataRoom() {
        textTitle = e_title.getText().toString();
        textDescription = e_description.getText().toString();
        if (!textTitle.equals("") || !textDescription.equals("")) {
            Calendar c = Calendar.getInstance();
            final int year = c.get(Calendar.YEAR);
            String[] monthName = {getString(R.string.january), getString(R.string.february), getString(R.string.march), getString(R.string.april), getString(R.string.may), getString(R.string.june), getString(R.string.july),
                    getString(R.string.august), getString(R.string.september), getString(R.string.october), getString(R.string.november), getString(R.string.december)};

            final String month = monthName[c.get(Calendar.MONTH)];
            String currentDate = new SimpleDateFormat("dd ", Locale.getDefault()).format(new Date());
            getCurrentPhoto();
            String myTitle = e_title.getText().toString();
            String myDesk = e_description.getText().toString();
            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

            if (quickModel != null) {
                if (pickImage != null) {
                    quickModel.setImage(pickImage);
                } else {
                    String myPickImage = quickModel.getImage();
                    quickModel.setTitle(myTitle);
                    quickModel.setDescription(myDesk);
                    quickModel.setImage(myPickImage);
                    quickModel.setColor(color);
                    quickModel.setCreateData(currentDate + " " + month + " " + year);
                }
                App.getDataBase().ideaDao().update(quickModel);
            } else {
                if(color == 0){
                    color = getResources().getColor(R.color.titleColor);
                }else {
                    color = e_title.getCurrentTextColor();
                }
                quickModel = new QuickModel(textTitle, textDescription, currentDate + " " + month + " " + year, pickImage, color, null);
                App.getDataBase().ideaDao().insert(quickModel);
            }
        }
        finish();
    }

    @SuppressLint("ResourceAsColor")
    public void getIncomingIntent() {
        Intent intent = getIntent();
        quickModel = (QuickModel) intent.getSerializableExtra("task");
        if (quickModel != null) {
            textTitle = quickModel.getTitle();
            e_title.setText(textTitle);
            textDescription = quickModel.getDescription();
            e_description.setText(textDescription);
            gallImage = quickModel.getImage();
            Glide.with(this).load(gallImage).into(image_title);
        }
    }

    @SuppressLint("ResourceAsColor")
    public void initView() {
        materialDesignFAM = findViewById(R.id.menu_floating);
        floatingActionButtonCameraPicker = findViewById(R.id.fab2);
        floatingActionButtonImagePicker = findViewById(R.id.fab3);
        image_title = findViewById(R.id.image_title);
        e_title = findViewById(R.id.edit_title2);
        e_description = findViewById(R.id.edit_description);
        back_view = findViewById(R.id.back_view);
        done_view = findViewById(R.id.done_view);
        floatingActionButtonCameraPicker.setOnClickListener(v -> {
            if (checkAndRequestPermissions(IdeaActivity.this)) {
                materialDesignFAM.close(true);
                openCamera();
            }
        });
        floatingActionButtonImagePicker.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_REQUEST);
            materialDesignFAM.close(true);
        });
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST) {
                final Uri imageUri = data.getData();
                gallImage = imageUri.toString();
                isGallery = true;
                Glide.with(this).load(imageUri).into(image_title);
            } else if (requestCode == CAMERA_REQUEST) {
                Bitmap thumbnailBitmap = (Bitmap) data.getExtras().get("data");
                assert thumbnailBitmap != null;
                Uri a = getImageUri(this, thumbnailBitmap);
                isGallery = false;
                if (captureImage == null) {
                    captureImage = a.toString();
                    Glide.with(this).load(captureImage).into(image_title);
                }
            }
        }
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "IMG_" + Calendar.getInstance().getTime(), null);
        return Uri.parse(path);
    }

    public static boolean checkAndRequestPermissions(Activity context) {
        int storagePermissions = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int cameraPermissions = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        List<String> listPermissions = new ArrayList<>();
        if (cameraPermissions != PackageManager.PERMISSION_GRANTED) {
            listPermissions.add(Manifest.permission.CAMERA);
        }
        if (storagePermissions != PackageManager.PERMISSION_GRANTED) {
            listPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(context, listPermissions.toArray(new String[listPermissions.size()]),
                    CAMERA_REQUEST);
            return false;
        }
        return true;
    }
}

