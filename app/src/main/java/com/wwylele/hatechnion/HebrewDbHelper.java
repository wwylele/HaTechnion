package com.wwylele.hatechnion;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class HebrewDbHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "hebrew.db";
    private static final int DATABASE_VERSION = 1;

    public HebrewDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + DictionaryEntry.TABLE_NAME + " (" +
                DictionaryEntry._ID + " INTEGER PRIMARY KEY," +
                DictionaryEntry.COLUMN_ORIGIN + " TEXT UNIQUE NOT NULL, " +
                DictionaryEntry.COLUMN_TRANSLATION + " TEXT NOT NULL) ";
        db.execSQL(SQL_CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DictionaryEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void add(String origin, String translation) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DictionaryEntry.COLUMN_ORIGIN, origin);
        contentValues.put(DictionaryEntry.COLUMN_TRANSLATION, translation);
        SQLiteDatabase db = getWritableDatabase();
        db.insert(DictionaryEntry.TABLE_NAME, null, contentValues);
        db.close();
    }

    public String get(String origin) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(DictionaryEntry.TABLE_NAME, new String[]{DictionaryEntry.COLUMN_TRANSLATION},
                DictionaryEntry.COLUMN_ORIGIN + "==?",
                new String[]{origin}, null, null, null
        );
        if (!cursor.moveToFirst()) return null;
        int idx = cursor.getColumnIndex(DictionaryEntry.COLUMN_TRANSLATION);
        String result = cursor.getString(idx);
        db.close();
        return result;
    }

    public static final class DictionaryEntry implements BaseColumns {
        public static final String TABLE_NAME = "dictionary";

        public static final String COLUMN_ORIGIN = "origin";
        public static final String COLUMN_TRANSLATION = "translation";
    }
}
