package com.example.testfreel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Proprietes
    private Button btnPrendrePhoto;
    private Button btnEnreg;
    private Button btnSelectImage;
    private ImageView imgAffichePhoto;
    private ImageView imgStitching;
    private Bitmap imageBitmap = null;

    private Bitmap part1 = null;
    private Bitmap part2 = null;
    private Bitmap part3 = null;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_MULTIPLE_IMAGE_GALLERY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OpenCVLoader.initDebug();
        
        initActivity();
    }

    public void stitchHorizontal(Bitmap part1, Bitmap part2, Bitmap part3){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false; // Leaving it to true enlarges the decoded image size.

        Mat img1 = new Mat();
        Mat img2 = new Mat();
        Mat img3 = new Mat();

        Utils.bitmapToMat(part1, img1);
        Utils.bitmapToMat(part2, img2);
        Utils.bitmapToMat(part3, img3);

        Bitmap imgBitmap = stitchImagesHorizontal(Arrays.asList(img1, img2, img3));
        imgStitching.setImageBitmap(imgBitmap);
    }

    Bitmap stitchImagesHorizontal(List<Mat> src) {
        Mat dst = new Mat();
        Core.hconcat(src, dst);
        Bitmap imgBitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, imgBitmap);

        return imgBitmap;
    }

    /**
     *  Initialisation activite
     */
    private void initActivity() {
        // Recuperation des objets graphiques
        btnPrendrePhoto = (Button) findViewById(R.id.btnPrendrePhoto);
        imgAffichePhoto = (ImageView) findViewById(R.id.imgAffichePhoto);
        imgStitching = (ImageView) findViewById(R.id.imgStitching);
        btnEnreg = (Button) findViewById(R.id.btnEnreg);
        btnSelectImage = (Button) findViewById(R.id.btnSelectImage);

        // Methode pour gerer les evenements
        createOnClickBtnPrendrePhoto();
        createOnclickBtnEnreg();
        createOnclickBtnSelectImage();

    }

    /**
     *  Evenement clic  sur le bouton btnSelectImage
     */
    private void createOnclickBtnSelectImage() {
        btnSelectImage.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent =  new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Selection image multiple"), REQUEST_MULTIPLE_IMAGE_GALLERY);
            }
        });
    }

    /**
     *  Evenement clic  sue le bouton btnEnreg
     */
    private void createOnclickBtnEnreg(){
        //
        btnEnreg.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("COUCOU", "Save picture");
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                // Enregistrer la photo
                MediaStore.Images.Media.insertImage(getContentResolver(), imageBitmap, "part" + timeStamp, "COUCOUCOU");
            }
        });
    }

    /**
     *  Evenement clic sur bouton btnPrendrePhoto
     */
    private void createOnClickBtnPrendrePhoto(){
        btnPrendrePhoto.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                prendreUnePhoto();
            }
        });
    }


    /**
     *  Acces appareil photo et memorise
     */
    private void prendreUnePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imgAffichePhoto.setImageBitmap(imageBitmap);
        } else if(requestCode == REQUEST_MULTIPLE_IMAGE_GALLERY && resultCode == RESULT_OK) {
            ClipData clipData = data.getClipData();

            if(clipData != null){
                imgStitching.setImageURI(clipData.getItemAt(0).getUri());
                try {
                    part1 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), clipData.getItemAt(0).getUri());
                    part2 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), clipData.getItemAt(1).getUri());
                    part3 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), clipData.getItemAt(2).getUri());
//                    if(part1 != null && part2 != null && part3 != null){
                    stitchHorizontal(part1, part2, part3);
//                    }
                    for(int i = 0; i < clipData.getItemCount(); i++){
                        ClipData.Item item = clipData.getItemAt(i);
                        Uri uri = item.getUri();
                        Log.e("IMAGE => ", uri.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                part1 = (Bitmap) clipData.getItemAt(1).getIntent().getExtras().get("data");
//                part2 = (Bitmap) clipData.getItemAt(2).getIntent().getExtras().get("data");
//                part3 = (Bitmap) clipData.getItemAt(3).getIntent().getExtras().get("data");

            }
        }
    }

}

