package com.giga.messanger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.giga.messanger.databinding.ActivityCameraBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 83854;
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 83123;
    private ActivityCameraBinding binding;
    private CameraSelector cameraSelector = new CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build();

    private ImageCapture imageCapture;

    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    String[] spin = { "Обычный", "Черно-белый", "Негатив"};
    String item = "Обычный";


    YUVtoRGB translator = new YUVtoRGB();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        } else {
            initializeCamera();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, spin);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinner.setAdapter(adapter);

        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                item = (String)parent.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        binding.spinner.setOnItemSelectedListener(itemSelectedListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeCamera();
        }
        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takePhoto();
        }
    }

    private void initializeCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                //Preview preview = new Preview.Builder().build();

                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .build();

                binding.saveBnt.setOnClickListener(v -> {
                            if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSION_WRITE_EXTERNAL_STORAGE);
                            }else{
                                takePhoto();
                            }
                        });

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(CameraActivity.this),
                        image -> {
                            Image img = image.getImage();
                            Bitmap savedBitmap = translator.translateYUV(img, CameraActivity.this);
                            Bitmap modifiedBitmap = modifyBitmap(savedBitmap);


                            binding.preview.setRotation(image.getImageInfo().getRotationDegrees());
                            binding.preview.setImageBitmap(modifiedBitmap);
                            image.close();
                        });

                Camera camera = cameraProvider.bindToLifecycle(CameraActivity.this, cameraSelector, imageAnalysis);
                cameraProvider.bindToLifecycle(CameraActivity.this, cameraSelector, imageCapture);

                binding.swBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if(isChecked){
                        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                    }else{
                        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                    }
                    cameraProvider.shutdown();
                    initializeCamera();
                });

                binding.ltBtn.setOnCheckedChangeListener((buttonView, isChecked) -> camera.getCameraControl().enableTorch(isChecked));

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {

        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH_mm_ss");
        String name = simpleDateFormat.format(new Date());

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image");


        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(CameraActivity.this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

                Bitmap bitmap = BitmapFactory.decodeFile("/storage/emulated/0/Pictures/CameraX-Image/" + name + ".jpg");
                Bitmap modifiedBitmap = modifyBitmap(bitmap);


                FileOutputStream out = null;
                try {
                    out = new FileOutputStream("/storage/emulated/0/Pictures/CameraX-Image/" + name + ".jpg");
                    modifiedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                Toast.makeText(getBaseContext(), "Фотография сохранена", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                exception.printStackTrace();
                Toast.makeText(getBaseContext(), "Ошибка", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bitmap modifyBitmap(Bitmap bitmap) {
        Bitmap savedBitmap = bitmap.copy(bitmap.getConfig(), true);
        if (item == "Обычный") {
        }else if (item == "Черно-белый") {
            int size = savedBitmap.getWidth() * savedBitmap.getHeight();
            int[] pixels = new int[size];
            savedBitmap.getPixels(pixels, 0, savedBitmap.getWidth(), 0, 0,
                    savedBitmap.getWidth(), savedBitmap.getHeight());
                for (int i = 0; i < size; i++) {
                    int color = pixels[i];
                    int r = color >> 16 & 0xff;
                    int g = color >> 8 & 0xff;
                    int b = color & 0xff;
                    int gray = (r + g + b) / 3;
                    pixels[i] = 0xff000000 | gray << 16 | gray << 8 | gray;
                }
            savedBitmap.setPixels(pixels, 0, savedBitmap.getWidth(), 0, 0,
                    savedBitmap.getWidth(), savedBitmap.getHeight());

        }else if (item == "Негатив") {
            int size = savedBitmap.getWidth() * savedBitmap.getHeight();
            int[] pixels = new int[size];
            savedBitmap.getPixels(pixels, 0, savedBitmap.getWidth(), 0, 0,
                    savedBitmap.getWidth(), savedBitmap.getHeight());
                for (int i = 0; i < size; i++) {
                    int color = pixels[i];
                    int r = 255 - (color >> 16 & 0xff);
                    int g = 255 - (color >> 8 & 0xff);
                    int b = 255 - (color & 0xff);
                    pixels[i] = 0xff000000 | r << 16 | g << 8 | b;
                }
            savedBitmap.setPixels(pixels, 0, savedBitmap.getWidth(), 0, 0,
                    savedBitmap.getWidth(), savedBitmap.getHeight());
        }
        return savedBitmap;
    }
}