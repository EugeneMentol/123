package com.giga.messanger.message;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.giga.messanger.R;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    TextView message, date;
    ImageView image;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);

        image = itemView.findViewById(R.id.image);
        message = itemView.findViewById(R.id.message);
        date = itemView.findViewById(R.id.message_date);
    }
}
