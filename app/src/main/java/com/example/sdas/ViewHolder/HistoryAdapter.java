package com.example.sdas.ViewHolder;

import static android.content.ContentValues.TAG;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sdas.Model.History;
import com.example.sdas.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class HistoryAdapter extends FirebaseRecyclerAdapter<
        History, HistoryAdapter.HistoryViewholder> {

    public HistoryAdapter(
            @NonNull FirebaseRecyclerOptions<History> options)
    {
        super(options);
    }

    // Function to bind the view in Card view(here
    // "person.xml") with data in
    // model class(here "person.class")
    @Override
    protected void
    onBindViewHolder(@NonNull HistoryViewholder holder,
                     int position, @NonNull History model)
    {
//        Log.d(TAG, "<< Setting data to viewholder >>");

        // Add *variables* from model class (here
        // "History.class")to appropriate view in Card
        // view (here "adapter_history.xml")
        holder.date.setText(model.getDate());
        holder.time.setText(model.getTime());

//        String stringdouble= Double.toString(model.getDistance());
        String stringdouble= String.format("%.2f", model.getDistance());
        holder.distance.setText(stringdouble + " meter");

        holder.risk.setText(model.getRisk());
        if(model.getRisk().equals("High"))
            holder.risk.setTextColor(Color.RED);
        else if(model.getRisk().equals("Medium"))
            holder.risk.setTextColor(Color.parseColor("#A84420"));
        else
            holder.risk.setTextColor(Color.parseColor("#19543E"));



    }

    // Function to tell the class about the Card view (here
    // "person.xml")in
    // which the data will be shown
    @NonNull
    @Override
    public HistoryViewholder
    onCreateViewHolder(@NonNull ViewGroup parent,
                       int viewType)
    {
        View view
                = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_history, parent, false);
        return new HistoryAdapter.HistoryViewholder(view);
    }

    // Sub Class to create references of the views in Crad
    // view (here "person.xml")
    class HistoryViewholder
            extends RecyclerView.ViewHolder {
        TextView date, time, distance, risk;
        public HistoryViewholder(@NonNull View itemView)
        {
            super(itemView);

            date = itemView.findViewById(R.id.tvDate);
            time = itemView.findViewById(R.id.tvTime);
            distance = itemView.findViewById(R.id.tvDistance);
            risk = itemView.findViewById(R.id.tvRisk);
//            Log.d(TAG, "<< Setting data to itemview >>");

        }
    }
}
