package com.example.internetradio.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {RadioStation.class}, version = 2, exportSchema = false)
public abstract class StationDatabase extends RoomDatabase {

    public abstract StationDao stationDao();

    private static volatile StationDatabase INSTANCE;

    public static StationDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (StationDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    StationDatabase.class,
                                    "station_database"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}