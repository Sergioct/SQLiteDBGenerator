package sergiocrespotoubes.com.dbgenerator;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Sergio on 07-Jan-17.
 */
public class DBMain {

    private static DBMain ourInstance = new DBMain();

    public static DBMain getInstance() {
        return ourInstance;
    }

    private DBMain() {
    }

    public SQLiteDatabase openDatabase(Context context, String databaseName) {
        return null;
    }

    public SQLiteDatabase createDatabase(Context context, String databaseName, String metadataFile, String dataFile){

        DBHelper dbHelper = new DBHelper(context, databaseName);
        SQLiteDatabase db = DBHelper.db;
        HashMap<String, String> dbTables = null;

        //Load metadata file
        JSONObject metadata = readFile(context, metadataFile);
        if (metadata != null) {
            dbTables = createDatabaseFromJson(db, metadata);
        }

        //Load data file
        if(dbTables != null){
            JSONObject data = readFile(context, dataFile);
            if(data != null){
                loadDatabaseData(db, dbTables, data);
            }
        }

        return db;
    }

    public SQLiteDatabase createDatabase(Context context, String databaseName, String metadataFile){

        DBHelper dbHelper = new DBHelper(context, databaseName);
        SQLiteDatabase db = DBHelper.db;

        //Load metadata file
        JSONObject metadata = readFile(context, metadataFile);
        if (metadata != null) {
            createDatabaseFromJson(db, metadata);
        }

        return db;
    }

    private HashMap<String, String> createDatabaseFromJson(SQLiteDatabase db, JSONObject jsonMetadata){

        HashMap<String, String> dbTables = new HashMap<>();
        JSONArray jsonTables;
        JSONObject jsonTable;
        int numTables;
        String name;

        try {
            if(jsonMetadata != null && jsonMetadata.has("tables")){
                jsonTables = jsonMetadata.getJSONArray("tables");
                numTables = jsonTables.length();

                for (int i = 0; i < numTables; i++) {
                    jsonTable = jsonTables.getJSONObject(i);
                    name = createTable(db, jsonTable);
                    if(name != null){
                        dbTables.put(name, null);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dbTables;
    }

    private String createTable(SQLiteDatabase db, JSONObject jsonTable){

        String name = null;
        String auxName = null;
        JSONArray jsonFields;
        int numFields;
        String sql = "";
        String column = null;

        try{
            if(jsonTable.has("name")){
                auxName = jsonTable.getString("name");
                if(jsonTable.has("fields")){
                    jsonFields = jsonTable.getJSONArray("fields");
                    numFields = jsonFields.length();

                    sql = "ID INTEGER PRIMARY KEY AUTOINCREMENT ";

                    for (int i = 0; i < numFields; i++) {
                        column = createField(jsonFields.getJSONObject(i));

                        if(column != null){
                            sql += ", " + column;
                        }
                    }

                    db.execSQL("DROP TABLE IF EXISTS " + auxName + ";");
                    sql = "CREATE TABLE IF NOT EXISTS " + auxName + " (" + sql + " )";
                    db.execSQL(sql);
                }
                name = auxName;
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

        return name;
    }

    private String createField(JSONObject jsonField){

        String column = "";
        String type;

        try{
            if(jsonField.has("name") && !jsonField.getString("name").equalsIgnoreCase("id") && jsonField.has("type")){

                type = jsonField.getString("type");
                if(type.equals(DBHelper.INTEGER) || type.equals(DBHelper.TEXT) || type.equals(DBHelper.DATE) || type.equals(DBHelper.REAL)){
                    column += jsonField.getString("name") + " " + type;

                    if(jsonField.has("isunique") && jsonField.getBoolean("isunique")){
                        column += " UNIQUE ";
                    }
                }
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

        return column;
    }

    private void loadDatabaseData(SQLiteDatabase db, HashMap<String, String> dbTables, JSONObject jsonData){

        int numTables;
        JSONArray jsonTables;
        JSONObject jsonTable;

        try {
            if(jsonData.has("tables")){
                jsonTables = jsonData.getJSONArray("tables");

                numTables = jsonTables.length();

                for (int i = 0; i < numTables; i++) {
                    jsonTable = jsonTables.getJSONObject(i);
                    if(jsonTable.has("name")){
                        String name = jsonTable.getString("name");

                        if(dbTables.containsKey(name)){
                            loadDatabaseTable(db, name, jsonTable);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadDatabaseTable(SQLiteDatabase db, String nameTable, JSONObject jsonTable) {

        JSONArray jsonRows;
        int numRows;
        JSONObject jsonRow;
        JSONArray jsonFields;

        try {
            if(jsonTable.has("rows")){
                jsonRows = jsonTable.getJSONArray("rows");
                numRows = jsonRows.length();

                for (int i = 0; i < numRows; i++) {
                    jsonRow = jsonRows.getJSONObject(i);

                    if(jsonRow != null){
                        jsonFields = jsonRow.getJSONArray("fields");
                        if(jsonFields!= null && jsonFields.length() > 0){
                            loadDatabaseRow(db, nameTable, jsonFields);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadDatabaseRow(SQLiteDatabase db, String nameTable, JSONArray jsonFields) {

        JSONObject jsonField;
        int numData;
        String name;
        String type;
        String text;
        long integer;
        long date;
        double real;
        String value;
        ContentValues values = new ContentValues();

        numData = jsonFields.length();

        for (int i = 0; i < numData; i++) {
            try{
                jsonField = jsonFields.getJSONObject(i);

                if(jsonField.has("name") && jsonField.has("type") && jsonField.has("value")){
                    name = jsonField.getString("name");
                    type = jsonField.getString("type");

                    switch(type){
                        case DBHelper.TEXT:
                            text = jsonField.getString("value");
                            value = text;
                            break;
                        case DBHelper.INTEGER:
                            integer = jsonField.getLong("value");
                            value = String.valueOf(integer);
                            break;
                        case DBHelper.DATE:
                            date = jsonField.getLong("value");
                            value = String.valueOf(date);
                            break;
                        case DBHelper.REAL:
                            real = jsonField.getDouble("value");
                            value = String.valueOf(real);
                            break;
                        default:
                            value = null;
                            break;
                    }

                    if(value != null){
                        values.put(name, value);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(values.size() > 0){
            db.insert(nameTable, null, values);
        }
    }

    private static JSONObject readFile(Context context, String fileName) {

        String text = "";
        JSONObject jsonObject = null;

        try {
            StringBuilder buf=new StringBuilder();
            InputStream inputStream = null;
            String sReader;

            inputStream = context.getAssets().open(fileName);

            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            while ((sReader=in.readLine()) != null) {
                buf.append(sReader);
            }
            in.close();
            text = buf.toString();
            text = text.replaceAll("\t","");
            text = text.replaceAll("\n","");
            jsonObject = new JSONObject(text);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("test", fileName +": "+text);

        return jsonObject;
    }

    private static String readFileText(Context context, String fileName) {

        String text = "";

        try {
            StringBuilder buf=new StringBuilder();
            InputStream inputStream = null;
            String sReader;

            inputStream = context.getAssets().open(fileName);

            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            while ((sReader=in.readLine()) != null) {
                buf.append(sReader);
            }
            in.close();
            text = buf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("test", fileName +": "+text);

        return text;
    }

    /***********************************************************************************************
     *      GENERATE REPOSITORY FILES
     **********************************************************************************************/

    public static void generateJavaCrudRepository(Context context, String path, String metadataFile) {

        //Load metadata file
        JSONObject jsonMetadata = readFile(context, metadataFile);

        if (jsonMetadata != null) {
            JSONArray jsonTables;
            JSONObject jsonTable;
            int numTables;
            String name;
            JSONArray jsonFields;

            try {
                if(jsonMetadata != null && jsonMetadata.has("tables")){
                    jsonTables = jsonMetadata.getJSONArray("tables");
                    numTables = jsonTables.length();

                    for (int i = 0; i < numTables; i++) {
                        jsonTable = jsonTables.getJSONObject(i);

                        if(jsonTable.has("name") && !jsonTable.isNull("name")
                                && jsonTable.has("fields") && !jsonTable.isNull("fields") ){
                            name = jsonTable.getString("name");

                            jsonFields = jsonTable.getJSONArray("fields");

                            if(name != null && jsonFields.length() > 0){
                                name = name.toLowerCase();
                                name = name.substring(0, 1).toUpperCase() + name.substring(1);

                                /* Java classes */
                                generateJavaClass(path, name, jsonFields);
                                generateJavaTableRepository(path, name, jsonFields);

                                /* iOS classes */
                                generateIosHClass(path, name, jsonFields);
                                generateIosMClass(path, name, jsonFields);
                                generateIosHTableRepository(path, name, jsonFields);
                                generateIosMTableRepository(path, name, jsonFields);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private static void generateJavaClass(String path, String name, JSONArray jsonFields) {
        try {
            File root = new File(path);

            if (!root.exists()) {
                root.mkdirs();
            }

            File gpxfile = new File(root, name + ".java");
            FileWriter writer = new FileWriter(gpxfile);
            int numFields = jsonFields.length();
            JSONObject jsonField;
            String fieldName;
            String fieldType;
            String fieldNameUpper;

            writer.append("public class "+name+ "{\n\n");
            writer.append("\tpublic "+name+ "(){}\n\n");

            writer.append("\tprivate long id;\n\n");
            writer.append("\tpublic long getId(){return id;}\n\n");
            writer.append("\tpublic void setId(long id){this.id = id;}\n\n");

            for (int i = 0; i < numFields; i++) {
                jsonField = jsonFields.getJSONObject(i);

                if(jsonField.has("name") && !jsonField.isNull("name")
                        && jsonField.has("type") && !jsonField.isNull("type")){
                    fieldName = jsonField.getString("name");
                    fieldType = jsonField.getString("type");

                    fieldName = fieldName.toLowerCase();

                    if(!fieldName.trim().equals("") && !fieldType.trim().equals("")){
                        switch (fieldType){
                            case DBHelper.TEXT:
                                fieldType = "String";
                            break;
                            case DBHelper.INTEGER:
                                fieldType = "int";
                            break;
                            case DBHelper.DATE:
                                fieldType = "long";
                            break;
                            case DBHelper.REAL:
                                fieldType = "double";
                            break;
                        }
                    }
                    writer.append("\tprivate "+fieldType+" "+fieldName+ ";\n\n");
                    fieldNameUpper = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

                    writer.append(String.format("\tpublic %s get%s(){return %s;}\n\n",
                            fieldType,
                            fieldNameUpper,
                            fieldName));

                    writer.append(String.format("\tpublic void set%s(%s %s){this.%s = %s;}\n\n",
                            fieldNameUpper,
                            fieldType,
                            fieldName,
                            fieldName,
                            fieldName));
                }
            }
            writer.append("}");

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void generateJavaTableRepository(String path, String name, JSONArray jsonFields) {
        try {
            String className;

            className = name.toLowerCase();
            className = className.substring(0, 1).toUpperCase() + className.substring(1) + "Repository";

            File root = new File(path);

            if (!root.exists()) {
                root.mkdirs();
            }

            File gpxfile = new File(root, className + ".java");
            FileWriter writer = new FileWriter(gpxfile);
            int numFields = jsonFields.length();
            JSONObject jsonField;
            String fieldName;
            String typeName;
            List<String> lNames = new ArrayList<>();
            List<String> lTypes = new ArrayList<>();

            writer.append("import android.content.ContentValues;\n");
            writer.append("import android.database.Cursor;\n");
            writer.append("import java.util.ArrayList;\n");
            writer.append("import android.database.sqlite.SQLiteDatabase;\n");
            writer.append("import java.util.List;\n\n");

            writer.append("public class "+className+ "{\n\n");
            writer.append("\tpublic "+className+ "(){}\n\n");

            /* Save names and types fields */
            for (int i = 0; i < numFields; i++) {
                jsonField = jsonFields.getJSONObject(i);

                if(jsonField.has("name") && !jsonField.isNull("name")
                        && jsonField.has("type") && !jsonField.isNull("type")){

                    fieldName = jsonField.getString("name");
                    typeName = jsonField.getString("type");

                    if(!fieldName.equals("")){
                        lNames.add(fieldName);
                        lTypes.add(typeName);
                    }
                }
            }

            /* CREATE CURSOR TO RESULT */
            writer.append(String.format("\tprivate static %s cursorToResult(final Cursor cursor) {\n\n", name));
            writer.append(String.format("\t\t%s %s = new %s();\n\n", name, name.toLowerCase(), name));
            writer.append(String.format("\t\t%s.setId(cursor.getLong(0));\n", name.toLowerCase()));

            for (int i = 0; i < numFields; i++) {
                fieldName = lNames.get(i);
                typeName = lTypes.get(i);
                fieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

                switch (typeName){
                    case DBHelper.TEXT:
                        typeName = "getString";
                        break;
                    case DBHelper.INTEGER:
                        typeName = "getInt";
                        break;
                    case DBHelper.DATE:
                        typeName = "getLong";
                        break;
                    case DBHelper.REAL:
                        typeName = "getDouble";
                        break;
                }

                writer.append(String.format("\t\t%s.set%s(cursor.%s(%d));\n",
                    name.toLowerCase(),
                    fieldName.substring(0, 1).toUpperCase() + fieldName.toLowerCase().substring(1),
                    typeName,
                    i));
            }
            
            writer.append(String.format("\n\t\treturn %s;\n", name.toLowerCase()));
            writer.append("\t}\n\n");

            /* INSERT METHOD */
            writer.append(String.format("\tpublic static long insert(SQLiteDatabase db, %s %s) {\n\n", name, name.toLowerCase()));
            writer.append("\t\tContentValues values = new ContentValues();\n");

            for (int i = 0; i < numFields; i++) {

                fieldName = lNames.get(i);

                writer.append(String.format("\t\tvalues.put(\"%s\", %s.get%s());\n",
                        fieldName,
                        name.toLowerCase(),
                        fieldName.substring(0, 1).toUpperCase() + fieldName.toLowerCase().substring(1)));
            }

            writer.append(String.format("\n\t\treturn db.insert(\"%s\", null, values);\n",
                    name));
            writer.append("\t}\n\n");

            /* READ METHOD */
            writer.append(String.format("\tpublic static List<%s> getAll(SQLiteDatabase db) {\n\n", name));
            writer.append(String.format("\t\t%s item;\n", name));
            writer.append(String.format("\t\tList<%s>list = new ArrayList<>();\n\n", name));
            writer.append(String.format("\t\tString selectQuery =  \"SELECT * FROM %s\";\n\n", name));
            writer.append("\t\tCursor cursor = db.rawQuery(selectQuery, null );\n\n");
            writer.append("\t\twhile(cursor.moveToNext()){\n");
            writer.append("\t\t\titem = cursorToResult(cursor);\n");
            writer.append("\t\t\tlist.add(item);\n");
            writer.append("\t\t}\n");
            writer.append("\t\tcursor.close();\n\n");
            writer.append("\t\treturn list;");
            writer.append("\t}\n\n");

            writer.append(String.format("\tpublic static List<%s> getById(SQLiteDatabase db, long id) {\n\n", name));
            writer.append(String.format("\t\t%s item;\n", name));
            writer.append(String.format("\t\tList<%s>list = new ArrayList<>();\n\n", name));
            writer.append(String.format("\t\tString selectQuery =  \"SELECT * FROM %s\";\n\n", name));
            writer.append("\t\tCursor cursor = db.rawQuery(selectQuery, null );\n\n");
            writer.append("\t\twhile(cursor.moveToNext()){\n");
            writer.append("\t\t\titem = cursorToResult(cursor);\n");
            writer.append("\t\t\tlist.add(item);\n");
            writer.append("\t\t}\n");
            writer.append("\t\tcursor.close();\n\n");
            writer.append("\t\treturn list;");
            writer.append("\t}\n\n");

            /* UPDATE METHOD */
            writer.append(String.format("\tpublic static void update(SQLiteDatabase db, %s %s) {\n\n", name, name.toLowerCase()));
            writer.append("\t\tContentValues values = new ContentValues();\n");

            for (int i = 0; i < numFields; i++) {

                fieldName = lNames.get(i);

                writer.append(String.format("\t\tvalues.put(\"%s\", %s.get%s());\n",
                        fieldName,
                        name.toLowerCase(),
                        fieldName.substring(0, 1).toUpperCase() + fieldName.toLowerCase().substring(1)));
            }

            writer.append(String.format("\n\t\tdb.update(\"%s\", values, \"ID + = ?\", new String[] {String.valueOf(%s.getId())});\n",
                    name,
                    name.toLowerCase()));
            writer.append("\t}\n\n");

            /* DELETE METHOD */
            writer.append(String.format("\tpublic static void delete(SQLiteDatabase db, %s %s) {\n", name, name.toLowerCase()));
            writer.append(String.format("\t\tdb.delete(\"%s\", \"ID = %s.getId()\", null);\n", name, name.toLowerCase()));
            writer.append(String.format("\t}\n\n", className.toLowerCase()));

            writer.append(String.format("\tpublic static void delete(SQLiteDatabase db, int id) {\n", name, name.toLowerCase()));
            writer.append(String.format("\t\tdb.delete(\"%s\", \"ID =  id\", null);\n", name));
            writer.append("\t}\n\n");

            /* CLOSE */

            writer.append("}");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void generateIosHClass(String path, String name, JSONArray jsonFields) {
        try {
            File root = new File(path);

            if (!root.exists()) {
                root.mkdirs();
            }

            File gpxfile = new File(root, name + ".h");
            FileWriter writer = new FileWriter(gpxfile);
            int numFields = jsonFields.length();
            JSONObject jsonField;
            String fieldName;
            String fieldType;

            writer.append("#import <Foundation/Foundation.h>\n\n");

            /* Interface */
            writer.append(String.format("@interface %s : NSObject{\n", name));
            /*
            for (int i = 0; i < numFields; i++) {
                jsonField = jsonFields.getJSONObject(i);

                if(jsonField.has("name") && !jsonField.isNull("name")
                        && jsonField.has("type") && !jsonField.isNull("type")){
                    fieldName = jsonField.getString("name");
                    fieldType = jsonField.getString("type");

                    fieldName = fieldName.toLowerCase();

                    if(!fieldName.trim().equals("") && !fieldType.trim().equals("")){
                        switch (fieldType){
                            case DBHelper.TEXT:
                                fieldType = "NSString *";
                                break;
                            case DBHelper.INTEGER:
                                fieldType = "NSInteger";
                                break;
                            case DBHelper.DATE:
                                fieldType = "@property (nonatomic) long";
                                break;
                            case DBHelper.REAL:
                                fieldType = "double";
                                break;
                        }
                    }
                    writer.append("\t"+fieldType+" "+fieldName+ ";\n");
                }
            }
            writer.append("}\n\n");*/

            /* Property */
            for (int i = 0; i < numFields; i++) {
                jsonField = jsonFields.getJSONObject(i);

                if(jsonField.has("name") && !jsonField.isNull("name")
                        && jsonField.has("type") && !jsonField.isNull("type")){
                    fieldName = jsonField.getString("name");
                    fieldType = jsonField.getString("type");

                    fieldName = fieldName.toLowerCase();

                    if(!fieldName.trim().equals("") && !fieldType.trim().equals("")){
                        switch (fieldType){
                            case DBHelper.TEXT:
                                fieldType = "@property (nonatomic) NSString *";
                                break;
                            case DBHelper.INTEGER:
                                fieldType = "@property (nonatomic) NSInteger";
                                break;
                            case DBHelper.DATE:
                                fieldType = "@property (nonatomic) long";
                                break;
                            case DBHelper.REAL:
                                fieldType = "@property (nonatomic) double";
                                break;
                        }
                    }
                    writer.append("\t"+fieldType+" "+fieldName+ ";\n");
                }
            }
            writer.append("}\n\n");

            writer.append("@end");

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void generateIosMClass(String path, String name, JSONArray jsonFields) {
        try {
            File root = new File(path);

            if (!root.exists()) {
                root.mkdirs();
            }

            File gpxfile = new File(root, name + ".h");
            FileWriter writer = new FileWriter(gpxfile);
            int numFields = jsonFields.length();
            JSONObject jsonField;
            String fieldName;
            String fieldType;

            writer.append(String.format("#import \"%s.h\"\n\n", name));
            writer.append(String.format("implementation %s\n\n", name));

            /* Synthesize */
            for (int i = 0; i < numFields; i++) {
                jsonField = jsonFields.getJSONObject(i);

                if(jsonField.has("name") && !jsonField.isNull("name")
                        && jsonField.has("type") && !jsonField.isNull("type")){
                    fieldName = jsonField.getString("name");
                    fieldName = fieldName.toLowerCase();
                    writer.append("\t@synthesize "+fieldName+ ";\n");
                }
            }
            writer.append("@end");

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void generateIosHTableRepository(String path, String name, JSONArray jsonFields) {
        try {
            String className;

            className = name.toLowerCase();
            className = className.substring(0, 1).toUpperCase() + className.substring(1) + "Repository";

            File root = new File(path);

            if (!root.exists()) {
                root.mkdirs();
            }

            File gpxfile = new File(root, className + ".h");
            FileWriter writer = new FileWriter(gpxfile);

            writer.append("#import <Foundation/Foundation.h>\n");
            writer.append(String.format("#import %s.h\n\n", name));

            writer.append(String.format("@implementation %sDAO : NSObject{\n"));
            writer.append("\tsqlite3 *bd\n");
            writer.append("}\n\n");

            writer.append("- (void) create;\n\n");
            writer.append("- (NSMutableArray *) getAll;\n\n");
            writer.append("- (NSMutableArray *) getById;\n\n");
            writer.append("- (void) update;\n\n");
            writer.append("- (void) delete;\n\n");

            writer.append("@end");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateIosMTableRepository(String path, String name, JSONArray jsonFields) {
        try {
            String className;

            className = name.toLowerCase();
            className = className.substring(0, 1).toUpperCase() + className.substring(1) + "Repository";

            File root = new File(path);

            if (!root.exists()) {
                root.mkdirs();
            }

            File gpxfile = new File(root, className + ".java");
            FileWriter writer = new FileWriter(gpxfile);
            int numFields = jsonFields.length();
            JSONObject jsonField;
            String fieldName;
            String typeName;
            List<String> lNames = new ArrayList<>();
            List<String> lTypes = new ArrayList<>();

            writer.append("#import \"VehiculoDAO.h\"\n");
            writer.append("#import \"Vehiculo.h\"\n\n");
            writer.append(String.format("@implementation %sDAO\n\n", name));

            writer.append("public class "+className+ "{\n\n");
            writer.append("\tpublic "+className+ "(){}\n\n");

            /* Save names and types fields */
            for (int i = 0; i < numFields; i++) {
                jsonField = jsonFields.getJSONObject(i);

                if(jsonField.has("name") && !jsonField.isNull("name")
                        && jsonField.has("type") && !jsonField.isNull("type")){

                    fieldName = jsonField.getString("name");
                    typeName = jsonField.getString("type");

                    if(!fieldName.equals("")){
                        lNames.add(fieldName);
                        lTypes.add(typeName);
                    }
                }
            }

            /* CREATE CURSOR TO RESULT */
            writer.append(String.format("\tprivate static %s cursorToResult(final Cursor cursor) {\n\n", name));
            writer.append(String.format("\t\t%s %s = new %s();\n\n", name, name.toLowerCase(), name));
            writer.append(String.format("\t\t%s.setId(cursor.getLong(0));\n", name.toLowerCase()));

            for (int i = 0; i < numFields; i++) {
                fieldName = lNames.get(i);
                typeName = lTypes.get(i);
                fieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

                switch (typeName){
                    case DBHelper.TEXT:
                        typeName = "getString";
                        break;
                    case DBHelper.INTEGER:
                        typeName = "getInt";
                        break;
                    case DBHelper.DATE:
                        typeName = "getLong";
                        break;
                    case DBHelper.REAL:
                        typeName = "getDouble";
                        break;
                }

                writer.append(String.format("\t\t%s.set%s(cursor.%s(%d));\n",
                        name.toLowerCase(),
                        fieldName.substring(0, 1).toUpperCase() + fieldName.toLowerCase().substring(1),
                        typeName,
                        i));
            }

            writer.append(String.format("\n\t\treturn %s;\n", name.toLowerCase()));
            writer.append("\t}\n\n");

            /* INSERT METHOD */
            writer.append(String.format("\tpublic static long insert(SQLiteDatabase db, %s %s) {\n\n", name, name.toLowerCase()));
            writer.append("\t\tContentValues values = new ContentValues();\n");

            for (int i = 0; i < numFields; i++) {

                fieldName = lNames.get(i);

                writer.append(String.format("\t\tvalues.put(\"%s\", %s.get%s());\n",
                        fieldName,
                        name.toLowerCase(),
                        fieldName.substring(0, 1).toUpperCase() + fieldName.toLowerCase().substring(1)));
            }

            writer.append(String.format("\n\t\treturn db.insert(\"%s\", null, values);\n",
                    name));
            writer.append("\t}\n\n");

            /* READ METHOD */
            writer.append(String.format("\tpublic static List<%s> getAll(SQLiteDatabase db) {\n\n", name));
            writer.append(String.format("\t\t%s item;\n", name));
            writer.append(String.format("\t\tList<%s>list = new ArrayList<>();\n\n", name));
            writer.append(String.format("\t\tString selectQuery =  \"SELECT * FROM %s\";\n\n", name));
            writer.append("\t\tCursor cursor = db.rawQuery(selectQuery, null );\n\n");
            writer.append("\t\twhile(cursor.moveToNext()){\n");
            writer.append("\t\t\titem = cursorToResult(cursor);\n");
            writer.append("\t\t\tlist.add(item);\n");
            writer.append("\t\t}\n");
            writer.append("\t\tcursor.close();\n\n");
            writer.append("\t\treturn list;");
            writer.append("\t}\n\n");

            writer.append(String.format("\tpublic static List<%s> getById(SQLiteDatabase db, long id) {\n\n", name));
            writer.append(String.format("\t\t%s item;\n", name));
            writer.append(String.format("\t\tList<%s>list = new ArrayList<>();\n\n", name));
            writer.append(String.format("\t\tString selectQuery =  \"SELECT * FROM %s\";\n\n", name));
            writer.append("\t\tCursor cursor = db.rawQuery(selectQuery, null );\n\n");
            writer.append("\t\twhile(cursor.moveToNext()){\n");
            writer.append("\t\t\titem = cursorToResult(cursor);\n");
            writer.append("\t\t\tlist.add(item);\n");
            writer.append("\t\t}\n");
            writer.append("\t\tcursor.close();\n\n");
            writer.append("\t\treturn list;");
            writer.append("\t}\n\n");

            /* UPDATE METHOD */
            writer.append(String.format("\tpublic static void update(SQLiteDatabase db, %s %s) {\n\n", name, name.toLowerCase()));
            writer.append("\t\tContentValues values = new ContentValues();\n");

            for (int i = 0; i < numFields; i++) {

                fieldName = lNames.get(i);

                writer.append(String.format("\t\tvalues.put(\"%s\", %s.get%s());\n",
                        fieldName,
                        name.toLowerCase(),
                        fieldName.substring(0, 1).toUpperCase() + fieldName.toLowerCase().substring(1)));
            }

            writer.append(String.format("\n\t\tdb.update(\"%s\", values, \"ID + = ?\", new String[] {String.valueOf(%s.getId())});\n",
                    name,
                    name.toLowerCase()));
            writer.append("\t}\n\n");

            /* DELETE METHOD */
            writer.append(String.format("\tpublic static void delete(SQLiteDatabase db, %s %s) {\n", name, name.toLowerCase()));
            writer.append(String.format("\t\tdb.delete(\"%s\", \"ID = %s.getId()\", null);\n", name, name.toLowerCase()));
            writer.append(String.format("\t}\n\n", className.toLowerCase()));

            writer.append(String.format("\tpublic static void delete(SQLiteDatabase db, int id) {\n", name, name.toLowerCase()));
            writer.append(String.format("\t\tdb.delete(\"%s\", \"ID =  id\", null);\n", name));
            writer.append("\t}\n\n");

            /* CLOSE */

            writer.append("}");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}