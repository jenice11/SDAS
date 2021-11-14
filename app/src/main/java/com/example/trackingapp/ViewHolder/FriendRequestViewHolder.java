package com.example.trackingapp.ViewHolder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackingapp.Interface.IRecyclerItemClickListener;
import com.example.trackingapp.R;

public class FriendRequestViewHolder extends RecyclerView.ViewHolder {
    public TextView txt_user_email;
    public Button btn_accept,btn_decline;

    public FriendRequestViewHolder(@NonNull View itemView) {
        super(itemView);
        txt_user_email = itemView.findViewById(R.id.txt_user_email);
        btn_accept = itemView.findViewById(R.id.btn_accept);
        btn_decline = itemView.findViewById(R.id.btn_decline);

    }

}
