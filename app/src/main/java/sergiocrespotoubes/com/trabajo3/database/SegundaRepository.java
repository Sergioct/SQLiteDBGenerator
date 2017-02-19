package sergiocrespotoubes.com.trabajo3.database;

import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;
import android.database.sqlite.SQLiteDatabase;
import java.util.List;

public class SegundaRepository{

	public SegundaRepository(){}

	private static Segunda cursorToResult(final Cursor cursor) {

		Segunda segunda = new Segunda();

		segunda.setId(cursor.getLong(0));
		segunda.setCol_fecha(cursor.getLong(1));

		return segunda;
	}

	public static long insert(SQLiteDatabase db, Segunda segunda) {

		ContentValues values = new ContentValues();
		values.put("COL_FECHA", segunda.getCol_fecha());

		return db.insert("Segunda", null, values);
	}

	public static List<Segunda> getAll(SQLiteDatabase db) {

		Segunda item = null;
		List<Segunda>list = new ArrayList<>();

		String selectQuery =  "SELECT * FROM Segunda";

		Cursor cursor = db.rawQuery(selectQuery, null );

		while(cursor.moveToNext()){
			item = cursorToResult(cursor);
			list.add(item);
		}
		cursor.close();

		return list;
	}

	public static Segunda getById(SQLiteDatabase db, long id) {

		Segunda item = null;
		String selectQuery =  "SELECT * FROM Segunda";

		Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(id)} );

		if(cursor.moveToNext()){
			item = cursorToResult(cursor);
		}
		cursor.close();

		return item;
	}

	public static void update(SQLiteDatabase db, Segunda segunda) {

		ContentValues values = new ContentValues();
		values.put("COL_FECHA", segunda.getCol_fecha());

		db.update("Segunda", values, "ID + = ?", new String[] {String.valueOf(segunda.getId())});
	}

	public static void delete(SQLiteDatabase db, Segunda segunda) {
		db.delete("Segunda", "ID = ?", new String[] {String.valueOf(segunda.getId())});
	}

	public static void delete(SQLiteDatabase db, int id) {
		db.delete("Segunda", "ID =  ?", new String[] {String.valueOf(id)});
	}

}