package it.science.unitn.lpsmt.auto.model;

import junit.framework.TestCase;

import it.science.unitn.lpsmt.auto.model.util.Const;

import static it.science.unitn.lpsmt.auto.Generator.getRefuelInstance;
import static it.science.unitn.lpsmt.auto.Generator.getVehicleInstance;

/**
 * TODO add doc
 */
public class RefuelTest extends TestCase{

    //TODO add description
    public void testRefuelCreation(){
        Vehicle v = getVehicleInstance();
        Refuel r = getRefuelInstance(v);

        try{
            r.setId(null);
            boolean cond = r.getId().equals(Const.NO_DB_ID_SET);
            assertTrue("passing null to setId() will be set to NO_DB_ID_SET.", cond);
        }catch (IllegalArgumentException ex){
            fail("Expecting setId(null) will set id to NO_DB_ID_SET.");
        }

        try{
            r.setId(Const.NO_DB_ID_SET - 1);
            fail("I can not set a id less then Const.NO_DB_ID_SET.");
        }catch (IllegalArgumentException ex){}

        try{
            r.setAmount(null);
            fail("I can not set a null amount of Refuel.");
        }catch (IllegalArgumentException ex){}

        try{
            r.setAmount(-1.0f);
            fail("I can not set an amount less then zero of Refuel.");
        }catch (IllegalArgumentException ex){}

        try{
            r.setNotes(null);
            boolean c = r.getNotes().isEmpty();
            assertTrue("passing null to setNotes() will be set to empty string.", c);

            r.setNotes("");
            c = r.getNotes().isEmpty();
            assertTrue("passing empty string to setNotes() will be set to empty string.", c);
        }catch (IllegalArgumentException ex){
            fail("I can set a null or empty string notes of Refuel.");
        }

        try{
            r.setPricePerLiter(null);
            fail("I can not set a null price per liter.");
        }catch (IllegalArgumentException ex){}

        try{
            r.setPricePerLiter(-1f);
            fail("I can not set a price per liter less then zero.");
        }catch (IllegalArgumentException ex){}

        try{
            r.setDate(null);
            fail("I can not set a null date.");
        }catch (IllegalArgumentException ex){}

        try{
            r.setKm(null);
            fail("I can not se a null km.");
        }catch (IllegalArgumentException ex){}

        try{
            r.setKm(-1);
            fail("I can not se a km value less then zero.");
        }catch (IllegalArgumentException ex){}
    }
}
