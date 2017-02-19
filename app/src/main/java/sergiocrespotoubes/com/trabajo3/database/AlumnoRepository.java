package sergiocrespotoubes.com.trabajo3.database;

import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;
import android.database.sqlite.SQLiteDatabase;
import java.util.List;

public class AlumnoRepository{

	public AlumnoRepository(){}

	private static Alumno cursorToResult(final Cursor cursor) {

		Alumno alumno = new Alumno();

		alumno.setId(cursor.getLong(0));
		alumno.setNombre(cursor.getString(1));
		alumno.setDni(cursor.getString(2));
		alumno.setNota(cursor.getDouble(3));
		alumno.setChico(cursor.getInt(4));

		return alumno;
	}

	public static long insert(SQLiteDatabase db, Alumno alumno) {

		ContentValues values = new ContentValues();
		values.put("NOMBRE", alumno.getNombre());
		values.put("DNI", alumno.getDni());
		values.put("NOTA", alumno.getNota());
		values.put("CHICO", alumno.getChico());

		return db.insert("Alumno", null, values);
	}

	public static List<Alumno> getAll(SQLiteDatabase db) {

		Alumno item = null;
		List<Alumno>list = new ArrayList<>();

		String selectQuery =  "SELECT * FROM Alumno";

		Cursor cursor = db.rawQuery(selectQuery, null );

		while(cursor.moveToNext()){
			item = cursorToResult(cursor);
			list.add(item);
		}
		cursor.close();

		return list;
	}

	public static Alumno getById(SQLiteDatabase db, long id) {

		Alumno item = null;
		String selectQuery =  "SELECT * FROM Alumno";

		Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(id)} );

		if(cursor.moveToNext()){
			item = cursorToResult(cursor);
		}
		cursor.close();

		return item;
	}

	public static void update(SQLiteDatabase db, Alumno alumno) {

		ContentValues values = new ContentValues();
		values.put("NOMBRE", alumno.getNombre());
		values.put("DNI", alumno.getDni());
		values.put("NOTA", alumno.getNota());
		values.put("CHICO", alumno.getChico());

		db.update("Alumno", values, "ID + = ?", new String[] {String.valueOf(alumno.getId())});
	}

	public static void delete(SQLiteDatabase db, Alumno alumno) {
		db.delete("Alumno", "ID = ?", new String[] {String.valueOf(alumno.getId())});
	}

	public static void delete(SQLiteDatabase db, int id) {
		db.delete("Alumno", "ID =  ?", new String[] {String.valueOf(id)});
	}

}