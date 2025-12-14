package com.example.internetradio.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface StationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RadioStation station);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RadioStation> stations);

    @Query("SELECT * FROM stations ORDER BY name ASC")
    LiveData<List<RadioStation>> getAllStations();

    @Query("DELETE FROM stations")
    void deleteAll();

    @Delete
    void delete(RadioStation station);

    @Update
    void update(RadioStation station);

    @Query("SELECT * FROM stations WHERE stationUuid = :uuid")
    LiveData<RadioStation> getStationByUuid(String uuid);

    @Query("SELECT * FROM stations WHERE is_favorite = 1 ORDER BY name ASC")
    LiveData<List<RadioStation>> getFavoriteStations();

    @Query("SELECT COUNT(stationUuid) FROM stations")
    int getStationCount();

}