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
		primera.setCol_primaria(cursor.getString(0));
		primera.setCol_unica(cursor.getString(1));
		primera.setCol_entero(cursor.getInt(2));

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

		Primera item;
		List<Primera>list = new ArrayList<>();

		String selectQuery =  "SELECT * FROM Primera";

		Cursor cursor = db.rawQuery(selectQuery, null );

		while(cursor.moveToNext()){
			item = cursorToResult(cursor);
			list.add(item);
		}
		cursor.close();

		return list;	}

	public static List<Primera> getById(SQLiteDatabase db, long id) {

		Primera item;
		List<Primera>list = new ArrayList<>();

		String selectQuery =  "SELECT * FROM Primera";

		Cursor cursor = db.rawQuery(selectQuery, null );

		while(cursor.moveToNext()){
			item = cursorToResult(cursor);
			list.add(item);
		}
		cursor.close();

		return list;	}

	public static void update(SQLiteDatabase db, Primera primera) {

		ContentValues values = new ContentValues();
		values.put("COL_PRIMARIA", primera.getCol_primaria());
		values.put("COL_UNICA", primera.getCol_unica());
		values.put("COL_ENTERO", primera.getCol_entero());

		db.update("Primera", values, "ID + = ?", new String[] {String.valueOf(primera.getId())});
	}

	public static void delete(SQLiteDatabase db, Primera primera) {
		db.delete("Primera", "ID = primera.getId()", null);
	}

	public static void delete(SQLiteDatabase db, int id) {
		db.delete("Primera", "ID =  id", null);
	}

}