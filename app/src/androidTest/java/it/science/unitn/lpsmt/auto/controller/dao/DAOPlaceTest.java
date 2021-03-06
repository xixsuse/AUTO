package it.science.unitn.lpsmt.auto.controller.dao;

import android.test.AndroidTestCase;

import it.science.unitn.lpsmt.auto.controller.PlaceDAO;
import it.science.unitn.lpsmt.auto.model.Place;
import it.science.unitn.lpsmt.auto.model.util.Const;

import static it.science.unitn.lpsmt.auto.Generator.*;

/**
 * TODO add doc
 */
public class DAOPlaceTest extends AndroidTestCase{
    private PlaceDAO daoPlace;

    @Override
    public void setUp() throws Exception{
        super.setUp();
        // >>>>> NB <<<<< remember to uninstall the menu_action_delete_and_modify application
        // from the device before run this menu_action_delete_and_modify
        this.daoPlace = new DAOPlace(getContext());
    }

    @Override
    public void tearDown() throws Exception{
        this.daoPlace.deleteAll();
        this.daoPlace.close();
        super.tearDown();
    }

    public void testIODB(){
        Place toStore = getPlaceInstance();

        // testing the storing procedure
        Long idFromDB = this.daoPlace.save(toStore);
        assertTrue(
            "Id from DB must be different from NO_DB_ID_SET",
            !idFromDB.equals(Const.NO_DB_ID_SET)
        );

        // menu_action_delete_and_modify if there is at least one place stored
        int placeStored = this.daoPlace.countObject();
        assertTrue("There must be at least one Place stored.", placeStored != 0);

        // check if there is the same object into db.
        Place fromDB = this.daoPlace.get(toStore.getId());
        assertTrue("Place from DB must be not null.", fromDB != null);
        assertTrue("Actual and stored Place must be the same.", toStore.equals(fromDB));

        // check if not save the same object
        this.daoPlace.save(toStore);
        int newPlacesStored = this.daoPlace.countObject();
        assertTrue("I can not save the same object twice.", placeStored == newPlacesStored);

        // menu_action_delete_and_modify the deleting procedure
        this.daoPlace.delete(fromDB);

        // menu_action_delete_and_modify if there isn't the place deleted before
        Place nullPlace = this.daoPlace.get(toStore.getId());
        assertTrue("Try to get a deleted place from DB, will returns null.", nullPlace == null);
    }
}
