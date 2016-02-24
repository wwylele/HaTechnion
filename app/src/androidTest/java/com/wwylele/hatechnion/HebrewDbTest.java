package com.wwylele.hatechnion;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class HebrewDbTest extends AndroidTestCase {

    public static final String LOG_TAG = HebrewDbTest.class.getSimpleName();

    @Override
    public void setUp() {
        mContext.deleteDatabase(HebrewDbHelper.DATABASE_NAME);
    }


    public void testCreateDb() throws Throwable {

        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(HebrewDbHelper.DictionaryEntry.TABLE_NAME);

        mContext.deleteDatabase(HebrewDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new HebrewDbHelper(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );


        assertTrue("Error: Your database was created without dictionary table",
                tableNameHashSet.isEmpty());

        c = db.rawQuery("PRAGMA table_info(" + HebrewDbHelper.DictionaryEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(HebrewDbHelper.DictionaryEntry._ID);
        locationColumnHashSet.add(HebrewDbHelper.DictionaryEntry.COLUMN_ORIGIN);
        locationColumnHashSet.add(HebrewDbHelper.DictionaryEntry.COLUMN_TRANSLATION);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }


    public void testDictionaryTable() {
        HebrewDbHelper dbHelper=new HebrewDbHelper(this.mContext);
        String origin="WTF",translation="What the f***";
        dbHelper.add("1","4");
        dbHelper.add("2","5");
        dbHelper.add(origin,translation);
        dbHelper.add("3","6");
        assertEquals("translation doesn't match!",translation,dbHelper.get(origin));


    }


}
