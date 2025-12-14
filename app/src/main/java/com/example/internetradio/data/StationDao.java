package com.example.internetradio.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
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
}