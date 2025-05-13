package com.example.my_application;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.TypeConverters;
import java.util.List;

@Entity(tableName = "patients")
public class PatientEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String knownDiagnosis;
    public String currentDiagnosis;
    public String date;
    public String time;

    @TypeConverters(StringListConverter.class)
    public List<String> imageUrls;
}
