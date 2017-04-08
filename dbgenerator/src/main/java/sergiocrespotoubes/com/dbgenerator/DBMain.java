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
        DBHelper dbHelper = new DBHelper(context, databaseName);
        return dbHelper.db;
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
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void generateIosCrudRepository(Context context, String path, String metadataFile) {

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

            /* INIT CLASS */

            writer.append("import android.content.ContentValues;\n");
            writer.append("import android.database.Cursor;\n");
            writer.append("import java.util.ArrayList;\n");
            writer.append("import android.database.sqlite.SQLiteDatabase;\n");
            writer.append("import java.util.List;\n\n");

            writer.append("public class "+className+ "{\n\n");
            writer.append("\tpublic "+className+ "(){}\n\n");

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
                    (i+1)));
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
            writer.append(String.format("\t\t%s item = null;\n", name));
            writer.append(String.format("\t\tList<%s>list = new ArrayList<>();\n\n", name));
            writer.append(String.format("\t\tString selectQuery =  \"SELECT * FROM %s\";\n\n", name));
            writer.append("\t\tCursor cursor = db.rawQuery(selectQuery, null );\n\n");
            writer.append("\t\twhile(cursor.moveToNext()){\n");
            writer.append("\t\t\titem = cursorToResult(cursor);\n");
            writer.append("\t\t\tlist.add(item);\n");
            writer.append("\t\t}\n");
            writer.append("\t\tcursor.close();\n\n");
            writer.append("\t\treturn list;\n");
            writer.append("\t}\n\n");

            writer.append(String.format("\tpublic static %s getById(SQLiteDatabase db, long id) {\n\n", name));
            writer.append(String.format("\t\t%s item = null;\n", name));
            writer.append(String.format("\t\tString selectQuery =  \"SELECT * FROM %s WHERE ID = ?\";\n\n", name));
            writer.append("\t\tCursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(id)} );\n\n");
            writer.append("\t\tif(cursor.moveToNext()){\n");
            writer.append("\t\t\titem = cursorToResult(cursor);\n");
            writer.append("\t\t}\n");
            writer.append("\t\tcursor.close();\n\n");
            writer.append("\t\treturn item;\n");
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

            writer.append(String.format("\n\t\tdb.update(\"%s\", values, \"ID = ?\", new String[] {String.valueOf(%s.getId())});\n",
                    name,
                    name.toLowerCase()));
            writer.append("\t}\n\n");

            /* DELETE METHOD */
            writer.append(String.format("\tpublic static void delete(SQLiteDatabase db, %s %s) {\n", name, name.toLowerCase()));
            writer.append(String.format("\t\tdb.delete(\"%s\", \"ID = ?\", new String[] {String.valueOf(%s.getId())});\n", name, name.toLowerCase()));
            writer.append(String.format("\t}\n\n", className.toLowerCase()));

            writer.append(String.format("\tpublic static void delete(SQLiteDatabase db, long id) {\n", name, name.toLowerCase()));
            writer.append(String.format("\t\tdb.delete(\"%s\", \"ID =  ?\", new String[] {String.valueOf(id)});\n", name));
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
            writer.append("#import \"sqlite3.h\"\n\n");

            /* Interface */
            writer.append(String.format("@interface %s : NSObject{\n", name));

            writer.append("\tNSInteger myid;\n");
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
                                fieldType = "long";
                                break;
                            case DBHelper.REAL:
                                fieldType = "double";
                                break;
                        }
                    }
                    writer.append("\t"+fieldType+" "+fieldName+ ";\n");
                }
            }
            writer.append("}\n\n");

            /* Property */
            writer.append("\t@property (nonatomic) NSInteger myid;\n");
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

            File gpxfile = new File(root, name + ".m");
            FileWriter writer = new FileWriter(gpxfile);
            int numFields = jsonFields.length();
            JSONObject jsonField;
            String fieldName;
            String fieldType;

            writer.append(String.format("#import \"%s.h\"\n\n", name));
            writer.append(String.format("@implementation %s\n\n", name));

            writer.append("\t@synthesize myid;\n");

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
            writer.append("\n@end");

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
            className = className.substring(0, 1).toUpperCase() + className.substring(1) + "DAO";

            File root = new File(path);

            if (!root.exists()) {
                root.mkdirs();
            }

            File gpxfile = new File(root, className + ".h");
            FileWriter writer = new FileWriter(gpxfile);

            writer.append("#import <Foundation/Foundation.h>\n");
            writer.append("#import <sqlite3.h>\n");
            writer.append(String.format("#import \"%s.h\"\n\n", name));

            writer.append(String.format("@interface %sDAO : NSObject{\n", name));
            writer.append("\tsqlite3 *db;\n");
            writer.append("}\n\n");

            writer.append(String.format("+ (%sDAO *) instance;\n\n", name));

            writer.append(String.format("- (void) createObject:(%s*) item;\n\n", name));
            writer.append("- (NSMutableArray *) getAll;\n\n");
            writer.append(String.format("- (%s *) getById:(NSInteger)auxid;\n\n", name));
            writer.append(String.format("- (void) updateObject:(%s*) item;\n\n", name));
            writer.append(String.format("- (void) deleteObject:(NSInteger)auxid;\n\n", name));

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
            className = className.substring(0, 1).toUpperCase() + className.substring(1) + "DAO";

            File root = new File(path);

            if (!root.exists()) {
                root.mkdirs();
            }

            File gpxfile = new File(root, className + ".m");
            FileWriter writer = new FileWriter(gpxfile);
            int numFields = jsonFields.length();
            JSONObject jsonField;
            String fieldName;
            String typeName;
            List<String> lNames = new ArrayList<>();
            List<String> lTypes = new ArrayList<>();

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

            /* INIT CLASS */

            writer.append(String.format("#import \"%sDAO.h\"\n", name));
            writer.append(String.format("#import \"%s.h\"\n\n", name));
            writer.append(String.format("@implementation %sDAO\n\n", name));

            writer.append(String.format("static %sDAO *instance;\n\n", name));

            writer.append(String.format("+ (%sDAO *) instance {\n", name));
            writer.append("\tif(instance == nil){\n");
            writer.append(String.format("\t\tinstance = [[%sDAO alloc] init];\n", name));

            writer.append("\t}\n");
            writer.append("\treturn instance;\n");
            writer.append("}\n\n");

            writer.append("- (id)init {\n");
            writer.append("\tif ((self = [super init])) {\n");

            writer.append("\t\tNSArray* paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);\n");
            writer.append("\t\tNSString* documentsDirectory = [paths lastObject];\n");
            writer.append("\t\tNSString* databasePath = [documentsDirectory stringByAppendingPathComponent:@\"database.sqlite3\"];\n");

            writer.append("\t\tif (sqlite3_open([databasePath UTF8String], &db) != SQLITE_OK) {\n");
            writer.append("\t\t\tNSLog(@\"Failed to open database!\");\n");
            writer.append("\t\t}\n");
            writer.append("\t}\n");
            writer.append("\treturn self;\n");
            writer.append("}\n\n");

            /* GET ALL */

            writer.append("- (NSMutableArray *) getAll{\n\n");
            writer.append(String.format("\tconst char *sql =  \"SELECT * FROM %s\";\n\n", name));
            writer.append("\tNSMutableArray * list = [[NSMutableArray alloc] init];\n\n");
            writer.append(String.format("\t%s *item = [[%s alloc] init];\n\n", name, name));

            writer.append("\tsqlite3_stmt *sqlStatement;\n");
            writer.append("\tif(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK)\n");
            writer.append("\t{\n");

            writer.append("\t\tif(sqlite3_step(sqlStatement) == SQLITE_ROW){\n");
            writer.append("\t\t\titem.myid = sqlite3_column_int(sqlStatement, 0);\n");
            for (int i = 0; i < numFields; i++) {
                fieldName = lNames.get(i);
                typeName = lTypes.get(i);

                switch (typeName){
                    case DBHelper.TEXT:
                        writer.append(String.format("\t\t\titem.%s = [NSString stringWithUTF8String:(char *)sqlite3_column_text(sqlStatement, %d)];\n", fieldName.toLowerCase(), (i+1)));
                        break;
                    case DBHelper.INTEGER:
                        writer.append(String.format("\t\t\titem.%s = sqlite3_column_int(sqlStatement, %d);\n", fieldName.toLowerCase(), (i+1)));
                        break;
                    case DBHelper.DATE:
                        writer.append(String.format("\t\t\titem.%s = sqlite3_column_int(sqlStatement, %d);\n", fieldName.toLowerCase(), (i+1)));
                        break;
                    case DBHelper.REAL:
                        writer.append(String.format("\t\t\titem.%s = sqlite3_column_int(sqlStatement, %d);\n", fieldName.toLowerCase(), (i+1)));
                        break;
                }
            }
            writer.append("\t\t[list addObject:item];\n");
            writer.append("\t\t}\n");
            writer.append("\t\tsqlite3_finalize(sqlStatement);\n");
            writer.append("\t}\n");
            writer.append("\treturn list;\n");
            writer.append("}\n\n");

            /* GET BY ID */
            writer.append(String.format("- (%s *) getById:(NSInteger)auxid{\n\n", name));
            writer.append(String.format("\tconst char *sql =  [[NSString stringWithFormat:@\"SELECT * FROM %s where id=%%ld\",auxid] UTF8String];\n\n", name));
            writer.append(String.format("\t%s *item = [[%s alloc] init];\n\n", name, name));

            writer.append("\tsqlite3_stmt *sqlStatement;\n");
            writer.append("\tif(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK)\n");
            writer.append("\t{\n");

            writer.append("\t\tif(sqlite3_step(sqlStatement) == SQLITE_ROW){\n");
            writer.append("\t\t\titem.myid = sqlite3_column_int(sqlStatement, 0);\n");
            for (int i = 0; i < numFields; i++) {
                fieldName = lNames.get(i);
                typeName = lTypes.get(i);

                switch (typeName){
                    case DBHelper.TEXT:
                        writer.append(String.format("\t\t\titem.%s = [NSString stringWithUTF8String:(char *)sqlite3_column_text(sqlStatement, %d)];\n", fieldName.toLowerCase(), (i+1)));
                        break;
                    case DBHelper.INTEGER:
                        writer.append(String.format("\t\t\titem.%s = sqlite3_column_int(sqlStatement, %d);\n", fieldName.toLowerCase(), (i+1)));
                        break;
                    case DBHelper.DATE:
                        writer.append(String.format("\t\t\titem.%s = sqlite3_column_int(sqlStatement, %d);\n", fieldName.toLowerCase(), (i+1)));
                        break;
                    case DBHelper.REAL:
                        writer.append(String.format("\t\t\titem.%s = sqlite3_column_int(sqlStatement, %d);\n", fieldName.toLowerCase(), (i+1)));
                        break;
                }
            }
            writer.append("\t\t}\n");
            writer.append("\t\tsqlite3_finalize(sqlStatement);\n");
            writer.append("\t}\n");
            writer.append("\treturn item;\n");
            writer.append("}\n\n");

            /* INSERT METHOD */

            writer.append(String.format("-(void)createObject:(%s *)item {\n", name));

            String sql = null;
            String questions = null;
            String fieldnames = null;

            for (int i = 0; i < numFields; i++) {
                fieldName = lNames.get(i);
                typeName = lTypes.get(i);
                if(sql != null){
                    sql += ", "+fieldName;
                    questions += ", ";
                    fieldnames += ", ";
                }else{
                    sql = fieldName;
                    questions = "";
                    fieldnames = "";
                }

                switch (typeName){
                    case DBHelper.TEXT:
                        questions += "'%@'";
                        fieldnames += "item."+fieldName.toLowerCase();
                        break;
                    case DBHelper.INTEGER:
                        questions += "'%ld'";
                        fieldnames += "(long)item."+fieldName.toLowerCase();
                        break;
                    case DBHelper.DATE:
                        questions += "'%ld'";
                        fieldnames += "(long)item."+fieldName.toLowerCase();
                        break;
                    case DBHelper.REAL:
                        questions += "'%@'";
                        fieldnames += "item."+fieldName.toLowerCase();
                        break;
                }
            }
            writer.append(String.format("\tNSString *sqlInsert = [NSString stringWithFormat:@\"Insert into %s(%s) VALUES (%s)\", %s];\n\n", name, sql, questions, fieldnames));

            writer.append("\n\tconst char *sql = [sqlInsert UTF8String];\n\n");
            writer.append("\tsqlite3_stmt *sqlStatement;\n");
            writer.append("\tif(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK){\n");
            writer.append("\t\tsqlite3_step(sqlStatement);\n");
            writer.append("\t\tsqlite3_finalize(sqlStatement);\n");
            writer.append("\t}\n");
            writer.append("}\n\n");

            /* UPDATE METHOD */
            writer.append(String.format("- (void) updateObject:(%s*)item{\n\n", name));
            String sqlUpdate = "";
            String sqlUpdateValues = "";


            for (int i = 0; i < numFields; i++) {
                fieldName = lNames.get(i);
                typeName = lTypes.get(i);

                sqlUpdate += ", item."+fieldName.toLowerCase();
                if(!sqlUpdateValues.equals("")){
                    sqlUpdateValues += ", ";
                }

                switch (typeName){
                    case DBHelper.TEXT:
                        sqlUpdateValues += (String.format("%s = '%%@'", fieldName.toLowerCase()));
                        break;
                    case DBHelper.INTEGER:
                        sqlUpdateValues += (String.format("%s = '%%ld'", fieldName.toLowerCase()));
                        break;
                    case DBHelper.DATE:
                        sqlUpdateValues += (String.format("%s = '%%ld'", fieldName.toLowerCase()));
                        break;
                    case DBHelper.REAL:
                        sqlUpdateValues += (String.format("%s = '%%ld'", fieldName.toLowerCase()));
                        break;
                }
            }
            writer.append(String.format("\n\tconst char *sql = [[NSString stringWithFormat:@\"update %s set %s where id=%%ld\" %s, item.myid] UTF8String];\n\n", name, sqlUpdateValues, sqlUpdate));

            writer.append("\tsqlite3_stmt *sqlStatement;\n\n");
            writer.append("\tif(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK){\n");
            writer.append("\t\tsqlite3_step(sqlStatement);\n");
            writer.append("\t\tsqlite3_finalize(sqlStatement);\n");
            writer.append("\t}\n\n");
            writer.append("}\n\n");

            /* DELETE METHOD */
            writer.append("- (void) deleteObject:(NSInteger)auxid{\n\n");

            writer.append(String.format("\tconst char *sql = [[NSString stringWithFormat:@\"delete from %s where ID=%%ld\",auxid] UTF8String];\n\n", name));
            writer.append("\tsqlite3_stmt *sqlStatement;\n");
            writer.append("\tif(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK){\n");
            writer.append("\t\tsqlite3_step(sqlStatement);\n");
            writer.append("\t\tsqlite3_finalize(sqlStatement);\n");
            writer.append("\t}\n");
            writer.append("}\n\n");

            /* CLOSE */
            writer.append("@end");
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void generateActivityClass(Context context, String path, String databaseName) {

        String fileName;

        fileName = databaseName.toLowerCase();
        fileName = databaseName.substring(0, 1).toUpperCase() + fileName.substring(1) + "Activity";

        File root = new File(path);

        if (!root.exists()) {
            root.mkdirs();
        }

        File gpxfile = new File(root, fileName + ".java");
        FileWriter writer = null;
        try {
            writer = new FileWriter(gpxfile);
            writer.append("import android.app.DatePickerDialog;\n");
            writer.append("import android.content.Context;\n");
            writer.append("import android.database.Cursor;\n");
            writer.append("import android.database.sqlite.SQLiteDatabase;\n");
            writer.append("import android.os.Bundle;\n");
            writer.append("import android.support.v7.app.AppCompatActivity;\n");
            writer.append("import android.view.View;\n");
            writer.append("import android.widget.AdapterView;\n");
            writer.append("import android.widget.ArrayAdapter;\n");
            writer.append("import android.widget.Button;\n");
            writer.append("import android.widget.DatePicker;\n");
            writer.append("import android.widget.EditText;\n");
            writer.append("import android.widget.ImageView;\n");
            writer.append("import android.widget.LinearLayout;\n");
            writer.append("import android.widget.ListView;\n");
            writer.append("import android.widget.RelativeLayout;\n");
            writer.append("import android.widget.Spinner;\n\n");
            writer.append("import java.text.SimpleDateFormat;\n");
            writer.append("import java.util.ArrayList;\n");
            writer.append("import java.util.Arrays;\n");
            writer.append("import java.util.Calendar;\n");
            writer.append("import java.util.HashMap;\n");
            writer.append("import java.util.List;\n\n");
            writer.append("import sergiocrespotoubes.com.dbgenerator.DBMain;\n\n");
            writer.append(String.format("public class %s extends AppCompatActivity {\n\n", fileName));
            writer.append("\tSQLiteDatabase db;\n\n");
            writer.append("\tfinal int SEARCH_INTEGER = 1;\n");
            writer.append("\tfinal int SEARCH_DATE = 2;\n");
            writer.append("\tfinal int SEARCH_TEXT = 3;\n\n");
            writer.append("\tContext context;\n\n");
            writer.append("\tSimpleDateFormat format = new SimpleDateFormat(\"dd MMM yyyy\");\n");
            writer.append("\tList<String> lTables;\n");
            writer.append("\tList<String> lColumns;\n");
            writer.append("\tList<String> lValues;\n");
            writer.append("\tList<String>lItems;\n");
            writer.append("\tListView lv_data;\n");
            writer.append("\tSpinner spinner_table;\n");
            writer.append("\tSpinner spinner_column;\n");
            writer.append("\tSpinner spinner_value;\n");
            writer.append("\tString table;\n");
            writer.append("\tString column; //order by column\n");
            writer.append("\tString value;\n");
            writer.append("\tHashMap<String, Integer>dbTypes;\n");
            writer.append("\tEditText et_value_start;\n");
            writer.append("\tEditText et_value_end;\n");
            writer.append("\tLinearLayout ll_values;\n");
            writer.append("\tLinearLayout ll_dates;\n");
            writer.append("\tLinearLayout ll_filters;\n");
            writer.append("\tEditText et_string;\n");
            writer.append("\tRelativeLayout rl_integer;\n");
            writer.append("\tButton bt_filter;\n");
            writer.append("\tint typeSearch = 0;\n");
            writer.append("\tImageView iv_date;\n");
            writer.append("\tImageView iv_filter;\n");
            writer.append("\tButton bt_start_date;\n");
            writer.append("\tButton bt_end_date;\n");
            writer.append("\tDatePickerDialog dpdStart;\n");
            writer.append("\tDatePickerDialog dpdEnd;\n");
            writer.append("\tCalendar calStart = Calendar.getInstance();\n");
            writer.append("\tCalendar calEnd = Calendar.getInstance();\n\n");
            writer.append("\t@Override\n");
            writer.append("\tprotected void onCreate(Bundle savedInstanceState) {\n");
            writer.append("\t\tsuper.onCreate(savedInstanceState);\n");
            writer.append("\t\tsetContentView(R.layout.activity_database2);\n\n");
            writer.append("\t\tcontext = this;\n");
            writer.append(String.format("\t\tdb = DBMain.getInstance().openDatabase(this, \"%s\");\n\n", databaseName));
            writer.append("\t\tloadViews();\n");
            writer.append("\t\tloadTablesList();\n");
            writer.append("\t\tloadListeners();\n");
            writer.append("\t\tloadSpinnerTables();\n");
            writer.append("\t}\n\n");
            writer.append("\tprivate void loadViews(){\n");
            writer.append("\t\tlv_data = (ListView) findViewById(R.id.lv_data);\n");
            writer.append("\t\tspinner_table = (Spinner) findViewById(R.id.spinner_table);\n");
            writer.append("\t\tspinner_column = (Spinner) findViewById(R.id.spinner_column);\n");
            writer.append("\t\tspinner_value = (Spinner) findViewById(R.id.spinner_value);\n");
            writer.append("\t\tet_string = (EditText) findViewById(R.id.et_string);\n");
            writer.append("\t\tet_value_start = (EditText) findViewById(R.id.et_value_start);\n");
            writer.append("\t\tet_value_end = (EditText) findViewById(R.id.et_value_end);\n");
            writer.append("\t\tll_values = (LinearLayout) findViewById(R.id.ll_values);\n");
            writer.append("\t\tll_filters = (LinearLayout) findViewById(R.id.ll_filters);\n");
            writer.append("\t\tbt_start_date = (Button) findViewById(R.id.bt_start_date);\n");
            writer.append("\t\tbt_end_date = (Button) findViewById(R.id.bt_end_date);\n");
            writer.append("\t\tll_dates = (LinearLayout) findViewById(R.id.ll_dates);\n");
            writer.append("\t\trl_integer = (RelativeLayout) findViewById(R.id.rl_integer);\n");
            writer.append("\t\tbt_filter = (Button) findViewById(R.id.bt_filter);\n");
            writer.append("\t\tiv_date = (ImageView) findViewById(R.id.iv_date);\n");
            writer.append("\t\tiv_filter = (ImageView) findViewById(R.id.iv_filter);\n");
            writer.append("\t\tbt_start_date = (Button) findViewById(R.id.bt_start_date);\n");
            writer.append("\t\tbt_end_date = (Button) findViewById(R.id.bt_end_date);\n");
            writer.append("\t}\n\n");
            writer.append("\tprivate void loadListeners() {\n");
            writer.append("\t\tspinner_table.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {\n");
            writer.append("\t\t\t@Override\n");
            writer.append("\t\t\tpublic void onItemSelected(AdapterView<?> parent, View view, int position, long id) {\n");
            writer.append("\t\t\t\ttable = spinner_table.getSelectedItem().toString();\n\n");
            writer.append("\t\t\t\thideFilters();\n");
            writer.append("\t\t\t\tloadSpinnerColumns();\n");
            writer.append("\t\t\t\tloadData();\n");
            writer.append("\t\t\t}\n\n");
            writer.append("\t\t\t@Override\n");
            writer.append("\t\t\tpublic void onNothingSelected(AdapterView<?> parent) {\n\n");
            writer.append("\t\t\t}\n");
            writer.append("\t\t});\n\n");
            writer.append("\t\tspinner_column.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {\n");
            writer.append("\t\t\t@Override\n");
            writer.append("\t\t\tpublic void onItemSelected(AdapterView<?> parent, View view, int position, long id) {\n");
            writer.append("\t\t\t\tcolumn = spinner_column.getSelectedItem().toString();\n\n");
            writer.append("\t\t\t\thideFilters();\n");
            writer.append("\t\t\t\tif(position != 0){\n");
            writer.append("\t\t\t\t\tloadSpinnerValues();\n\n");
            writer.append("\t\t\t\t\tif(column != null){\n");
            writer.append("\t\t\t\t\t\tif(dbTypes.containsKey(column)){\n");
            writer.append("\t\t\t\t\t\t\tswitch(dbTypes.get(column)){\n");
            writer.append("\t\t\t\t\t\t\t\tcase Cursor.FIELD_TYPE_INTEGER:\n");
            writer.append("\t\t\t\t\t\t\t\t\tll_values.setVisibility(View.VISIBLE);\n");
            writer.append("\t\t\t\t\t\t\t\t\trl_integer.setVisibility(View.VISIBLE);\n");
            writer.append("\t\t\t\t\t\t\t\t\tbreak;\n");
            writer.append("\t\t\t\t\t\t\t\tcase Cursor.FIELD_TYPE_STRING:\n");
            writer.append("\t\t\t\t\t\t\t\t\tet_string.setVisibility(View.VISIBLE);\n");
            writer.append("\t\t\t\t\t\t\t\t\tbreak;\n");
            writer.append("\t\t\t\t\t\t\t}\n");
            writer.append("\t\t\t\t\t\t}\n");
            writer.append("\t\t\t\t\t}\n");
            writer.append("\t\t\t\t}else{\n");
            writer.append("\t\t\t\t\tcolumn = null;\n");
            writer.append("\t\t\t\t}\n");
            writer.append("\t\t\t\tloadData();\n");
            writer.append("\t\t\t}\n\n");
            writer.append("\t\t\t@Override\n");
            writer.append("\t\t\tpublic void onNothingSelected(AdapterView<?> parent) {\n\n");
            writer.append("\t\t\t}\n");
            writer.append("\t\t});\n\n");
            writer.append("\t\tspinner_value.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {\n");
            writer.append("\t\t\t@Override\n");
            writer.append("\t\t\tpublic void onItemSelected(AdapterView<?> parent, View view, int position, long id) {\n");
            writer.append("\t\t\t\tif(position != 0){\n");
            writer.append("\t\t\t\t\tvalue = spinner_value.getSelectedItem().toString();\n");
            writer.append("\t\t\t\t}else{\n");
            writer.append("\t\t\t\t\tvalue = null;\n");
            writer.append("\t\t\t\t}\n");
            writer.append("\t\t\t\tloadData();\n");
            writer.append("\t\t\t}\n\n");
            writer.append("\t\t\t@Override\n");
            writer.append("\t\t\tpublic void onNothingSelected(AdapterView<?> parent) {\n\n");
            writer.append("\t\t\t}\n");
            writer.append("\t\t});\n\n");
            writer.append("\t\tbt_filter.setOnClickListener(new View.OnClickListener() {\n");
            writer.append("\t\t\t@Override\n");
            writer.append("\t\t\tpublic void onClick(View v) {\n");
            writer.append("\t\t\t\tloadData();\n");
            writer.append("\t\t\t}\n");
            writer.append("\t\t});\n\n");
            writer.append("\t\tiv_date.setOnClickListener(new View.OnClickListener() {\n");
            writer.append("\t\t\t@Override\n");
            writer.append("\t\t\tpublic void onClick(View v) {\n");
            writer.append("\t\t\t\tif(ll_values.getVisibility() == View.VISIBLE){\n");
            writer.append("\t\t\t\t\tll_dates.setVisibility(View.VISIBLE);\n");
            writer.append("\t\t\t\t\tll_values.setVisibility(View.GONE);\n");
            writer.append("\t\t\t\t}else{\n");
            writer.append("\t\t\t\t\tll_dates.setVisibility(View.GONE);\n");
            writer.append("\t\t\t\t\tll_values.setVisibility(View.VISIBLE);\n");
            writer.append("\t\t\t\t}\n");
            writer.append("\t\t\t}\n");
            writer.append("\t\t});\n\n");
            writer.append("\t\tiv_filter.setOnClickListener(new View.OnClickListener() {\n");
            writer.append("\t\t\t@Override\n");
            writer.append("\t\t\tpublic void onClick(View v) {\n");
            writer.append("\t\t\t\tif(ll_filters.getVisibility() == View.VISIBLE){\n");
            writer.append("\t\t\t\t\tll_filters.setVisibility(View.GONE);\n");
            writer.append("\t\t\t\t}else{\n");
            writer.append("\t\t\t\t\tll_filters.setVisibility(View.VISIBLE);\n");
            writer.append("\t\t\t\t}\n");
            writer.append("\t\t\t}\n");
            writer.append("\t\t});\n\n");
            writer.append("\t\tbt_start_date.setOnClickListener(new View.OnClickListener() {\n");
            writer.append("\t\t\t@Override\n");
            writer.append("\t\t\tpublic void onClick(View v) {\n\n");
            writer.append("\t\t\t\tdpdStart = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {\n");
            writer.append("\t\t\t\t\t@Override\n");
            writer.append("\t\t\t\t\tpublic void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {\n");
            writer.append("\t\t\t\t\t\tcalStart.set(Calendar.DAY_OF_MONTH, dayOfMonth);\n");
            writer.append("\t\t\t\t\t\tcalStart.set(Calendar.MONTH, monthOfYear);\n");
            writer.append("\t\t\t\t\t\tcalStart.set(Calendar.YEAR, year);\n");
            writer.append("\t\t\t\t\t\tbt_start_date.setText(format.format(calStart.getTime()));\n");
            writer.append("\t\t\t\t\t}\n");
            writer.append("\t\t\t\t}, calStart.get(Calendar.YEAR), calStart.get(Calendar.MONTH), calStart.get(Calendar.DAY_OF_MONTH));\n");
            writer.append("\t\t\t\tdpdStart.show();\n");
            writer.append("\t\t\t}\n");
            writer.append("\t\t});\n\n");
            writer.append("\t\tbt_end_date.setOnClickListener(new View.OnClickListener() {\n");
            writer.append("\t\t\t@Override\n");
            writer.append("\t\t\tpublic void onClick(View v) {\n");
            writer.append("\t\t\t\tdpdEnd = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {\n");
            writer.append("\t\t\t\t\t@Override\n");
            writer.append("\t\t\t\t\tpublic void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {\n");
            writer.append("\t\t\t\t\t\tcalEnd.set(Calendar.DAY_OF_MONTH, dayOfMonth);\n");
            writer.append("\t\t\t\t\t\tcalEnd.set(Calendar.MONTH, monthOfYear);\n");
            writer.append("\t\t\t\t\t\tcalEnd.set(Calendar.YEAR, year);\n");
            writer.append("\t\t\t\t\t\tbt_end_date.setText(format.format(calEnd.getTime()));\n");
            writer.append("\t\t\t\t\t}\n");
            writer.append("\t\t\t\t}, calEnd.get(Calendar.YEAR), calEnd.get(Calendar.MONTH), calEnd.get(Calendar.DAY_OF_MONTH));\n");
            writer.append("\t\t\t\tdpdEnd.show();\n");
            writer.append("\t\t\t}\n");
            writer.append("\t\t});\n\n");
            writer.append("\t}\n\n");
            writer.append("\tprivate void hideFilters() {\n");
            writer.append("\t\tll_values.setVisibility(View.GONE);\n");
            writer.append("\t\tll_dates.setVisibility(View.GONE);\n");
            writer.append("\t\tet_string.setVisibility(View.GONE);\n");
            writer.append("\t\trl_integer.setVisibility(View.GONE);\n");
            writer.append("\t}\n\n");
            writer.append("\tprivate void loadSpinnerTables(){\n");
            writer.append("\t\tArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lTables);\n");
            writer.append("\t\tspinner_table.setAdapter(adapter);\n");
            writer.append("\t}\n\n");
            writer.append("\tprivate void loadSpinnerColumns(){\n");
            writer.append("\t\tlColumns = new ArrayList<String>();\n");
            writer.append("\t\tlColumns.add(\"*\");\n");
            writer.append("\t\tloadColumsTableList(table);\n");
            writer.append("\t\tArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lColumns);\n");
            writer.append("\t\tspinner_column.setAdapter(adapter);\n");
            writer.append("\t}\n\n");
            writer.append("\tprivate void loadSpinnerValues(){\n");
            writer.append("\t\tlValues = new ArrayList<String>();\n");
            writer.append("\t\tlValues.add(\"*\");\n");
            writer.append("\t\tloadValuesGroup();\n\n");
            writer.append("\t\tArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lValues);\n");
            writer.append("\t\tspinner_value.setAdapter(adapter);\n");
            writer.append("\t}\n\n");
            writer.append("\tprivate void loadTablesList(){\n");
            writer.append("\t\tlTables = new ArrayList<>();\n\n");
            writer.append("\t\tCursor c = db.rawQuery(\"SELECT name FROM sqlite_master WHERE type='table'\", null);\n\n");
            writer.append("\t\tif (c.moveToFirst()) {\n");
            writer.append("\t\t\twhile ( !c.isAfterLast() ) {\n");
            writer.append("\t\t\t\tlTables.add(c.getString(0));\n");
            writer.append("\t\t\t\tc.moveToNext();\n");
            writer.append("\t\t\t}\n");
            writer.append("\t\t}\n");
            writer.append("\t}\n\n");
            writer.append("\tprivate void loadColumsTableList(String table){\n");
            writer.append("\t\tCursor dbCursor = db.query(table, null, null, null, null, null, null);\n");
            writer.append("\t\tString[] columnNames = dbCursor.getColumnNames();\n");
            writer.append("\t\tint numColumns = dbCursor.getColumnCount();\n\n");
            writer.append("\t\tif(numColumns > 0){\n");
            writer.append("\t\t\tdbTypes = new HashMap();\n\n");
            writer.append("\t\t\tif(dbCursor.moveToNext()){\n");
            writer.append("\t\t\t\tfor (int i = 0; i < numColumns; i++) {\n");
            writer.append("\t\t\t\t\tdbTypes.put(dbCursor.getColumnName(i), dbCursor.getType(i));\n");
            writer.append("\t\t\t\t}\n");
            writer.append("\t\t\t}\n");
            writer.append("\t\t}else{\n");
            writer.append("\t\t\tdbTypes = null;\n");
            writer.append("\t\t}\n\n");
            writer.append("\t\tlColumns = Arrays.asList(columnNames);\n");
            writer.append("\t}\n\n");
            writer.append("\tprivate void loadValuesGroup(){\n");
            writer.append("\t\tCursor c = db.rawQuery(String.format(\"SELECT %s FROM %s group by %s\", column, table, column), null);\n");
            writer.append("\t\tString auxValue;\n\n");
            writer.append("\t\twhile (c.moveToNext()) {\n");
            writer.append("\t\t\tauxValue = c.getString(0);\n\n");
            writer.append("\t\t\tif(auxValue != null && !auxValue.equals(\"\")){\n");
            writer.append("\t\t\t\tlValues.add(c.getString(0));\n");
            writer.append("\t\t\t}\n");
            writer.append("\t\t}\n");
            writer.append("\t}\n\n");
            writer.append("\tprivate void loadByTable(){\n");
            writer.append("\t\tlItems = new ArrayList<>();\n");
            writer.append("\t\tCursor c = db.rawQuery(String.format(\"SELECT * FROM %s\", table), null);\n");
            writer.append("\t\tloadFromCursor(c);\n");
            writer.append("\t}\n\n");
            writer.append("\tprivate void loadByColumn(String query){\n");
            writer.append("\t\tlItems = new ArrayList<>();\n");
            writer.append("\t\tCursor c = db.rawQuery(String.format(\"SELECT * FROM %s %s ORDER BY %s\", table, query, column), null);\n");
            writer.append("\t\tloadFromCursor(c);\n");
            writer.append("\t}\n\n");
            writer.append("\tprivate void loadByType(String query){\n");
            writer.append("\t\tlItems = new ArrayList<>();\n");
            writer.append("\t\tCursor c = db.rawQuery(String.format(\"SELECT * FROM %s WHERE %s='%s' %s\", table, column, value, query), null);\n");
            writer.append("\t\tloadFromCursor(c);\n");
            writer.append("\t}\n\n");
            writer.append("\tprivate void loadFromCursor(Cursor c){\n\n");
            writer.append("\t\tint numColumns = c.getColumnCount();\n");
            writer.append("\t\tString value;\n");
            writer.append("\t\tString item;\n\n");
            writer.append("\t\twhile (c.moveToNext()) {\n");
            writer.append("\t\t\titem = \"\";\n");
            writer.append("\t\t\tfor (int i = 0; i < numColumns; i++) {\n\n");
            writer.append("\t\t\t\titem += c.getColumnName(i) + \": \";\n");
            writer.append("\t\t\t\tvalue = c.getString(i);\n");
            writer.append("\t\t\t\tif(value != null){\n");
            writer.append("\t\t\t\t\titem += value;\n");
            writer.append("\t\t\t\t}\n");
            writer.append("\t\t\t\titem += \"\\n\";\n");
            writer.append("\t\t\t}\n");
            writer.append("\t\t\tlItems.add(item);\n");
            writer.append("\t\t}\n");
            writer.append("\t}\n\n");
            writer.append("\tprivate void loadData(){\n\n");
            writer.append("\t\tlItems = new ArrayList<>();\n\n");
            writer.append("\t\tif(table != null){\n");
            writer.append("\t\t\tif(column != null){\n\n");
            writer.append("\t\t\t\tString query = generateQuery();\n\n");
            writer.append("\t\t\t\tif(value != null){\n");
            writer.append("\t\t\t\t\tif(!query.equals(\"\")){\n");
            writer.append("\t\t\t\t\t\tquery = \" and \" + query;\n");
            writer.append("\t\t\t\t\t}\n");
            writer.append("\t\t\t\t\tloadByType(query);\n");
            writer.append("\t\t\t\t}else{\n");
            writer.append("\t\t\t\t\tif(!query.equals(\"\")){\n");
            writer.append("\t\t\t\t\t\tquery = \" where \" + query;\n");
            writer.append("\t\t\t\t\t}\n");
            writer.append("\t\t\t\t\tloadByColumn(query);\n");
            writer.append("\t\t\t\t}\n");
            writer.append("\t\t\t}else{\n");
            writer.append("\t\t\t\tloadByTable();\n");
            writer.append("\t\t\t}\n");
            writer.append("\t\t}\n");
            writer.append("\t\tArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lItems);\n");
            writer.append("\t\tlv_data.setAdapter(adapter);\n");
            writer.append("\t}\n\n");
            writer.append("\tprivate String generateQuery() {\n\n");
            writer.append("\t\tString query = \"\";\n\n");
            writer.append("\t\tif(ll_dates.getVisibility() == View.VISIBLE){\n");
            writer.append("\t\t\tquery = String.format(\" %s >= %d and %s <= %d \", column, calStart.getTimeInMillis(), column, calEnd.getTimeInMillis());\n");
            writer.append("\t\t}else if(ll_values.getVisibility() == View.VISIBLE){\n");
            writer.append("\t\t\tString value1 = et_value_start.getText().toString();\n");
            writer.append("\t\t\tString value2 = et_value_end.getText().toString();\n");
            writer.append("\t\t\tif(!value1.equals(\"\") && !value2.equals(\"\")){\n");
            writer.append("\t\t\t\tquery = String.format(\" %s >= %d and %s <= %d \", column, Integer.valueOf(value1), column, Integer.valueOf(value2));\n");
            writer.append("\t\t\t}\n");
            writer.append("\t\t}else if(et_string.getVisibility() == View.VISIBLE){\n");
            writer.append("\t\t\tString text = et_string.getText().toString();\n");
            writer.append("\t\t\tif(!text.equals(\"\")){\n");
            writer.append("\t\t\t\tquery = String.format(\" %s like '%%%s%%' \", column, text);\n");
            writer.append("\t\t\t}\n");
            writer.append("\t\t}\n\n");
            writer.append("\t\treturn query;\n");
            writer.append("\t}\n\n");
            writer.append("}	\n");

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}