package com.example.my_application;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface VisitDao {
    @Insert
    void insertVisit(VisitEntity visit);

    @Query("SELECT COUNT(*) FROM visits WHERE patientId = :patientId")
    int getVisitCountForPatient(int patientId);

    @Query("SELECT * FROM visits WHERE patientId = :patientId ORDER BY date DESC")
    List<VisitEntity> getVisitsForPatient(int patientId);
}
