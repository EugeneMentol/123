package com.giga.messanger.chats;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.giga.messanger.R;

public class ChatViewHolder extends RecyclerView.ViewHolder {

    TextView chat_name;
    public ChatViewHolder(@NonNull View itemView) {
        super(itemView);

        chat_name = itemView.findViewById(R.id.nick_name);
    }
}
