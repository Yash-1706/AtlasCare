package com.example.my_application;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.TypeConverters;
import java.util.List;

@Entity(tableName = "visits")
public class VisitEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(index = true)
    public int patientId;
    public String date;
    public String notes;
    public String time;

    @TypeConverters(StringListConverter.class)
    public List<String> imageUrls;

    @TypeConverters(StringListConverter.class)
    public List<String> prescriptionImageUrls;
}
