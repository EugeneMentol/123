package com.giga.messanger;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.giga.messanger.databinding.ActivityLoginBinding;
import com.giga.messanger.databinding.ActivityRegBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;

public class RegActivity extends AppCompatActivity {

    private ActivityRegBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.logBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.emailEd.getText().toString().isEmpty() || binding.passEd.getText().toString().isEmpty() || binding.nickEd.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),"Заполните поля", Toast.LENGTH_SHORT).show();
                }else{
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(binding.emailEd.getText().toString(),binding.passEd.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    HashMap<String,String> userInfo = new HashMap<>();
                                    userInfo.put("chats","");
                                    userInfo.put("email", binding.emailEd.getText().toString());
                                    userInfo.put("nickname",binding.nickEd.getText().toString());
                                        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue(userInfo);
                                    startActivity(new Intent(RegActivity.this, MainActivity.class));
                                }
                            });
                }
            }
        });

    }
}