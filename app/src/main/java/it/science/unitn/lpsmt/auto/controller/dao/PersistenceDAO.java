package it.science.unitn.lpsmt.auto.controller.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import it.science.unitn.lpsmt.auto.controller.util.Const;
import it.science.unitn.lpsmt.auto.model.Cost;
import it.science.unitn.lpsmt.auto.model.Place;
import it.science.unitn.lpsmt.auto.model.Vehicle;
import it.science.unitn.lpsmt.auto.ui.MainActivity;

/**
 * TODO add doc
 */
class PersistenceDAO extends SQLiteOpenHelper {
    private static PersistenceDAO instance;

    /**
     * TODO add doc
     */
    public PersistenceDAO() {
        super(
            MainActivity.getApp().getApplicationContext(), // static reference to context
            Const.DB_NAME,                // db name
            null,                         // default cursor factory
            Const.DB_VERSION              // db version
        );
        instance = this; // necessary because public constructor
    }

    /**
     * TODO add doc
     * @param testContext
     */
    public PersistenceDAO(Context testContext){
        super(
            testContext,
            Const.DB_NAME,                // db name
            null,                         // default cursor factory
            Const.DB_VERSION              // db version
        );
        instance = this; // necessary because public constructor
    }

    /**
     * TODO add doc
     * @return
     */
    public static PersistenceDAO getInstance(){
        if(instance == null)
            instance = new PersistenceDAO();
        return instance;
    }

//==================================================================================================
//  OVERRIDE
//==================================================================================================
    @Override
    public void onCreate(SQLiteDatabase db) {
        // use the db passed as arg because the db is not already created
        // so "db" is the only valid instance. https://goo.gl/gwctTn
        db.beginTransaction();
        try {
            db.execSQL(Vehicle.SQLData.SQL_CREATE);
            db.execSQL(Place.SQLData.SQL_CREATE);
            db.execSQL(Cost.SQLData.SQL_CREATE);
            db.setTransactionSuccessful();
        }catch ( Throwable ex ){
            Log.e(PersistenceDAO.class.getSimpleName(), ex.getMessage());
        }finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // TODO here insert the drop sql statement for old table
        onCreate(sqLiteDatabase);
    }
}
