package sergiocrespotoubes.com.trabajo3.database;

import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import sergiocrespotoubes.com.dbgenerator.DBMain;
import sergiocrespotoubes.com.trabajo3.R;

public class Database4Activity extends AppCompatActivity {

	SQLiteDatabase db;

	final int SEARCH_INTEGER = 1;
	final int SEARCH_DATE = 2;
	final int SEARCH_TEXT = 3;

	Context context;

	SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");
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
	String value;
	HashMap<String, Integer>dbTypes;
	EditText et_value_start;
	EditText et_value_end;
	LinearLayout ll_values;
	LinearLayout ll_dates;
	LinearLayout ll_filters;
	EditText et_string;
	RelativeLayout rl_integer;
	Button bt_filter;
	int typeSearch = 0;
	ImageView iv_date;
	ImageView iv_filter;
	Button bt_start_date;
	Button bt_end_date;
	DatePickerDialog dpdStart;
	DatePickerDialog dpdEnd;
	Calendar calStart = Calendar.getInstance();
	Calendar calEnd = Calendar.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_database2);

		context = this;
		db = DBMain.getInstance().openDatabase(this, "database4");

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
		et_string = (EditText) findViewById(R.id.et_string);
		et_value_start = (EditText) findViewById(R.id.et_value_start);
		et_value_end = (EditText) findViewById(R.id.et_value_end);
		ll_values = (LinearLayout) findViewById(R.id.ll_values);
		ll_filters = (LinearLayout) findViewById(R.id.ll_filters);
		bt_start_date = (Button) findViewById(R.id.bt_start_date);
		bt_end_date = (Button) findViewById(R.id.bt_end_date);
		ll_dates = (LinearLayout) findViewById(R.id.ll_dates);
		rl_integer = (RelativeLayout) findViewById(R.id.rl_integer);
		bt_filter = (Button) findViewById(R.id.bt_filter);
		iv_date = (ImageView) findViewById(R.id.iv_date);
		iv_filter = (ImageView) findViewById(R.id.iv_filter);
		bt_start_date = (Button) findViewById(R.id.bt_start_date);
		bt_end_date = (Button) findViewById(R.id.bt_end_date);
	}

	private void loadListeners() {
		spinner_table.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				table = spinner_table.getSelectedItem().toString();

				hideFilters();
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

				hideFilters();
				if(position != 0){
					loadSpinnerValues();

					if(column != null){
						if(dbTypes.containsKey(column)){
							switch(dbTypes.get(column)){
								case Cursor.FIELD_TYPE_INTEGER:
									ll_values.setVisibility(View.VISIBLE);
									rl_integer.setVisibility(View.VISIBLE);
									break;
								case Cursor.FIELD_TYPE_STRING:
									et_string.setVisibility(View.VISIBLE);
									break;
							}
						}
					}
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

		bt_filter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadData();
			}
		});

		iv_date.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(ll_values.getVisibility() == View.VISIBLE){
					ll_dates.setVisibility(View.VISIBLE);
					ll_values.setVisibility(View.GONE);
				}else{
					ll_dates.setVisibility(View.GONE);
					ll_values.setVisibility(View.VISIBLE);
				}
			}
		});

		iv_filter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(ll_filters.getVisibility() == View.VISIBLE){
					ll_filters.setVisibility(View.GONE);
				}else{
					ll_filters.setVisibility(View.VISIBLE);
				}
			}
		});

		bt_start_date.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				dpdStart = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						calStart.set(Calendar.DAY_OF_MONTH, dayOfMonth);
						calStart.set(Calendar.MONTH, monthOfYear);
						calStart.set(Calendar.YEAR, year);
						bt_start_date.setText(format.format(calStart.getTime()));
					}
				}, calStart.get(Calendar.YEAR), calStart.get(Calendar.MONTH), calStart.get(Calendar.DAY_OF_MONTH));
				dpdStart.show();
			}
		});

		bt_end_date.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dpdEnd = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						calEnd.set(Calendar.DAY_OF_MONTH, dayOfMonth);
						calEnd.set(Calendar.MONTH, monthOfYear);
						calEnd.set(Calendar.YEAR, year);
						bt_end_date.setText(format.format(calEnd.getTime()));
					}
				}, calEnd.get(Calendar.YEAR), calEnd.get(Calendar.MONTH), calEnd.get(Calendar.DAY_OF_MONTH));
				dpdEnd.show();
			}
		});

	}

	private void hideFilters() {
		ll_values.setVisibility(View.GONE);
		ll_dates.setVisibility(View.GONE);
		et_string.setVisibility(View.GONE);
		rl_integer.setVisibility(View.GONE);
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
		int numColumns = dbCursor.getColumnCount();

		if(numColumns > 0){
			dbTypes = new HashMap();

			if(dbCursor.moveToNext()){
				for (int i = 0; i < numColumns; i++) {
					dbTypes.put(dbCursor.getColumnName(i), dbCursor.getType(i));
				}
			}
		}else{
			dbTypes = null;
		}

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

	private void loadByColumn(String query){
		lItems = new ArrayList<>();
		Cursor c = db.rawQuery(String.format("SELECT * FROM %s %s ORDER BY %s", table, query, column), null);
		loadFromCursor(c);
	}

	private void loadByType(String query){
		lItems = new ArrayList<>();
		Cursor c = db.rawQuery(String.format("SELECT * FROM %s WHERE %s='%s' %s", table, column, value, query), null);
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

				String query = generateQuery();

				if(value != null){
					if(!query.equals("")){
						query = " and " + query;
					}
					loadByType(query);
				}else{
					if(!query.equals("")){
						query = " where " + query;
					}
					loadByColumn(query);
				}
			}else{
				loadByTable();
			}
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lItems);
		lv_data.setAdapter(adapter);
	}

	private String generateQuery() {

		String query = "";

		if(ll_dates.getVisibility() == View.VISIBLE){
			query = String.format(" %s >= %d and %s <= %d ", column, calStart.getTimeInMillis(), column, calEnd.getTimeInMillis());
		}else if(ll_values.getVisibility() == View.VISIBLE){
			String value1 = et_value_start.getText().toString();
			String value2 = et_value_end.getText().toString();
			if(!value1.equals("") && !value2.equals("")){
				query = String.format(" %s >= %d and %s <= %d ", column, Integer.valueOf(value1), column, Integer.valueOf(value2));
			}
		}else if(et_string.getVisibility() == View.VISIBLE){
			String text = et_string.getText().toString();
			if(!text.equals("")){
				query = String.format(" %s like '%%%s%%' ", column, text);
			}
		}

		return query;
	}

}	
