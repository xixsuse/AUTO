package it.science.unitn.lpsmt.auto.ui.fragment.main;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.Date;
import java.util.List;

import it.science.unitn.lpsmt.auto.controller.CostDAO;
import it.science.unitn.lpsmt.auto.controller.VehicleDAO;
import it.science.unitn.lpsmt.auto.controller.dao.DAOCost;
import it.science.unitn.lpsmt.auto.controller.dao.DAOVehicle;
import it.science.unitn.lpsmt.auto.model.Maintenance;
import it.science.unitn.lpsmt.auto.model.Place;
import it.science.unitn.lpsmt.auto.model.Refuel;
import it.science.unitn.lpsmt.auto.model.Vehicle;
import it.science.unitn.lpsmt.auto.model.util.Const;
import lpsmt.science.unitn.it.auto.R;

/**
 * TODO add doc
 */
public class MainFragment extends Fragment {
    public static final String TAG = MainFragment.class.getSimpleName();
    private FloatingActionsMenu fab;
    private VehicleInsertedCardViewAdapter vehicleInsertedAdapter;
//    private LastRefuelsCardViewAdapter lastRefuelAdapter;
    private DeadLineCardViewAdapter deadLineCardViewAdapter;

    public MainFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_main, container, false);
        this.initFAB(v);
        this.initRecycleView(v);
        return v;
    }

    private void initFAB(View v){
        // get the instanced one for onClick method: avoiding null references
        fab = (FloatingActionsMenu)v.findViewById(R.id.btnFab);
        FloatingActionButton bntAddRefuel = (FloatingActionButton)v.findViewById(R.id.btnAddRefuel);
        FloatingActionButton bntAddCost   = (FloatingActionButton)v.findViewById(R.id.btnAddCost);
        FloatingActionButton bntAddVehicle= (FloatingActionButton)v.findViewById(R.id.btnAddVehicle);
        FABActionListener fabal = new FABActionListener();
        bntAddRefuel.setOnClickListener(fabal);
        bntAddCost.setOnClickListener(fabal);
        bntAddVehicle.setOnClickListener(fabal);
    }

    private void initRecycleView( View v ){
        RecyclerView vehicleInserted = (RecyclerView) v.findViewById(R.id.frag_main_vehicle_inserted_view);
        vehicleInserted.setLayoutManager(new LinearLayoutManager(v.getContext()));
        vehicleInserted.setItemAnimator(new DefaultItemAnimator());
        this.vehicleInsertedAdapter = new VehicleInsertedCardViewAdapter(v.getContext());
        vehicleInserted.setAdapter(this.vehicleInsertedAdapter);

//        RecyclerView lastRefuel = (RecyclerView) v.findViewById(R.id.frag_main_last_refuel);
//        lastRefuel.setLayoutManager(new LinearLayoutManager(v.getContext()));
//        lastRefuel.setItemAnimator(new DefaultItemAnimator());
//        this.lastRefuelAdapter = new LastRefuelsCardViewAdapter(v.getContext());
//        lastRefuel.setAdapter(this.lastRefuelAdapter);

        RecyclerView deadlines = (RecyclerView) v.findViewById(R.id.frag_main_deadlines);
        deadlines.setLayoutManager(new LinearLayoutManager(v.getContext()));
        deadlines.setItemAnimator(new DefaultItemAnimator());
        this.deadLineCardViewAdapter = new DeadLineCardViewAdapter(v.getContext());
        deadlines.setAdapter(this.deadLineCardViewAdapter);
    }

//==================================================================================================
//  GETTER
//==================================================================================================
    private final class FABActionListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            switch ( view.getId() ){
                case R.id.btnAddRefuel:{
                    List<Vehicle> list = new DAOVehicle().getAll();
                    if( list.size() != 0 ) {
                        Vehicle v = list.get(0);
                        Location l = new Location(it.science.unitn.lpsmt.auto.controller.util.Const.LOCATION_PROVIDER);
                        Place p = new Place(Const.NO_DB_ID_SET, "MyAddress", l);
                        Refuel r = new Refuel(Const.NO_DB_ID_SET, v, 30f, 1.324f, new Date(), 30000, p);
                        DAOCost dao = new DAOCost();
                        dao.save(r);
                        Toast.makeText(view.getContext(), "Refuel saved.", Toast.LENGTH_LONG ).show();
//                        lastRefuelAdapter.notifyRefuelChanges();
                    }else{
                        Toast.makeText(view.getContext(), "No Vehicle to save the refuel.",
                                Toast.LENGTH_LONG ).show();
                    }
                    break;
                }
                case R.id.btnAddCost:{
                    List<Vehicle> list = new DAOVehicle().getAll();
                    if( list.size() != 0 ){
                        Vehicle v = list.get(0);
                        Location l = new Location(it.science.unitn.lpsmt.auto.controller.util.Const.LOCATION_PROVIDER);
                        Place p = new Place(Const.NO_DB_ID_SET, "MyAddress", l);
                        Maintenance m = new Maintenance(Const.NO_DB_ID_SET, v, 150f, "notes",
                                "SomeTax", Maintenance.Type.TAX, p, 100);

                        CostDAO dao = new DAOCost();
                        dao.save(m);
                        Toast.makeText(view.getContext(), "Maintenance saved.",
                                Toast.LENGTH_LONG ).show();
                        deadLineCardViewAdapter.notifyDeadLinesChanges();
                    }else{
                        Toast.makeText(view.getContext(), "No Vehicle to save the refuel.",
                                Toast.LENGTH_LONG ).show();
                    }
                    break;
                }
                case R.id.btnAddVehicle: {
                    VehicleDAO dao = new DAOVehicle();
                    Vehicle v = new Vehicle(Const.NO_DB_ID_SET, "myVehicle", "XX123", Vehicle.Fuel.GASOLINE, new Date());
                    dao.save(v);
                    Toast.makeText(view.getContext(), "Vehicle saved.", Toast.LENGTH_LONG ).show();
                    vehicleInsertedAdapter.notifyVehiclesChanges();
                    break;
                }
            }
            fab.collapse();
        }
    }
}
