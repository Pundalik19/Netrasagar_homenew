package com.example.netrasagar.recyclers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.netrasagar.R;

import java.util.ArrayList;
import java.util.List;

public class RecyclerCrewAdapter extends RecyclerView.Adapter<RecyclerCrewAdapter.ViewHolder> implements Filterable {
    private final Context context;
    ArrayList<CrewDisplayList> CrwList;
    ArrayList<CrewDisplayList> CrwListFull;
    int RecyclerViewName;
    TextView txtTotCrew;
    EditText txtCrewAdditional;

    public RecyclerCrewAdapter(Context context, ArrayList<CrewDisplayList> ArrCrewList){
        this.context=context;
        this.CrwList=ArrCrewList;

    }
    public RecyclerCrewAdapter(Context context, ArrayList<CrewDisplayList> ArrCrewList, TextView txtTotCrew, EditText txtCrewAdditional){
        this.context=context;
        this.CrwList=ArrCrewList;
        this.txtTotCrew=txtTotCrew;
        this.txtCrewAdditional=txtCrewAdditional;
    }
    public void full_table_copy(){
        CrwListFull = new ArrayList<>(CrwList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.crew_row, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        RecyclerViewName=parent.getId();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int pos = position;
        holder.name.setText(CrwList.get(pos).Name);
        holder.aadhar.setText(CrwList.get(pos).Aadhar);
        holder.status.setText(CrwList.get(pos).vesselStatus);
        //holder.txt_qty.setText(String.format("%.2f",fishcatchlst.get(position).qty) + " KG");
        if (CrwList.get(pos).status) {
            holder.crewSelect.setVisibility(View.VISIBLE);
        }
        else {
            holder.crewSelect.setVisibility(View.INVISIBLE);
        }
        holder.crewSelect.setChecked(CrwList.get(pos).checked);

        holder.crewSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                CrwList.get(pos).checked=b;
                for(CrewDisplayList item :CrwListFull){
                    if(item.Aadhar.equals(CrwList.get(pos).Aadhar)){
                        item.checked=b;
                    }
                }
            }
        });

        holder.crewRow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(RecyclerViewName== R.id.po_crewList ){
                    String cName = CrwList.get(pos).Name;
                    CrwList.remove(pos);
                    notifyDataSetChanged();
                    Toast.makeText(context, cName+" removed from list.", Toast.LENGTH_SHORT).show();
                    update_crew_count();
                }

                return false;
            }
        });

    }

    public void update_crew_count(){
        try{
            txtTotCrew.setText(String.valueOf(CrwList.size() + Integer.valueOf(txtCrewAdditional.getText().toString())));
        }catch(NumberFormatException ee){
            txtTotCrew.setText(String.valueOf(CrwList.size() ));
            Log.d("update_crew_count",ee.toString());
        }

    }

    public void remove_item(ArrayList<CrewDisplayList> itemlist){
        CrwList.removeAll(itemlist);
        CrwListFull.removeAll(itemlist);
    }
    public void remove_item(CrewDisplayList item){
        CrwList.remove(item);
        CrwListFull.remove(item);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return CrwList.size();
    }

       public class ViewHolder extends RecyclerView.ViewHolder{
        TextView name, aadhar, status;
        CheckBox crewSelect;
        ConstraintLayout crewRow;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.crw_Name);
            aadhar = itemView.findViewById(R.id.crw_aadhar);
            status = itemView.findViewById(R.id.crw_status);
            crewSelect = itemView.findViewById(R.id.crw_chk);
            crewRow = itemView.findViewById(R.id.crw_row);
        }
    }

    @Override
    public Filter getFilter() {
        return FilteredItems;
    }

    private Filter FilteredItems = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<CrewDisplayList> filteredList = new ArrayList<>();
            if(constraint == null || constraint.length()==0){
                filteredList.addAll(CrwListFull);
            }else{
                String filterPattern = constraint.toString().toLowerCase();
                for(CrewDisplayList item :CrwListFull){
                    if(item.Name.toLowerCase().contains(filterPattern) || item.Aadhar.toLowerCase().contains(filterPattern)){
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values=filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            CrwList.clear();
            CrwList.addAll((List)filterResults.values);
            notifyDataSetChanged();

        }
    };
}
