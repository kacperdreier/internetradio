package com.example.internetradio.data; // Utwórz nowy pakiet 'data'

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "stations")
public class RadioStation {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @SerializedName("stationuuid")
    private String stationUuid;

    @SerializedName("name")
    private String name;

    @SerializedName("url_resolved")
    private String url;

    @SerializedName("country")
    private String country;

    public RadioStation(String stationUuid, String name, String url, String country) {
        this.stationUuid = stationUuid;
        this.name = name;
        this.url = url;
        this.country = country;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStationUuid() { return stationUuid; }
    public void setStationUuid(String stationUuid) { this.stationUuid = stationUuid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}