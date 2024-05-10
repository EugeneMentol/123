package com.giga.messanger.users;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.giga.messanger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class UsersAdapter extends RecyclerView.Adapter<UserViewHolder> {

    private ArrayList<User> users = new ArrayList<>();

    public  UsersAdapter(ArrayList<User> users){
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.person_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.nick_name.setText(users.get(position).nickname);

        holder.itemView.setOnClickListener(view -> {
            createChat(users.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private void createChat(User user){
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        HashMap<String, String> chatInfo = new HashMap<>();
        chatInfo.put("user1", uid);
        chatInfo.put("user2", user.uid);

        String chatId = generateChatId(uid, user.uid);
        FirebaseDatabase.getInstance().getReference().child("Chats").child(chatId).setValue(chatInfo);

        addChatId(uid, chatId);
        addChatId(user.uid, chatId);
    }
    private String generateChatId(String userId1, String userId2){
        String sum = userId1+userId2;
        char[] charArray = sum.toCharArray();
        Arrays.sort(charArray);
        return new String(charArray);
    }
    private void addChatId(String uid, String chatId){
        FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("chats").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        String chats = task.getResult().getValue().toString();
                        String chatsUpd = addStrId(chats, chatId);

                        FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("chats").setValue(chatsUpd);
                    }
                });
    }
    private String addStrId(String string, String chatId){
        string += (string.isEmpty()) ? chatId : (","+chatId);
        return string;
    }
}
