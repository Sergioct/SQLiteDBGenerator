package sergiocrespotoubes.com.trabajo3.database;

import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;
import android.database.sqlite.SQLiteDatabase;
import java.util.List;

public class ProductoRepository{

	public ProductoRepository(){}

	private static Producto cursorToResult(final Cursor cursor) {

		Producto producto = new Producto();

		producto.setId(cursor.getLong(0));
		producto.setNombre(cursor.getString(1));
		producto.setCoste(cursor.getDouble(2));

		return producto;
	}

	public static long insert(SQLiteDatabase db, Producto producto) {

		ContentValues values = new ContentValues();
		values.put("NOMBRE", producto.getNombre());
		values.put("COSTE", producto.getCoste());

		return db.insert("Producto", null, values);
	}

	public static List<Producto> getAll(SQLiteDatabase db) {

		Producto item = null;
		List<Producto>list = new ArrayList<>();

		String selectQuery =  "SELECT * FROM Producto";

		Cursor cursor = db.rawQuery(selectQuery, null );

		while(cursor.moveToNext()){
			item = cursorToResult(cursor);
			list.add(item);
		}
		cursor.close();

		return list;
	}

	public static Producto getById(SQLiteDatabase db, long id) {

		Producto item = null;
		String selectQuery =  "SELECT * FROM Producto WHERE ID = ?";

		Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(id)} );

		if(cursor.moveToNext()){
			item = cursorToResult(cursor);
		}
		cursor.close();

		return item;
	}

	public static void update(SQLiteDatabase db, Producto producto) {

		ContentValues values = new ContentValues();
		values.put("NOMBRE", producto.getNombre());
		values.put("COSTE", producto.getCoste());

		db.update("Producto", values, "ID = ?", new String[] {String.valueOf(producto.getId())});
	}

	public static void delete(SQLiteDatabase db, Producto producto) {
		db.delete("Producto", "ID = ?", new String[] {String.valueOf(producto.getId())});
	}

	public static void delete(SQLiteDatabase db, long id) {
		db.delete("Producto", "ID =  ?", new String[] {String.valueOf(id)});
	}

}