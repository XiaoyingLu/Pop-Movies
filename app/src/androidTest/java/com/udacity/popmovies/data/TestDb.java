/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.udacity.popmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.Arrays;
import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MovieContract.MovieEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.MostPopularMoviesEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.HighestRatedMoviesEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.FavoriteEntry.TABLE_NAME);


        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new MovieDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain all the entry tables
        assertTrue("Error: Your database was created without all the entry tables",
                tableNameHashSet.isEmpty());

        checkTableColumns(db, MovieContract.MovieEntry.TABLE_NAME, MovieContract.MovieEntry.getColumns());
        checkTableColumns(db, MovieContract.MostPopularMoviesEntry.TABLE_NAME, MovieContract.MostPopularMoviesEntry.getColumns());
        checkTableColumns(db, MovieContract.HighestRatedMoviesEntry.TABLE_NAME, MovieContract.HighestRatedMoviesEntry.getColumns());
        checkTableColumns(db, MovieContract.FavoriteEntry.TABLE_NAME, MovieContract.FavoriteEntry.getColumns());

        c.close();
        db.close();
    }

    private void checkTableColumns(SQLiteDatabase db, String tableName, String[] tableColumns) {
        Cursor c = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());
        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> columnHashSet = new HashSet<String>();
        columnHashSet.addAll(Arrays.asList(tableColumns));

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            columnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required movie
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required movie entry columns",
                columnHashSet.isEmpty());
    }

    public void testMoviesTable() {
        insertMovie();
    }

    public void testMostPopularMoviesTable() {
        // First insert the movie, and then use the movieRowId to insert
        // the movie. Make sure to cover as many failure cases as you can.
        long movieId = insertMovie();

        // First step: Get reference to writable database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what you want to insert
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieContract.COLUMN_MOVIE_ID_KEY, movieId);

        // Insert ContentValues into database and get a row ID back
        long id = db.insert(MovieContract.MostPopularMoviesEntry.TABLE_NAME, null, contentValues);

        // Query the database and receive a Cursor back
        Cursor cursor = db.query(MovieContract.MostPopularMoviesEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        // Move the cursor to a valid database row
        assertTrue("Error: No Records returned from MostPopularMoviesEntry query", cursor.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("testInsertReadDb MostPopularMoviesEntry failed to validate",
                cursor, contentValues);

        // Finally, close the cursor and database
        cursor.close();
        db.close();
    }

    public void testHighestRatedMoviesTable() {
        // First insert the movie, and then use the movieRowId to insert
        // the movie. Make sure to cover as many failure cases as you can.
        long movieId = insertMovie();

        // First step: Get reference to writable database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what you want to insert
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieContract.COLUMN_MOVIE_ID_KEY, movieId);

        // Insert ContentValues into database and get a row ID back
        long id = db.insert(MovieContract.HighestRatedMoviesEntry.TABLE_NAME, null, contentValues);

        // Query the database and receive a Cursor back
        Cursor cursor = db.query(MovieContract.HighestRatedMoviesEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        // Move the cursor to a valid database row
        assertTrue("Error: No Records returned from HighestRatedMoviesEntry query", cursor.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("testInsertReadDb HighestRatedMoviesEntry failed to validate",
                cursor, contentValues);

        // Finally, close the cursor and database
        cursor.close();
        db.close();
    }

    public void testFavoriteMoviesTable() {
        // First insert the movie, and then use the movieRowId to insert
        // the movie. Make sure to cover as many failure cases as you can.
        long movieId = insertMovie();

        // First step: Get reference to writable database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what you want to insert
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieContract.COLUMN_MOVIE_ID_KEY, movieId);

        // Insert ContentValues into database and get a row ID back
        long id = db.insert(MovieContract.FavoriteEntry.TABLE_NAME, null, contentValues);

        // Query the database and receive a Cursor back
        Cursor cursor = db.query(MovieContract.FavoriteEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        // Move the cursor to a valid database row
        assertTrue("Error: No Records returned from FavoriteEntry query", cursor.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("testInsertReadDb FavoriteEntry failed to validate",
                cursor, contentValues);

        // Finally, close the cursor and database
        cursor.close();
        db.close();
    }

    public long insertMovie() {
        // First step: Get reference to writable database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what you want to insert)
        ContentValues testValues = TestUtilities.createMovieValues();

        // Insert ContentValues into database and get a row ID back
        long movieRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(movieRowId != -1);

        // Query the database and receive a Cursor back
        Cursor cursor = db.query(MovieContract.MovieEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        // Move the cursor to a valid database row
        assertTrue("Error: No Records returned from MovieEntry query", cursor.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: MovieEntry Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned from location query",
                cursor.moveToNext());

        // Finally, close the cursor and database
        cursor.close();
        db.close();

        return movieRowId;
    }
}
