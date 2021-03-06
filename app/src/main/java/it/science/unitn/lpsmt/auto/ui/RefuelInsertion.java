package it.science.unitn.lpsmt.auto.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognizerIntent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import it.science.unitn.lpsmt.auto.controller.calendar.CalendarUtils;
import it.science.unitn.lpsmt.auto.controller.dao.DAOCost;
import it.science.unitn.lpsmt.auto.controller.dao.DAOVehicle;
import it.science.unitn.lpsmt.auto.controller.util.DateUtils;
import it.science.unitn.lpsmt.auto.model.Place;
import it.science.unitn.lpsmt.auto.model.Refuel;
import it.science.unitn.lpsmt.auto.model.Vehicle;
import it.science.unitn.lpsmt.auto.model.util.Const;
import it.science.unitn.lpsmt.auto.ui.service.GPSService;
import it.science.unitn.lpsmt.auto.ui.util.Preferences;
import lpsmt.science.unitn.it.auto.R;

public class RefuelInsertion extends ActionBarActivity {
    public static String TAG = RefuelInsertion.class.getSimpleName();
    public static final int REQUEST_CODE = 1010;
    public static final int STT_REQUEST_CODE = 1011;
    public static final String UPDATE_REFUEL = "update_refuel_id";

    private Refuel refuelToUpdate;
    private List<Vehicle> vehicleList;
    private static Location locationFromGPS;

    // gui components
    private Spinner spinnerVehicleAssociated;
    private EditText editKM;
    private static EditText editCurrentPlace;
    private Switch switchGetCurrentPlace;
    private EditText editAmount;
    private EditText editPpl;
    private Switch switchToday;
    private EditText editDate;
    private EditText editNotes;

    // filed for gps service
    private ServiceConnection mConnection = new MyServiceConnection();
    private Messenger mMessenger = new Messenger(new ServiceHandler());
    private Messenger mService;

//==================================================================================================
//  CHECK AND SAVING METHODS
//==================================================================================================
    private boolean save(){
        // get the associated vehicle
        Vehicle v = this.vehicleList.get(this.spinnerVehicleAssociated.getSelectedItemPosition()-1);

        // build new Place
        Place p = new Place(Const.NO_DB_ID_SET, editCurrentPlace.getText().toString(), locationFromGPS);

        // get the km
        Integer km = Integer.parseInt(this.editKM.getText().toString());

        // get the amount
        Float amount = Float.parseFloat(this.editAmount.getText().toString());

        // get the price per liter
        Float ppl = Float.parseFloat(this.editPpl.getText().toString());

        // get the data
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date atData;
        try {
            atData = f.parse(this.editDate.getText().toString());
        } catch (ParseException i){
            displayToast(R.string.activity_refuel_insertion_refuel_data_parse_error);
            return false;
        }

        // get the notes
        String notes = "";
        if( !this.editNotes.getText().toString().isEmpty() ){
            notes = this.editNotes.getText().toString();
        }

        Refuel r = new Refuel(Const.NO_DB_ID_SET, v, amount, notes, ppl, atData, km, p );
        new DAOCost().save(r);
        return true;
    }

    private boolean checkFields(){
        if( this.spinnerVehicleAssociated.getSelectedItemPosition() == 0 ) {
            displayToast(R.string.activity_refuel_insertion_vehicle_not_selected);
            return false;
        }

        if( this.editKM.getText().toString().isEmpty() ){
            displayToast(R.string.activity_refuel_insertion_vehicle_km_missing);
            return false;
        }

        if( this.editAmount.getText().toString().isEmpty() ){
            displayToast(R.string.activity_refuel_insertion_refuel_amount_missing);
            return false;
        }

        if( this.editPpl.getText().toString().isEmpty() ){
            displayToast(R.string.activity_refuel_insertion_refuel_price_per_liter_missing);
            return false;
        }

        if( this.editDate.getText().toString().isEmpty() ){
            displayToast(R.string.activity_refuel_insertion_refuel_data_missing);
            return false;
        }

        return true;
    }

//==================================================================================================
//  UTILITIES METHODS
//==================================================================================================
    private void showDatePickerDialog(){
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        // The STT engine will work to recognize Italian.
        // This is mostly out of laziness, just to prove the concept
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "it-IT");
        intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                getResources().getString(R.string.activity_refuel_insertion_tts_text)
        );
        try {
            startActivityForResult(intent, STT_REQUEST_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(
                getApplicationContext(),
                getResources().getString(R.string.activity_refuel_insertion_tts_error),
                Toast.LENGTH_SHORT
            ).show();
        }
    }

    // Match expressions like "(Oggi) ho fatto 10 euro (e 50) di benzina a 1 euro (e 20) al litro"
    private void parseSTT( String stt ) throws Exception{

        //stt is the best matching stt
        ArrayDeque<String> userSpeech = new ArrayDeque<>();
        StringTokenizer tokenizer = new StringTokenizer(stt, " ");
        while (tokenizer.hasMoreTokens())
            userSpeech.add(tokenizer.nextToken());

        // Check for the first item

        if (userSpeech.peek().equals("oggi")) {
            userSpeech.pop();
            this.switchToday.setChecked(true);
        }
        else if (userSpeech.peek().equals("ieri")) {
            userSpeech.pop();
            this.switchToday.setChecked(false);
            this.editDate.setText(CalendarUtils.yesterday());
        }


        // Go on until there is nothing left on the stack
        String s;
        while( !userSpeech.isEmpty() ){
            s = userSpeech.pop();

            // Match price expressions
            if (s.matches("\\d+") || s.equals("un")){
                // Saving the integer and fractional part of the cost
                float intPart;
                float fractPart = 0.0f;

                // STT engine quirk: "un"/"uno" is parsed as a word rather than a number
                if( s.equals("un") || s.equals("uno") ) intPart = 1.0f;
                else intPart = Float.parseFloat(s);

                // Discard "euro" if it's there
                if (userSpeech.peek().equals("euro"))
                    userSpeech.pop();

                // Now, look for a fractional cost, if there's still something in the deque
                if (!userSpeech.isEmpty()) {
                    s = userSpeech.peek();
                    if (s.equals("e")) {
                        // Discard "e"
                        userSpeech.pop();

                        //Match the fractional amount
                        s = userSpeech.pop();
                        fractPart = Float.parseFloat("0." + s);
                    }
                }

                // if this.editAmount is not empty, fill this.editPpl
                if (this.editAmount.getText().toString().isEmpty())
                    this.editAmount.setText((intPart + fractPart) + "");
                else
                    this.editPpl.setText((intPart + fractPart) + "");
            }
        }
    }

    private void displayToast(int resources){
        Toast.makeText(getApplicationContext(), getResources().getString(resources),
                Toast.LENGTH_LONG).show();
    }

    private void displayToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

//==================================================================================================
//  INIT METHODS
//==================================================================================================
    private void initSpinnerVehicleAssociated(){
        ArrayList<String> list = new ArrayList<>();
        if( new DAOVehicle().countObject() != 0 ) {
            list.add(getResources().getString(R.string.frag_view_costs_no_vehicle_selected));
            vehicleList = new DAOVehicle().getAll();
            for( Vehicle i : vehicleList ){
                list.add(i.getName());
            }
        }else {
            list.add(getResources().getString(R.string.frag_main_no_vehicle_inserted));
            // TODO disable the form
        }
        CustomArrayAdapter<CharSequence> spinnerAdapter = new CustomArrayAdapter<>(
            getApplicationContext(), list.toArray(new CharSequence[list.size()])
        );

        this.spinnerVehicleAssociated = (Spinner)findViewById(R.id.refuel_insertion_vehicle_associated);
        this.spinnerVehicleAssociated.setAdapter(spinnerAdapter);
        Preferences p = Preferences.getInstance();
        if(p.contains("last_vehicle_used")&& spinnerAdapter.getCount() != 1){
            this.spinnerVehicleAssociated.setSelection(p.pullInteger("last_vehicle_used") );
        }
    }

    private void initKm(){
        this.editKM = (EditText)findViewById(R.id.refuel_insertion_km);
    }

    private void initEditTextCurrentPlace(){
        editCurrentPlace = (EditText)findViewById(R.id.refuel_insertion_place_edit);
    }

    private void initSwitchGetCurrentPlace(){
        this.switchGetCurrentPlace = (Switch) findViewById(R.id.refuel_insertion_switch_current_place);
        this.switchGetCurrentPlace.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
              @Override
              public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                  if (b) doBind();
                  else doUnBind();
                  editCurrentPlace.setEnabled(!b);
              }
          }
        );
    }

    private void initEditTextAmount(){
        this.editAmount = (EditText)findViewById(R.id.refuel_insertion_amount);
    }

    private void initEditTextPpl(){
        this.editPpl = (EditText)findViewById(R.id.refuel_insertion_ppl);
    }

    private void initSwitchToday(){
        this.switchToday = (Switch)findViewById(R.id.refuel_insertion_switch_current_date);
        this.switchToday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                EditText edt = (EditText) findViewById(R.id.refuel_insertion_data_edit);
                if( b ){
                    edt.setText(CalendarUtils.today());
                } else {
                    edt.setText("");
                }
            }
        });
    }

    private void initEditTextDate(){
        this.editDate = (EditText)findViewById(R.id.refuel_insertion_data_edit);
        this.editDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) showDatePickerDialog();
            }
        });
    }

    private void initEditTextNotes(){
        this.editNotes = (EditText)findViewById(R.id.refuel_insertion_notes);
    }

    private void populateWithRefuelToUpdate(){
        this.spinnerVehicleAssociated.setSelection(this.vehicleList.indexOf(this.refuelToUpdate.getVehicle()) + 1);
        if( this.refuelToUpdate.getPlace() != null )
            editCurrentPlace.setText(this.refuelToUpdate.getPlace().getAddress());
        this.editKM.setText( this.refuelToUpdate.getKm().toString() );
        this.editAmount.setText( this.refuelToUpdate.getAmount().toString() );
        this.editPpl.setText( this.refuelToUpdate.getPricePerLiter().toString() );
        this.editDate.setText(DateUtils.getStringFromDate( this.refuelToUpdate.getDate(), "dd/MM/yyyy") );
        if( this.refuelToUpdate.getNotes() != null || !this.refuelToUpdate.getNotes().isEmpty() )
            this.editNotes.setText(this.refuelToUpdate.getNotes());
    }

//==================================================================================================
//  GPS SERVICE METHODS
//==================================================================================================
    private void doBind(){
        this.mMessenger = new Messenger(new ServiceHandler());
        Intent gps = new Intent( getApplicationContext(), GPSService.class );
        getApplicationContext().bindService(gps, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void doUnBind(){
       if( this.mService != null ){
           Message msg = Message.obtain(null, GPSService.Protocol.REQUEST_UNBIND);
           msg.replyTo = mMessenger;
           try {
               mService.send(msg);
           } catch (RemoteException e) {
               Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_LONG).show();
           }
           getApplicationContext().unbindService(mConnection);
       }
    }

//==================================================================================================
//  OVERRIDE
//==================================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refuel_insertion);

        this.initSpinnerVehicleAssociated();
        this.initKm();
        this.initEditTextCurrentPlace();
        this.initSwitchGetCurrentPlace();
        this.initEditTextAmount();
        this.initEditTextPpl();
        this.initEditTextDate();
        this.initSwitchToday();
        this.initEditTextNotes();
        Bundle args = getIntent().getExtras();
        if( args != null && args.containsKey(UPDATE_REFUEL) ){
            Long id = (Long)args.get(UPDATE_REFUEL);
            this.refuelToUpdate = (Refuel) new DAOCost().get(id);
            this.populateWithRefuelToUpdate();
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(this.switchGetCurrentPlace.isChecked()) {
            doUnBind();
        }
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        if(this.switchGetCurrentPlace.isChecked())
            doBind();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode == Activity.RESULT_OK ){
            switch( requestCode ){
                case STT_REQUEST_CODE:{
                    if( data != null ) {
                        ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        try {
                            parseSTT(res.get(0));
                        } catch (Exception e) {
                            Toast.makeText(
                                    this.getApplicationContext(),
                                    this.getResources().getString(R.string.activity_refuel_insertion_tts_notrecognized),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                }
            }
        }//Result code for various error.
        else if( resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
            displayToast("Audio Error");
        }else if(resultCode == RecognizerIntent.RESULT_CLIENT_ERROR){
            displayToast("Client Error");
        }else if(resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
            displayToast("Network Error");
        }else if(resultCode == RecognizerIntent.RESULT_NO_MATCH){
            displayToast("No Match");
        }else if(resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
            displayToast("Server Error");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_done_and_tts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch( id ){
            case R.id.done:{
                if( this.refuelToUpdate != null ){

                    return true;
                }else {
                    if (checkFields() && save()) {
                        Toast.makeText(
                                getApplicationContext(),
                                getResources().getString(R.string.activity_refuel_insertion_refuel_save_success),
                                Toast.LENGTH_SHORT
                        ).show();
                        Preferences.getInstance().put("last_vehicle_used", this.spinnerVehicleAssociated.getSelectedItemPosition());
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                    return true;
                }
            }
            case R.id.tts:{
                promptSpeechInput();
                return true;
            }
            default: return super.onOptionsItemSelected(item);
        }
    }

//==================================================================================================
//  INNER CLASS
//==================================================================================================
    static class CustomArrayAdapter<T> extends ArrayAdapter<T> {
        public CustomArrayAdapter(Context ctx, T [] objects){
            super(ctx, R.layout.spinner_item, objects);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent){
            return super.getView(position, convertView, parent);
        }
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            String format = "%d/%d/%d";
            String date = String.format(format, day, month+1, year);
            EditText edt = (EditText) getActivity().findViewById(R.id.refuel_insertion_data_edit);
            edt.setText(date);
        }
    }

    private static class ServiceHandler extends Handler {
        @Override
        public void handleMessage( Message msg ){
            switch (msg.what){
                case GPSService.Protocol.SEND_LOCATION:{
                    Bundle receivedBundle = msg.getData();
                    String address = receivedBundle.getString(GPSService.Protocol.RETRIEVED_ADDRESS);
                    locationFromGPS = receivedBundle.getParcelable(GPSService.Protocol.RETRIEVED_LOCATION);
                    editCurrentPlace.setText(address);
                    break;
                }
                default: super.handleMessage(msg);
            }
        }
    }

    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            Message msg = Message.obtain(null, GPSService.Protocol.REQUEST_BIND);
            msg.replyTo = mMessenger;
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                Toast.makeText(
                    RefuelInsertion.this.getApplicationContext(),
                    e.getMessage(), Toast.LENGTH_LONG
                ).show();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name){ mService = null; }
    }
}
