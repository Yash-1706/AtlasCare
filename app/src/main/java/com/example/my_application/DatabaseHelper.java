package com.example.my_application;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "HospitalDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_PATIENTS = "patients";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "visit_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_PATIENTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATIENTS);
        onCreate(db);
    }

    // Method to get today's patient count
    public int getPatientCountForToday() {
        SQLiteDatabase db = this.getReadableDatabase();
        String todayDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PATIENTS + " WHERE " + COLUMN_DATE + " = ?", new String[]{todayDate});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    // Method to get this month's patient count
    public int getPatientCountForMonth() {
        SQLiteDatabase db = this.getReadableDatabase();
        String monthPattern = new SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PATIENTS + " WHERE " + COLUMN_DATE + " LIKE ?", new String[]{"%/" + monthPattern});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    // Method to get patient count for a selected date
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
