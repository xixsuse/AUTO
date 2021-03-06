package it.science.unitn.lpsmt.auto.controller;

import java.util.List;

import it.science.unitn.lpsmt.auto.model.Place;

/**
 * TODO add doc
 */
public interface PlaceDAO {
    /**
     * TODO add doc
     */
    void close();

    /**
     * TODO add doc
     * @param p
     * @return
     */
    Long save( Place p );

    /**
     * TODO add doc
     * @param id
     * @return
     */
    Place get( Long id );

    /**
     * TODO add doc
     * @param id
     * @return
     */
    boolean exists( Long id );

    /**
     * TODO add doc
     * @param p
     */
    void delete( Place p );

    /**
     * TODO add doc
     * @param id
     */
    void delete( Long id );

    /**
     * TODO add doc
     */
    void deleteAll();

    /**
     * TODO add doc
     * @return
     */
    List<Place> getAll();

    /**
     * TODO add doc
     * @return
     */
    int countObject();
}
