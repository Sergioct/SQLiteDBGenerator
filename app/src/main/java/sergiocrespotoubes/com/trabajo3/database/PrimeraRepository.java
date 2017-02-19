package sergiocrespotoubes.com.trabajo3.database;

import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;
import android.database.sqlite.SQLiteDatabase;
import java.util.List;

public class PrimeraRepository{

	public PrimeraRepository(){}

	private static Primera cursorToResult(final Cursor cursor) {

		Primera primera = new Primera();

		primera.setId(cursor.getLong(0));
		primera.setCol_primaria(cursor.getString(1));
		primera.setCol_unica(cursor.getString(2));
		primera.setCol_entero(cursor.getInt(3));

		return primera;
	}

	public static long insert(SQLiteDatabase db, Primera primera) {

		ContentValues values = new ContentValues();
		values.put("COL_PRIMARIA", primera.getCol_primaria());
		values.put("COL_UNICA", primera.getCol_unica());
		values.put("COL_ENTERO", primera.getCol_entero());

		return db.insert("Primera", null, values);
	}

	public static List<Primera> getAll(SQLiteDatabase db) {

		Primera item = null;
		List<Primera>list = new ArrayList<>();

		String selectQuery =  "SELECT * FROM Primera";

		Cursor cursor = db.rawQuery(selectQuery, null );

		while(cursor.moveToNext()){
			item = cursorToResult(cursor);
			list.add(item);
		}
		cursor.close();

		return list;
	}

	public static Primera getById(SQLiteDatabase db, long id) {

		Primera item = null;
		String selectQuery =  "SELECT * FROM Primera WHERE ID = ?";

		Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(id)} );

		if(cursor.moveToNext()){
			item = cursorToResult(cursor);
		}
		cursor.close();

		return item;
	}

	public static void update(SQLiteDatabase db, Primera primera) {

		ContentValues values = new ContentValues();
		values.put("COL_PRIMARIA", primera.getCol_primaria());
		values.put("COL_UNICA", primera.getCol_unica());
		values.put("COL_ENTERO", primera.getCol_entero());

		db.update("Primera", values, "ID = ?", new String[] {String.valueOf(primera.getId())});
	}

	public static void delete(SQLiteDatabase db, Primera primera) {
		db.delete("Primera", "ID = ?", new String[] {String.valueOf(primera.getId())});
	}

	public static void delete(SQLiteDatabase db, long id) {
		db.delete("Primera", "ID =  ?", new String[] {String.valueOf(id)});
	}

}