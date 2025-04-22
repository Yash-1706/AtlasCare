package com.example.my_application;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "HospitalDB";
    private static final int DATABASE_VERSION = 5; // Incrementing again to force upgrade

    private static final String TABLE_PATIENTS = "patients";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_KNOWN_DIAGNOSIS = "knownDiagnosis";
    private static final String COLUMN_CURRENT_DIAGNOSIS = "currentDiagnosis";
    private static final String COLUMN_DATE = "visit_date";
    private static final String COLUMN_TIME = "visit_time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // Force a check/upgrade of the database at construction time
        SQLiteDatabase db = this.getWritableDatabase();
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the patients table with ALL required columns
        String createTable = "CREATE TABLE " + TABLE_PATIENTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_KNOWN_DIAGNOSIS + " TEXT, " +
                COLUMN_CURRENT_DIAGNOSIS + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_TIME + " TEXT)";
        db.execSQL(createTable);
        Log.d("DatabaseHelper", "Created database table with all required columns");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "Upgrading database from version " + oldVersion + " to " + newVersion);
        
        // For schema issues like these, it's safest to recreate the table
        // This will delete existing data, but fixes the schema issues
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATIENTS);
        onCreate(db);
        Log.d("DatabaseHelper", "Database table recreated successfully");
    }

    // Add a new patient
    public boolean addPatient(PatientModel patient) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            
            // Log what we're trying to insert for debugging
            Log.d("DatabaseHelper", "Adding patient: name=" + patient.getName() + 
                  ", knownDiagnosis=" + patient.getKnownDiagnosis() + 
                  ", currentDiagnosis=" + patient.getCurrentDiagnosis() + 
                  ", date=" + patient.getDate() + 
                  ", time=" + patient.getTime());
            
            values.put(COLUMN_NAME, patient.getName());
            values.put(COLUMN_KNOWN_DIAGNOSIS, patient.getKnownDiagnosis());
            values.put(COLUMN_CURRENT_DIAGNOSIS, patient.getCurrentDiagnosis());
            values.put(COLUMN_DATE, patient.getDate());
            values.put(COLUMN_TIME, patient.getTime());

            long result = db.insert(TABLE_PATIENTS, null, values);
            if (result == -1) {
                Log.e("DatabaseHelper", "Failed to insert patient: " + values.toString());
            } else {
                Log.d("DatabaseHelper", "Patient inserted successfully with ID: " + result);
            }

            db.close();
            return result != -1;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Exception adding patient: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Get all patients
    public List<PatientModel> getAllPatients() {
        List<PatientModel> patients = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PATIENTS, null);

        if (cursor.moveToFirst()) {
            do {
                PatientModel patient = new PatientModel();
                int idIndex = cursor.getColumnIndex(COLUMN_ID);
                int nameIndex = cursor.getColumnIndex(COLUMN_NAME);
                int knownDiagnosisIndex = cursor.getColumnIndex(COLUMN_KNOWN_DIAGNOSIS);
                int currentDiagnosisIndex = cursor.getColumnIndex(COLUMN_CURRENT_DIAGNOSIS);
                int dateIndex = cursor.getColumnIndex(COLUMN_DATE);
                int timeIndex = cursor.getColumnIndex(COLUMN_TIME);

                if (idIndex != -1) patient.setId(cursor.getInt(idIndex));
                if (nameIndex != -1) patient.setName(cursor.getString(nameIndex));
                if (knownDiagnosisIndex != -1) patient.setKnownDiagnosis(cursor.getString(knownDiagnosisIndex));
                if (currentDiagnosisIndex != -1) patient.setCurrentDiagnosis(cursor.getString(currentDiagnosisIndex));
                if (dateIndex != -1) patient.setDate(cursor.getString(dateIndex));
                if (timeIndex != -1) patient.setTime(cursor.getString(timeIndex));

                patients.add(patient);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return patients;
    }
    
    // Clear all data from the database
    public boolean clearAllData() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            int deletedRows = db.delete(TABLE_PATIENTS, null, null);
            Log.d("DatabaseHelper", "Deleted " + deletedRows + " rows from " + TABLE_PATIENTS);
            db.close();
            return true;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error clearing database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Get count of today's patients
    public int getPatientCountForToday() {
        SQLiteDatabase db = this.getReadableDatabase();
        String todayDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PATIENTS + " WHERE " + COLUMN_DATE + " = ?", new String[]{todayDate});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return count;
    }

    // Get count of this month's patients
    public int getPatientCountForMonth() {
        SQLiteDatabase db = this.getReadableDatabase();
        String monthPattern = new SimpleDateFormat("MM/yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PATIENTS + " WHERE " + COLUMN_DATE + " LIKE ?", new String[]{"%/" + monthPattern});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return count;
    }

    // Get count for a specific date
    public int getPatientCountForDate(String selectedDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PATIENTS + " WHERE " + COLUMN_DATE + " = ?", new String[]{selectedDate});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return count;
    }
}
