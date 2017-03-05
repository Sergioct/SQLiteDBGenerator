package sergiocrespotoubes.com.trabajo3;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sergio on 04-Mar-17.
 */

public class DatabaseActivity2 extends AppCompatActivity {

    ListView lv_data;
    Spinner spinner_table;
    Spinner spinner_column;
    Spinner spinner_category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database2);

        loadViews();
        loadListeners();
        loadSpinnerTables();
        //loadSpinnerColumns();
        //loadSpinnerCategories();
    }

    private void loadViews(){
        lv_data = (ListView) findViewById(R.id.lv_data);
        spinner_table = (Spinner) findViewById(R.id.spinner_table);
        spinner_column = (Spinner) findViewById(R.id.spinner_column);
        spinner_category = (Spinner) findViewById(R.id.spinner_category);
    }

    private void loadListeners() {
        spinner_table.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadSpinnerColumns(spinner_table.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_column.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("TEST", "Change column");

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("TEST", "Change category");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void loadSpinnerTables(){
        List<String> lTables = new ArrayList();

        lTables.add("fasfafs");
        lTables.add("fasfafs1");
        lTables.add("fasfafs2");
        lTables.add("fasfafs3");
        lTables.add("fasfafs4");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lTables);
        spinner_table.setAdapter(adapter);
    }

    private void loadSpinnerColumns(String table){
        List<String> lColumns = new ArrayList();



        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lColumns);
        spinner_column.setAdapter(adapter);
    }


    private void loadSpinnerCategories(){
        List<String> lCategories = new ArrayList();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lCategories);
        spinner_category.setAdapter(adapter);
    }


}
