package com.mahmoudjoe3.imagecoding;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final int REQCODE = 100;
    private static final String TAG = "TimeFrame";
    @BindView(R.id.code)
    TextView codeTXT;
    @BindView(R.id.image)
    ImageView image;
    String code;
    @BindView(R.id.decode)
    Button decode;
    @BindView(R.id.quality)
    EditText quality;
    int Quality=50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }

    private Boolean IsPermissionGranted() {
        return (
                ContextCompat.checkSelfPermission(this
                        , Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        );
    }

    @OnClick({R.id.encode, R.id.decode})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.encode:
                encodeImageClick();
                break;
            case R.id.decode:
                decodeImageClick();
                break;
        }
    }

    private void decodeImageClick() {
        Bitmap bitmap = ImageCompressor.decode_String_To_Image(code);
        image.setImageBitmap(bitmap);
    }



    private void encodeImageClick() {
        if (IsPermissionGranted()) {
            selectImage();
            decode.setEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQCODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQCODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage();
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImage() {
        codeTXT.setText("");
        image.setImageBitmap(null);
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQCODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            Quality=Integer.parseInt(quality.getText().toString());

            double startTime = System.nanoTime();
            Bitmap bitmap = uriToBitmap(uri);
            double stopTime = System.nanoTime();
            Log.d(TAG, "onActivityResult: uriToBitmap time ==> " + ((stopTime - startTime) / 1000000000)+" Sec ");

            double startTime_encode = System.nanoTime();
            code = ImageCompressor.encode_Image_To_String(bitmap,Quality);
            double stopTime_encode = System.nanoTime();
            Log.d(TAG, "onActivityResult: encode_Image_To_String time ==> " + ((stopTime_encode - startTime_encode) / 1000000000)+" Sec ");

//            double startTimeCode = System.nanoTime();
//            codeTXT.setText(code);
//            double stopTimeCode = System.nanoTime();
//            Log.d(TAG, "onActivityResult: codeText time ==> "+(stopTimeCode -startTimeCode)/1000000000);
            double codeSize=(double) (((code.length() * 2) / 1024)/1024);
            Log.d(TAG, "onActivityResult: encoded image Size ==> " +  codeSize+ " kB");
            Toast.makeText(this, "new image Size = "+codeSize+" MB \n And in KB --> "+
                    (double)((code.length() * 2) / 1024)+" KB", Toast.LENGTH_LONG).show();

        }
    }




    private Bitmap uriToBitmap(Uri uri) {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT >= 29) {
            ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), uri);
            try {
                bitmap = ImageDecoder.decodeBitmap(source);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

}