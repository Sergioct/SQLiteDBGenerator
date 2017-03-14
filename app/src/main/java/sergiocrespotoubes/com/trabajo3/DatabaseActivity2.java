package sergiocrespotoubes.com.trabajo3;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sergiocrespotoubes.com.dbgenerator.DBMain;

/**
 * Created by Sergio on 04-Mar-17.
 */

public class DatabaseActivity2 extends AppCompatActivity {

    SQLiteDatabase db;

    List<String> lTables;
    List<String> lColumns;
    List<String> lValues;
    List<String>lItems;
    ListView lv_data;
    Spinner spinner_table;
    Spinner spinner_column;
    Spinner spinner_value;
    String table;
    String column; //order by column
    String value; //only with data "X"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database2);

        db = DBMain.getInstance().openDatabase(this, "database2");

        loadViews();
        loadTablesList();
        loadListeners();
        loadSpinnerTables();
    }

    private void loadViews(){
        lv_data = (ListView) findViewById(R.id.lv_data);
        spinner_table = (Spinner) findViewById(R.id.spinner_table);
        spinner_column = (Spinner) findViewById(R.id.spinner_column);
        spinner_value = (Spinner) findViewById(R.id.spinner_value);
    }

    private void loadListeners() {
        spinner_table.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                table = spinner_table.getSelectedItem().toString();
                loadSpinnerColumns();
                loadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_column.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                column = spinner_column.getSelectedItem().toString();
                if(position != 0){
                    loadSpinnerValues();
                }else{
                    column = null;
                }
                loadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_value.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0){
                    value = spinner_value.getSelectedItem().toString();
                }else{
                    value = null;
                }
                loadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void loadSpinnerTables(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lTables);
        spinner_table.setAdapter(adapter);
    }

    private void loadSpinnerColumns(){
        lColumns = new ArrayList<String>();
        lColumns.add("*");
        loadColumsTableList(table);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lColumns);
        spinner_column.setAdapter(adapter);
    }

    private void loadSpinnerValues(){
        lValues = new ArrayList<String>();
        lValues.add("*");
        loadValuesGroup();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lValues);
        spinner_value.setAdapter(adapter);
    }

    private void loadTablesList(){
        lTables = new ArrayList<>();

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        if (c.moveToFirst()) {
            while ( !c.isAfterLast() ) {
                lTables.add(c.getString(0));
                c.moveToNext();
            }
        }
    }

    private void loadColumsTableList(String table){
        Cursor dbCursor = db.query(table, null, null, null, null, null, null);
        String[] columnNames = dbCursor.getColumnNames();
        lColumns = Arrays.asList(columnNames);
    }

    private void loadValuesGroup(){
        Cursor c = db.rawQuery(String.format("SELECT %s FROM %s group by %s", column, table, column), null);
        String auxValue;

        while (c.moveToNext()) {
            auxValue = c.getString(0);

            if(auxValue != null && !auxValue.equals("")){
                lValues.add(c.getString(0));
            }
        }
    }

    private void loadByTable(){
        lItems = new ArrayList<>();
        Cursor c = db.rawQuery(String.format("SELECT * FROM %s", table), null);
        loadFromCursor(c);
    }

    private void loadByColumn(){
        lItems = new ArrayList<>();
        Cursor c = db.rawQuery(String.format("SELECT * FROM %s ORDER BY %s", table, column), null);
        loadFromCursor(c);
    }

    private void loadByType(){
        lItems = new ArrayList<>();
        Cursor c = db.rawQuery(String.format("SELECT * FROM %s WHERE %s='%s'", table, column, value), null);
        loadFromCursor(c);
    }

    private void loadFromCursor(Cursor c){

        int numColumns = c.getColumnCount();
        String value;
        String item;

        while (c.moveToNext()) {
            item = "";
            for (int i = 0; i < numColumns; i++) {

                item += c.getColumnName(i) + ": ";
                value = c.getString(i);
                if(value != null){
                    item += value;
                }
                item += "\n";
            }
            lItems.add(item);
        }
    }

    private void loadData(){

        lItems = new ArrayList<>();

        if(table != null){
            if(column != null){
                if(value != null){
                    loadByType();
                }else{
                    loadByColumn();
                }
            }else{
                loadByTable();
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lItems);
        lv_data.setAdapter(adapter);
    }

}
