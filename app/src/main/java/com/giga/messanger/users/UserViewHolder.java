package com.giga.messanger.users;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.RecyclerView;

import com.giga.messanger.R;

public class UserViewHolder extends RecyclerView.ViewHolder {

    TextView nick_name;
    public UserViewHolder(@NonNull View itemView) {
        super(itemView);

        nick_name = itemView.findViewById(R.id.nick_name);

    }
}
