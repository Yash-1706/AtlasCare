package com.example.my_application;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;
import java.util.List;

@Dao
public interface PatientDao {
    @Insert
    void insertPatient(PatientEntity patient);

    @Query("SELECT * FROM patients ORDER BY id DESC")
    List<PatientEntity> getAllPatients();

    @Delete
    void deletePatient(PatientEntity patient);

    @Update
    void updatePatient(PatientEntity patient);

    @Query("DELETE FROM patients")
    void clearAllPatients();
}
