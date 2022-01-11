package com.example.sdas.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
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
            holder.background.setBackgroundColor(Color.parseColor("#FCD9D9"));
//            holder.risk.setTextColor(Color.parseColor("#D60F0F"));

        else if(model.getRisk().equals("Medium"))
            holder.background.setBackgroundColor(Color.parseColor("#FFF0D6"));
//            holder.risk.setTextColor(Color.parseColor("#FFA000"));
        else
            holder.background.setBackgroundColor(Color.parseColor("#E9FCEA"));
//            holder.risk.setTextColor(Color.parseColor("#43A047"));
    }

    // Function to tell the class about the Card view
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

    // Sub Class to create references of the views in Card
    class HistoryViewholder
            extends RecyclerView.ViewHolder {
        TextView date, time, distance, risk;
        ConstraintLayout background;
        public HistoryViewholder(@NonNull View itemView)
        {
            super(itemView);

            date = itemView.findViewById(R.id.tvDate);
            time = itemView.findViewById(R.id.tvTime);
            distance = itemView.findViewById(R.id.tvDistance);
            risk = itemView.findViewById(R.id.tvRisk);
            background = itemView.findViewById(R.id.clayout);
//            Log.d(TAG, "<< Setting data to itemview >>");

        }
    }



//    @Override
//    public History getItem(int position) {
//        return super.getItem(getItemCount() - 1 - position);
//    }
}
