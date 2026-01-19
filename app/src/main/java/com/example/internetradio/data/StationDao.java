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

    @Query("SELECT * FROM stations ORDER BY name ASC")
    LiveData<List<RadioStation>> getAllStations();

    @Delete
    void delete(RadioStation station);

    @Update
    void update(RadioStation station);

    @Query("SELECT * FROM stations WHERE stationUuid = :uuid")
    LiveData<RadioStation> getStationByUuid(String uuid);

    @Query("SELECT * FROM stations WHERE is_favorite = 1 LIMIT 10")
    LiveData<List<RadioStation>> getFavoriteStations();

    @Query("UPDATE stations SET espIndex = -1")
    void resetAllEspIndices();

    @Query("SELECT * FROM stations")
    List<RadioStation> getAllStationsSync();

    @Query("SELECT * FROM stations WHERE is_favorite = 1")
    List<RadioStation> getFavoriteStationsSync();

    @Query("UPDATE stations SET is_favorite = 0, espIndex = -1")
    void clearAllFavorites();
}