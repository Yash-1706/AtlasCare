package com.example.my_application;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {PatientEntity.class, VisitEntity.class}, version = 4)
@TypeConverters({StringListConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    public abstract PatientDao patientDao();
    public abstract VisitDao visitDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "patient_db")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries() // For demo only; use async in production
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
