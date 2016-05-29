package edu.msu.cse.patelke6.DynamicShuttleshuttle;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;

import butterknife.Bind;
import butterknife.ButterKnife;
//https://github.com/dm77/barcodescanner

public class MainActivity extends AppCompatActivity {
    private static final int ZXING_CAMERA_PERMISSION = 1;
    private Class<?> mClss;
    private String rideDataFilePath;
    private static final int MY_PERMISSIONS_REQUEST = 0;
    private static String[] PERMISSIONS_REQUEST = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Bind(R.id.startScanningBtn) Button startQRActivityBtn;
    @Bind(R.id.viewStatisticsBtn) Button startStatisticsActivityBtn;
    @Bind(R.id.MobilityLogoView) ImageView MobilityLogoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

        Picasso.with(this)
                .load(R.drawable.logo)
                .fit()
                .centerInside()
                .into(MobilityLogoView);

        rideDataFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "DynamicShuttleRides.txt";


        requestReadWritePermission();

        if(!checkDataFile()){
            //Todo if file cannot be created or loaded, do not log data or allow access to Statistics activity
            Toast.makeText(this, "Error Loading Data File. Statistics Will Not Available", Toast.LENGTH_SHORT).show();
        }

        startQRActivityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchActivity(QRScannerActivity.class);
            }
        });
        startStatisticsActivityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), StatisticsActivity.class);
                intent.putExtra("rideDataFilePath", rideDataFilePath);
                startActivity(intent);
            }
        });


    }

    //Ask the user for Read and Write External Storage Permission
    private void requestReadWritePermission(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // todo Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(this, PERMISSIONS_REQUEST,
                        MY_PERMISSIONS_REQUEST);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, PERMISSIONS_REQUEST,
                        MY_PERMISSIONS_REQUEST);
            }
        }
    }

    //Start the Scanning Activity
    public void launchActivity(Class<?> clss) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            mClss = clss;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(this, clss);
            intent.putExtra("rideDataFilePath", rideDataFilePath);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ZXING_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchActivity(QRScannerActivity.class);
                } else {
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
                }

            case MY_PERMISSIONS_REQUEST:
                if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    checkDataFile();
                    Log.i("Permission", "Write and Read permission has now been granted.");

                } else {
                    //ToDo do not exit application, just do not store the information for the statistics activity
                    Log.i("Permission", "Write and Read permission was NOT granted.");
                    Toast.makeText(this, "Permission required to operate app", Toast.LENGTH_LONG).show();

                    //Close App
                    android.os.Handler handler = new android.os.Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            System.exit(0);
                        }
                    }, 2000);
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                return;
        }
    }

    //Check if the data file used to storage ride history exist, if it does not create it once we have
    //read write permissions.
    private boolean checkDataFile(){
        File ridesJson = new File(rideDataFilePath);
        try{
            if (!ridesJson.exists()) {
                ridesJson.createNewFile();
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                jsonObject.put(JSONKeyNames.RootKey, jsonArray);

                FileWriter file = new FileWriter(ridesJson, false);
                file.append(jsonObject.toString());
                file.flush();
                file.close();
                Log.i("Create File", "ShuttleData.json created");
            }

        }catch (Exception ex){
            Log.e("Main Activity","Error Creating File: " + ex.toString());
            return false;
        }
        return true;
    }

 }

