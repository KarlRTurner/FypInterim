/*
  Created by Karl on 26 Oct 2016.
 */
package ie.dit.dtw;

// Reference: The following code is from
//Susan McKeever's Mobile development Class

import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.SQLException;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteOpenHelper;

public class MyDBManager {
    // These are the names of the columns the table will contain
    public static final String KEY_ROWID = "_id";
    public static final String KEY_PHOTONAME = "Photoname";
    public static final String KEY_LATITUDE = "Latitude";
    public static final String KEY_LONGITUDE = "Longitude";
    public static final String KEY_PHOTODATE = "Photo_date";

    private static final String DATABASE_NAME = "Database";
    private static final String DATABASE_TABLE = "Photo";
    private static final int DATABASE_VERSION = 1;
    // This is the string containing the SQL database create statement
    private static final String DATABASE_CREATE = "create table " +
            DATABASE_TABLE + " (_id integer primary key autoincrement, " +
            "Photoname text not null, " +
            "Latitude real not null, " +
            "Longitude real not null," +
            "Photo_date);";

    private final Context context;

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    // constructor for your class
    public MyDBManager(Context ctx) {
// Context is a way that Android transfers info about Activities and apps.
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    // This is the helper class that will create the dB if it doesn’t exist and
    //upgrades it if the structure has changed. It needs a constructor, an
    //onCreate() method and an onUpgrade() method

    private static class DatabaseHelper extends SQLiteOpenHelper {
        // constructor for your dB helper class. This code is standard. You’ve set
        //up the parameter values for the constructor already…database name,etc
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
// The “Database_create” string below needs to contain the SQL
            //statement needed to create the dB
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion) {

 /*If you want to change the structure of your database, e.g.
      Add a new column to a table, the code will go head..
      This method only triggers if the database version number has
            increased*/


        }
    } // end of the help class

    // from here on, include whatever methods will be used to access or change data
    //in the database
    //---opens the database--- any activity that uses the dB will need to do this
    public MyDBManager open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---closes the database--- any activity that uses the dB will need to do this
    public void close() {
        DBHelper.close();
    }


    //---insert a person into the database---
    public long insertPhoto(String name, float latitude, float longitude, String date) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_PHOTONAME, name);
        initialValues.put(KEY_LATITUDE, latitude);
        initialValues.put(KEY_LONGITUDE, longitude);
        initialValues.put(KEY_PHOTODATE, date);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    //---deletes a particular person---
    public boolean deletePhoto(long rowId) {
        // delete statement. If any rows deleted (i.e. >0), returns true
        return db.delete(DATABASE_TABLE, KEY_ROWID +
                "=" + rowId, null) > 0;
    }

    //---retrieves all the rows---
    public Cursor getAllPhotos() {
        return db.query(DATABASE_TABLE, new String[]{
                        KEY_ROWID,
                        KEY_PHOTONAME,
                        KEY_LATITUDE,
                        KEY_LONGITUDE,
                        KEY_PHOTONAME},
                null,
                null,
                null,
                null,
                null);
    }

    //---retrieves a particular row---
    public Cursor getPhoto(long rowId) throws SQLException {
        Cursor mCursor = db.query(true, DATABASE_TABLE, new String[]{
                        KEY_ROWID,
                        KEY_PHOTONAME,
                        KEY_LATITUDE,
                        KEY_LONGITUDE,
                        KEY_PHOTONAME},
                KEY_ROWID + "=" + rowId,
                null,
                null,
                null,
                null,
                null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    //---updates a person---
    public boolean updatePhotoDate(long rowId, String date) {
        ContentValues args = new ContentValues();
        args.put(KEY_PHOTONAME, date);
        return db.update(DATABASE_TABLE, args,
                KEY_ROWID + "=" + rowId, null) > 0;
    }

    // end Reference:
}