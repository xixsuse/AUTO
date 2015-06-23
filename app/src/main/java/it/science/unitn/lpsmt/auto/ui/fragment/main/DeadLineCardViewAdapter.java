package it.science.unitn.lpsmt.auto.ui.fragment.main;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import it.science.unitn.lpsmt.auto.controller.CostDAO;
import it.science.unitn.lpsmt.auto.controller.dao.DAOCost;
import it.science.unitn.lpsmt.auto.model.Maintenance;
import lpsmt.science.unitn.it.auto.R;

/**
 * this is the card adapter for the Recycle view of MainFragment
 */
public class DeadLineCardViewAdapter extends RecyclerView.Adapter<DeadLineCardViewAdapter.ViewHolder>{
    private Context context;

    private ArrayList<Maintenance> deadlines = new ArrayList<>();

    public DeadLineCardViewAdapter( Context c ){
        this.context = c;
        this.populateDeadLines();
    }

    private void populateDeadLines(){
        CostDAO dao = new DAOCost();
        this.deadlines.clear();
        this.deadlines.addAll(dao.getAllMaintenanceWhereTypeIs(Maintenance.Type.TAX));
        dao.close();
    }

    public void notifyDeadLinesChanges(){
        populateDeadLines();
        notifyDataSetChanged();
    }

    public boolean isAdapterEmpty(){ return this.deadlines.isEmpty(); }

    public String getPaymentSymbol(){
        Locale def = Locale.getDefault();
        if( def.equals(Locale.ITALY) )
            return " €";
        else if( def.equals(Locale.US) )
            return " $";
        else
            return "";
    }

//==================================================================================================
//  OVERRIDE
//==================================================================================================
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int rowLayout = R.layout.adapter_frag_main_deadlines;
        View v = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Maintenance m = this.deadlines.get(position);
        holder.maintenanceAssociated = m;
        holder.name.setText(m.getName());
        holder.amount.setText(m.getAmount()+getPaymentSymbol());
    }

    @Override
    public int getItemCount() { return this.deadlines.size(); }

//==================================================================================================
//  INNER CLASS
//==================================================================================================
    public Drawable DEFAULT_BG;
    public MyActionMode mode;

    public class ViewHolder extends RecyclerView.ViewHolder{
        public Maintenance maintenanceAssociated;
        public TextView name;
        public TextView amount;
        public ViewHolder( final View itemView) {
            super(itemView);
            DEFAULT_BG = itemView.getBackground();

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mode == null) {
                        Toast.makeText(view.getContext(), "Maintenance id: " + maintenanceAssociated.getId(),
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if( mode == null ) {
                        mode = new MyActionMode(itemView);
                        view.startActionMode(mode);
                        itemView.setBackgroundColor(Color.rgb(197, 202, 233));
                        return true;
                    }else{
                        return false;
                    }
                }
            });
            name = (TextView) itemView.findViewById(R.id.card_view_deadlines_name);
            amount = (TextView) itemView.findViewById(R.id.card_view_deadlines_amount);
        }
    }

    // https://goo.gl/1jIz4a
    public class MyActionMode implements ActionMode.Callback {
        private View item;
        public MyActionMode(View item) { this.item = item; }

        @Override // Called when the action mode is created; startActionMode() was called
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.setTitle("Actions");
            actionMode.getMenuInflater().inflate(R.menu.menu_action_delete_and_modify, menu);
            return true;
        }

        @Override // Called each time the action mode is shown.
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override // Called when the user selects a contextual menu item
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.modify: {
                    Toast.makeText(
                        DeadLineCardViewAdapter.this.context,
                        "modify action",
                        Toast.LENGTH_LONG
                    ).show();
                    actionMode.finish();
                    return true;
                }
                case R.id.delete: {
                    Toast.makeText(
                        DeadLineCardViewAdapter.this.context,
                        "delete action",
                        Toast.LENGTH_LONG
                    ).show();
                    actionMode.finish();
                    return true;
                }
                default: return false;
            }
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override // Called when the user exits the action mode
        public void onDestroyActionMode(ActionMode actionMode) {
            mode = null;
            item.setBackground(DEFAULT_BG);
        }
    }
}
