package com.example.netrasagar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

public class RecyclerFishCatchAdapter extends RecyclerView.Adapter<RecyclerFishCatchAdapter.ViewHolder> {

    Context context;
    ArrayList<FishCatchList> fishcatchlst;

    RecyclerFishCatchAdapter(Context context, ArrayList<FishCatchList> ArrFishlist){
        this.context=context;
        this.fishcatchlst=ArrFishlist;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fish_catch_row, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txt_name.setText(fishcatchlst.get(position).fishname);
        if(fishcatchlst.get(position).rate>0){
            holder.txt_rate.setText(String.valueOf(fishcatchlst.get(position).rate));
        }else{
            holder.txt_rate.setVisibility(View.GONE);
        }
        holder.txt_qty.setText(String.format("%.2f",fishcatchlst.get(position).qty) + " KG");
      int fishId = fishcatchlst.get(position).id;
        if (Arrays.stream(new int[]{1, 2, 3}).anyMatch(f -> f == fishId)) {
            holder.img.setImageResource( R.drawable.f_mackerel);
        }
        else if(Arrays.stream(new int[] {4,5}).anyMatch(f -> f == fishId)){
            holder.img.setImageResource(  R.drawable.f_sardine);
        }
        else if(Arrays.stream(new int[] {6,7}).anyMatch(f -> f == fishId)){
            holder.img.setImageResource(  R.drawable.f_othersardine);
        }
        else if(Arrays.stream(new int[] {8,9,10,11}).anyMatch(f -> f == fishId)){
            holder.img.setImageResource(  R.drawable.f_prawn);
        }
        else if (fishId == 12){
            holder.img.setImageResource(  R.drawable.f_tigerprawn);
        }
        else if (fishId == 13){
            holder.img.setImageResource(  R.drawable.f_solarprawn);
        }
        else if(fishId == 14){
            holder.img.setImageResource(  R.drawable.f_kingfish);
        }
        else if(fishId == 17){
            holder.img.setImageResource(  R.drawable.f_rays);
        }
        else if(fishId == 20){
            holder.img.setImageResource(  R.drawable.f_catfish);
        }
        else if(fishId == 22){
            holder.img.setImageResource(  R.drawable.f_butterfish);
        }
        else if(fishId == 23){
            holder.img.setImageResource(  R.drawable.f_jewfish);
        }
        else if(fishId == 24){
            holder.img.setImageResource(  R.drawable.f_indiansalmon);
        }
        else if(fishId == 25){
            holder.img.setImageResource(  R.drawable.f_silverbelly);
        }
        else if(fishId == 30){
            holder.img.setImageResource(  R.drawable.f_pomfret);
        }
        else if(fishId == 33){
            holder.img.setImageResource(  R.drawable.f_mullet);
        }
        else if(fishId == 32){
            holder.img.setImageResource(  R.drawable.f_ladyfish);
        }
        else if(fishId == 34){
            holder.img.setImageResource(  R.drawable.f_caranx);
        }
        else if(fishId == 36){
            holder.img.setImageResource(  R.drawable.f_bobay_duck);
        }
        else if(fishId == 37){
            holder.img.setImageResource(  R.drawable.f_sepia);
        }
        else if(fishId == 38){
            holder.img.setImageResource(  R.drawable.f_perches);
        }
        else if(fishId == 40){
            holder.img.setImageResource(  R.drawable.f_ambasis);
        }
        else if(fishId == 41){
            holder.img.setImageResource(  R.drawable.f_ribbon);
        }
        else if(fishId == 45){
            holder.img.setImageResource(  R.drawable.f_eel);
        }
        else if(fishId == 39){
            holder.img.setImageResource(  R.drawable.f_crab);
        }
        else if(fishId == 48){
            holder.img.setImageResource(  R.drawable.f_cobia);
        }
        else if(fishId == 46){
            holder.img.setImageResource(  R.drawable.f_leather_jacket);
        }
        else if(fishId == 49 || fishId == 50){
            holder.img.setImageResource(  R.drawable.f_tuna);
        }
        else if(fishId == 51){
            holder.img.setImageResource(  R.drawable.f_barramundi);
        }
        else if(fishId == 57){
            holder.img.setImageResource(  R.drawable.f_barracuda);
        }
        else if(fishId == 62){
            holder.img.setImageResource(  R.drawable.f_common_carp);
        }
        else if(fishId == 54){
            holder.img.setImageResource(  R.drawable.f_grouper);
        }
        else if(fishId == 55){
            holder.img.setImageResource(  R.drawable.f_white_snapper);
        }
        else if(fishId == 56){
            holder.img.setImageResource(  R.drawable.f_sailfish);
        }
        else if(fishId == 58){
            holder.img.setImageResource(  R.drawable.f_lizard_fish);
        }
        else if(fishId == 29){
            holder.img.setImageResource(  R.drawable.f_silver_bar);
        }
        else if(fishId == 15 || fishId == 16){
            holder.img.setImageResource(  R.drawable.f_mori);
        }
        else if(fishId == 27){
            holder.img.setImageResource(  R.drawable.f_lobster);
        }
        else if(fishId == 31){
            holder.img.setImageResource(  R.drawable.f_paplet_black);
        }
        else if(fishId == 53){
            holder.img.setImageResource(  R.drawable.f_shetuk);
        }
        else if(fishId == 18){
            holder.img.setImageResource(  R.drawable.f_verli);
        }
        else if(fishId == 21){
            holder.img.setImageResource(  R.drawable.f_dodyaro);
        }
        else if(fishId == 28){
            holder.img.setImageResource(  R.drawable.f_lepo);
        }
        else {
            holder.img.setImageResource(  R.drawable.fishcartoon);
        }
        int pos1 = position;

        holder.llrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (context instanceof reports == false) {
                    final EditText input1 = new EditText(context);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context)
                            .setTitle(fishcatchlst.get(pos1).fishname)
                            .setMessage("Enter new Qty of Fish")
                            .setView(input1)
                            .setIcon(R.drawable.ic_qtyupdate)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (input1.getText().toString().matches("\\d+(?:\\.\\d+)?")) {
                                        fishcatchlst.get(pos1).qty = Double.valueOf(input1.getText().toString());
                                        notifyItemChanged(pos1);
                                    } else {
                                        Toast.makeText(context, "Qty should be Positive Decimal value.", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            })
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                    builder.show();
                }
            }
        });
        holder.llrow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (fishcatchlst.get(pos1).fish_catch_id == null) {
                    if (context instanceof reports == false) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                .setTitle("DELETE RECORD")
                                .setMessage("Are you sure to delete record?")
                                .setIcon(R.drawable.ic_delete)
                                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        fishcatchlst.remove(pos1);
                                        notifyDataSetChanged();
                                        Toast.makeText(context, "Fish catch record deleted", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                        builder.show();
                    }
                }
                else
                    Toast.makeText(context,"This item cannot be Deleted. Instead make Qty 0KG.",Toast.LENGTH_LONG).show();
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return fishcatchlst.size();
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
        TextView txt_name, txt_qty, txt_rate;
        LinearLayout llrow;

        public ViewHolder(View itemView) {
            super(itemView);
            txt_name = itemView.findViewById(R.id.FCR_FishName);
            txt_qty = itemView.findViewById(R.id.FCR_Qty);
            txt_rate = itemView.findViewById(R.id.FCR_Rate);
            img = itemView.findViewById(R.id.FCR_imgFish);
            llrow = itemView.findViewById(R.id.llrow);
        }
    }

}
