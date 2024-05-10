package com.giga.messanger;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;


import com.giga.messanger.bot.add.AddChat;
import com.giga.messanger.bot.chats.Chats;
import com.giga.messanger.bot.prof.Prof;
import com.giga.messanger.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    CameraDevice mCameraDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        } else {

            getSupportFragmentManager().beginTransaction().replace(binding.container.getId(), new Chats()).commit();
            binding.botNav.setSelectedItemId(R.id.chats);

            Map<Integer, Fragment> fragmentMap = new HashMap<>();
            fragmentMap.put(R.id.chats, new Chats());
            fragmentMap.put(R.id.add_chat, new AddChat());
            fragmentMap.put(R.id.prof, new Prof());

            binding.botNav.setOnItemSelectedListener(item -> {
                Fragment fragment = fragmentMap.get(item.getItemId());
                getSupportFragmentManager().beginTransaction().replace(binding.container.getId(), fragment).commit();
                return true;
            });

            binding.cameraBtn.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, CameraActivity.class));
            });
        }
    }
}