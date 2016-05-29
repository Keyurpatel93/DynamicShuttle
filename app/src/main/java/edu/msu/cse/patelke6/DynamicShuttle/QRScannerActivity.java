package edu.msu.cse.patelke6.DynamicShuttleshuttle;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class QRScannerActivity extends BaseScannerActivity implements FullScannerFragment.QRScanListener {
    @Bind(R.id.temp) TextView status;
    @Bind(R.id.statusFrameLayout) FrameLayout statusLayout;

    private String rideDataFilePath;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_full_scanner_fragment);

        Intent intent = getIntent();
        rideDataFilePath = intent.getStringExtra("rideDataFilePath");

        ButterKnife.bind(this);
        setupToolbar();
    }


    @Override
    public void onSuccessfulScan(String json) {
        status.setMovementMethod(new ScrollingMovementMethod());
        try{
            JSONObject rideJson = new JSONObject(json);
            status.setText("Welcome Aboard "+ rideJson.getString("RiderName") + "\n\n" + json);
            statusLayout.setBackgroundResource(R.color.scan_successful);
            //status.setBackgroundResource(R.color.scan_successful);
            status.setTextColor(Color.BLACK);
            logData(json);
            android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                public void run() {
                    status.setText("");
                    statusLayout.setBackgroundColor(Color.TRANSPARENT);
                }
            }, 4000);
        }catch (Exception e){
            Log.e("Check if works", e.getMessage());
            status.setText("Not in Json Format!! \n QR Text Value: "+ json);
        }
    }

    private void logData(String json){
        String ridesDataJsonString = readJsonFromFile();
        if (ridesDataJsonString != null) {
            try {
                JSONObject processedRide = new JSONObject(json);

                JSONObject dataJson = new JSONObject(ridesDataJsonString);
                JSONArray dataJsonArray = dataJson.getJSONArray("Rides");
                Boolean alreadyProcessed = false;

                //Check to make sure QR code was not scanned twice
                for (int i = 0; i < dataJsonArray.length(); i++) {
                    JSONObject row = dataJsonArray.getJSONObject(i);
                   if(row.getString(JSONKeyNames.RideID).equals(processedRide.getString(JSONKeyNames.RideID))){
                       Log.i("QRScanner", "Ride Processed Already");
                       alreadyProcessed = true;
                   }
                }

                if(!alreadyProcessed) {

                    Date currentTime = new Date();
                    JSONObject rideToLog = new JSONObject();
                    rideToLog.put(JSONKeyNames.RideID, processedRide.getString(JSONKeyNames.RideID));
                    rideToLog.put(JSONKeyNames.RiderName, processedRide.getString(JSONKeyNames.RiderName));
                    rideToLog.put(JSONKeyNames.PassengerCount, processedRide.getInt(JSONKeyNames.PassengerCount));
                    rideToLog.put(JSONKeyNames.Destination, processedRide.getString(JSONKeyNames.Destination));
                    rideToLog.put(JSONKeyNames.PickUp, processedRide.getString(JSONKeyNames.PickUp));
                    rideToLog.put(JSONKeyNames.BoardingTime, processedRide.getLong(JSONKeyNames.BoardingTime));
                    rideToLog.put(JSONKeyNames.ArrivalTime, processedRide.getLong(JSONKeyNames.ArrivalTime));
                    rideToLog.put(JSONKeyNames.BoardedTime, currentTime.getTime());
                    dataJsonArray.put(rideToLog);

                    FileWriter file = new FileWriter(new File(rideDataFilePath), false);
                    file.append(dataJson.toString());
                    file.flush();
                    file.close();
                }
            } catch (Exception ex) {
                Log.e("QRScannerActivity", ex.toString());
            }
        }
    }


    //Todo in future create a singleton class to handle all json functionality
    private String readJsonFromFile(){
        String json = null;
        try {
            InputStream is = new FileInputStream(new File(rideDataFilePath));
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            Log.i("readJsonFromFile", ex.toString());
            return null;
        }
        return json;
    }

}