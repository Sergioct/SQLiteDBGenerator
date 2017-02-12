package sergiocrespotoubes.com.dbgenerator;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    //DATABASE TYPES
    public static final String TEXT = "TEXT";
    public static final String INTEGER = "INTEGER";
    public static final String DATE = "DATE";
    public static final String REAL = "REAL";
    public static final String UNIQUE = "UNIQUE";

    public static SQLiteDatabase db;

    public DBHelper(Context context) {
        super(context, null, null, 1);
    }

    public DBHelper(Context context, String databaseName) {
        super(context, databaseName, null, 1);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}