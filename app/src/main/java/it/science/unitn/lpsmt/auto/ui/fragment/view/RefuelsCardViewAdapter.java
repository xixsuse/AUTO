package it.science.unitn.lpsmt.auto.ui.fragment.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.science.unitn.lpsmt.auto.model.Refuel;
import lpsmt.science.unitn.it.auto.R;

/**
 * this is the card adapter for the Recycle view of MainFragment
 */
public class RefuelsCardViewAdapter extends RecyclerView.Adapter<RefuelsCardViewAdapter.ViewHolder> {
    private Context context;

    private ArrayList<Refuel> refuels = new ArrayList<>();

    /**
     * TODO add doc
     * @param c
     */
    public RefuelsCardViewAdapter(Context c){
        this.context = c;
    }

    /**
     * TODO add doc
     * @param refuels
     */
    public void setData( List<Refuel> refuels ){
        this.refuels.clear();
        if(refuels != null)
            this.refuels.addAll(refuels);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int rowLayout = R.layout.adapter_frag_main_last_refuels;
        View v = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Refuel r = this.refuels.get(position);
        holder.refuelAssociated = r;
        holder.address.setText( r.getPlace().getAddress() );
        holder.amount.setText( r.getAmount()+" euro at "+r.getPricePerLiter()+" euro/liter");
        holder.vehicle.setText( r.getVehicle().getName()+" - "+r.getVehicle().getPlate() );
    }

    @Override
    public int getItemCount() { return this.refuels.size(); }

//==================================================================================================
//  INNER CLASS
//==================================================================================================
    public static class ViewHolder extends RecyclerView.ViewHolder{
        public Refuel refuelAssociated;
        public TextView address;
        public TextView amount;
        public TextView vehicle;
        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), "Refuel id: " + refuelAssociated.getId(),
                            Toast.LENGTH_LONG).show();
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Toast.makeText(view.getContext(), "Long press on Refuel id: " + refuelAssociated.getId(),
                            Toast.LENGTH_LONG).show();
                    return false;
                }
            });
            address = (TextView) itemView.findViewById(R.id.card_view_last_refuel_address);
            amount  = ( TextView ) itemView.findViewById(R.id.card_view_last_refuel_amount);
            vehicle = ( TextView ) itemView.findViewById(R.id.card_view_last_refuel_vehicle);
        }
    }
}