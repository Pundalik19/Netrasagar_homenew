package com.example.netrasagar;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerVesselAdapter extends RecyclerView.Adapter<RecyclerVesselAdapter.ViewHolder> {

    private final Context context;
    ArrayList<VesselPassList> VesselList;

    RecyclerVesselAdapter(Context context, ArrayList<VesselPassList> ArrVesselList){
        this.context=context;
        this.VesselList=ArrVesselList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.vessel_row, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final  int pos = position;
        holder.name.setText(VesselList.get(position).vesselName);
        holder.vrc.setText(VesselList.get(position).vrc);
        holder.passStatus.setText(VesselList.get(position).passStatus);
        holder.jetty.setText(VesselList.get(position).jetty);
        //holder.txt_qty.setText(String.format("%.2f",fishcatchlst.get(position).qty) + " KG");
        if (VesselList.get(position).vesselstatus) {
            holder.img.setImageResource(R.drawable.boat_blue);
        }
        else {
            holder.img.setImageResource(  R.drawable.boat_red);
        }
        holder.vslrw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context instanceof reprint_pass){
                    Intent intent = new Intent(context,print_pass.class);
                    intent.putExtra("passNo",VesselList.get(pos).passNo);
                    intent.putExtra("StateId",VesselList.get(pos).state_id);
                    context.startActivity(intent);
                    ((reprint_pass) context).finish();
                }
            }
        });



    }

    @Override
    public int getItemCount() {
        return VesselList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView img;
        TextView name, vrc, passStatus, jetty;
        LinearLayout vslrw;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.vRow_img);
            name = itemView.findViewById(R.id.vRow_Name);
            vrc = itemView.findViewById(R.id.vRow_vrc);
            passStatus = itemView.findViewById(R.id.vRow_PassStatus);
            vslrw = itemView.findViewById(R.id.vsl_row);
            jetty = itemView.findViewById(R.id.vRow_jetty);
        }
    }
}
