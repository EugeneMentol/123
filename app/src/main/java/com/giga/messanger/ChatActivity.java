package com.giga.messanger;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.giga.messanger.databinding.ActivityChatBinding;
import com.giga.messanger.message.Message;
import com.giga.messanger.message.MessagesAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private Uri filePath;

    public String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String chatId = getIntent().getStringExtra("chatId");

        loadMessages(chatId);

        binding.messageBtn.setOnClickListener(v -> {
            String message = binding.message.getText().toString();
            if (message.isEmpty() && imagePath == null){
                Toast.makeText(this, "Заполните сообщение", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            String date = simpleDateFormat.format(new Date());
            String image = "";
            if(!(imagePath == null)) image = imagePath;

            binding.message.setText("");
            imagePath = null;
            sendMessage(chatId, message, date, image);
        });
        binding.imageBtn.setOnClickListener(v -> {
            selectImage();
        });
    }

    private void sendMessage(String chatId, String message, String date, String image){
        if (chatId==null) return;

        HashMap<String, String> messageInfo = new HashMap<>();
        messageInfo.put("text", message);
        messageInfo.put("ownerId", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        messageInfo.put("date", date);
        messageInfo.put("image", image);

        FirebaseDatabase.getInstance().getReference().child("Chats").child(chatId)
                .child("messages").push().setValue(messageInfo);
    }

    private void loadMessages(String chatId){
        if (chatId==null) return;

        FirebaseDatabase.getInstance().getReference().child("Chats")
                .child(chatId).child("messages").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) return;

                        List<Message> messages = new ArrayList<>();
                        for (DataSnapshot messageSnapshot : snapshot.getChildren()){
                            String messageId = messageSnapshot.getKey();
                            String ownerId = messageSnapshot.child("ownerId").getValue().toString();
                            String text = messageSnapshot.child("text").getValue().toString();
                            String date = messageSnapshot.child("date").getValue().toString();
                            String image = messageSnapshot.child("image").getValue().toString();

                            messages.add(new Message(messageId, ownerId, text, date, image));
                        }

                        binding.messagesRv.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                        binding.messagesRv.setAdapter(new MessagesAdapter(messages));
                        binding.messagesRv.smoothScrollToPosition(20);
                        binding.messagesRv.smoothScrollToPosition(messages.size());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    ActivityResultLauncher<Intent> pickImageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode()== Activity.RESULT_OK && result.getData()!=null && result.getData().getData()!=null){
                        filePath = result.getData().getData();

                        uploadImage();
                    }
                }
            }
    );

    private void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        pickImageActivityResultLauncher.launch(intent);
    }
    private void uploadImage() {
        if (filePath != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String uuid = UUID.randomUUID().toString();

            FirebaseStorage.getInstance().getReference().child("images/" + uid + "/" + uuid)
                    .putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getBaseContext(), "Фотография добавлена", Toast.LENGTH_SHORT).show();
                            FirebaseStorage.getInstance().getReference().child("images/" + uid + "/" + uuid).getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        imagePath = uri.toString();
                                    });
                        }
                    });
        }
    }

}