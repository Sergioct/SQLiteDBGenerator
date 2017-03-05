package sergiocrespotoubes.com.trabajo3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.List;

import sergiocrespotoubes.com.dbgenerator.DBMain;

public class MainActivity extends AppCompatActivity {

    final String GENERATED_PATH = Environment.getExternalStorageDirectory().toString()+"/Mydata/";
    SQLiteDatabase db0;
    SQLiteDatabase db1;
    SQLiteDatabase db2;

    Button bt_open_folder;
    Button bt_database1;
    Button bt_database2;

    int PERMISSION_REQUEST = 5349;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, PERMISSION_REQUEST);
            }else {
                havePermissions();
            }
        }else{
            havePermissions();
        }

        bt_open_folder = (Button) findViewById(R.id.bt_open_folder);
        bt_database1 = (Button) findViewById(R.id.bt_database2);
        bt_database2 = (Button) findViewById(R.id.bt_database1);

        bt_open_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFolder();
            }
        });

        bt_database1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatabase1();
            }
        });

        bt_database2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatabase2();
            }
        });
    }

    private void havePermissions(){

        /* Test 1 */
        db0 = DBMain.getInstance().createDatabase(this, "database1", "meta1.txt");
        DBMain.generateJavaCrudRepository(this, GENERATED_PATH, "meta1.txt");
        DBMain.generateIosCrudRepository(this, GENERATED_PATH, "meta1.txt");
        /*
        Primera primera = new Primera();
        primera.setCol_primaria("Primer texto");
        primera.setCol_unica("Primer texto unico");
        primera.setCol_entero(1);

        //Insert
        long id = PrimeraRepository.insert(db0, primera);

        //GetById
        Primera primera1 = PrimeraRepository.getById(db0, id);

        // Update
        primera1.setCol_entero(2);
        PrimeraRepository.update(db0, primera1);

        //Get All
        List<Primera>lPrimera = PrimeraRepository.getAll(db0);

        //delete
        PrimeraRepository.delete(db0, id);
        lPrimera = PrimeraRepository.getAll(db0);*/

        /* Test 2 */
        db1 = DBMain.getInstance().createDatabase(this, "database2", "meta2.txt");
        DBMain.generateJavaCrudRepository(this, GENERATED_PATH, "meta2.txt");
        DBMain.generateIosCrudRepository(this, GENERATED_PATH, "meta2.txt");

        /* Test 3 */
        db2 = DBMain.getInstance().createDatabase(this, "database3", "meta3.txt");
        DBMain.generateJavaCrudRepository(this, GENERATED_PATH, "meta3.txt");
        DBMain.generateIosCrudRepository(this, GENERATED_PATH, "meta3.txt");
    }

    private void openDatabase1(){
        Intent intent = new Intent(this, DatabaseActivity2.class);
        startActivity(intent);
    }

    private void openDatabase2(){
        Intent intent = new Intent(this, DatabaseActivity2.class);
        startActivity(intent);
    }

    private void openFolder(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(GENERATED_PATH);
        intent.setDataAndType(uri, "text/csv");
        startActivity(Intent.createChooser(intent, "Open folder"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_REQUEST){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                havePermissions();
            }
        }
    }

}