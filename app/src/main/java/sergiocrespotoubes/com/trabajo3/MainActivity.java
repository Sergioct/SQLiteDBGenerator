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
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import sergiocrespotoubes.com.dbgenerator.DBMain;

public class MainActivity extends AppCompatActivity {

    final String GENERATED_PATH = Environment.getExternalStorageDirectory().toString()+"/Mydata/";
    SQLiteDatabase db;

    Button bt_open_folder;
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
        bt_open_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFolder();
            }
        });
    }

    private void havePermissions(){

        /* Test 1 */
        db = DBMain.getInstance().createDatabase(this, "database1", "meta1.txt");
        DBMain.generateJavaCrudRepository(this, GENERATED_PATH, "meta1.txt");
        DBMain.generateIosCrudRepository(this, GENERATED_PATH, "meta1.txt");

        /* Test 2 */
        db = DBMain.getInstance().createDatabase(this, "database2", "meta2.txt");
        DBMain.generateJavaCrudRepository(this, GENERATED_PATH, "meta2.txt");
        DBMain.generateIosCrudRepository(this, GENERATED_PATH, "meta2.txt");

        /* Test 3 */
        db = DBMain.getInstance().createDatabase(this, "database3", "meta3.txt");
        DBMain.generateJavaCrudRepository(this, GENERATED_PATH, "meta3.txt");
        DBMain.generateIosCrudRepository(this, GENERATED_PATH, "meta3.txt");
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