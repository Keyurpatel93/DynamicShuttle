package edu.msu.cse.patelke6.DynamicShuttleshuttle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by keyurpatel on 4/25/16.
 */
public class StatisticsActivity extends AppCompatActivity {
    @Bind(R.id.passengersBoardedView) TextView passengerBoardedView;
    @Bind(R.id.ridesGivenView) TextView ridesGivenView;
    @Bind(R.id.arrivalTimeDelayView) TextView arrivalTimeDelayedView;
    @Bind(R.id.pickUpTimeDelayView) TextView pickUpTimeDelayedView;
    @Bind(R.id.resetDataBtn) Button restBtn;

    private String rideDataFilePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics_activity);

        ButterKnife.bind(this);
        Intent intent = getIntent();
        rideDataFilePath = intent.getStringExtra("rideDataFilePath");

        restBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restDataFile();
            }
        });

        updateView();
    }


    private void updateView(){
        String ridesDataJsonString = readJsonFromFile();
        if (ridesDataJsonString != null) {
            try {
                JSONObject dataJson = new JSONObject(ridesDataJsonString);
                JSONArray dataJsonArray = dataJson.getJSONArray(JSONKeyNames.RootKey);

                int passengerCount = 0;
                int ridesProcessed = dataJsonArray.length();


                long delayPickUp = 0;
                for (int i = 0; i < dataJsonArray.length(); i++) {
                    JSONObject row = dataJsonArray.getJSONObject(i);

                    passengerCount += row.getInt(JSONKeyNames.PassengerCount);
                    Date rideBoardingTime = new Date(row.getLong(JSONKeyNames.BoardingTime));
                    Date actualPickUpTime = new Date(row.getLong(JSONKeyNames.BoardedTime));

                    delayPickUp += actualPickUpTime.getTime() - rideBoardingTime.getTime();
                }
                if(ridesProcessed == 0){
                    delayPickUp = 0;
                } else
                    delayPickUp = delayPickUp/ridesProcessed;

                //Highly dependent on time being correctly synchronized across devices
                //read time values from a server in the future.

                String averageDelay = String.format("%02d min: %02d sec",
                        TimeUnit.MILLISECONDS.toMinutes(delayPickUp),
                        TimeUnit.MILLISECONDS.toSeconds(delayPickUp) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(delayPickUp)));
                Log.i("DelayPickUp" , averageDelay);

                passengerBoardedView.setText(Integer.toString(passengerCount));
                ridesGivenView.setText(Integer.toString(ridesProcessed));
                pickUpTimeDelayedView.setText(averageDelay);
                if(delayPickUp <= 0){
                    pickUpTimeDelayedView.setText(averageDelay.replace("-",""));
                    pickUpTimeDelayedView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                } else{
                    pickUpTimeDelayedView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }


            } catch (Exception ex) {
                Log.e("QRScannerActivity", ex.toString());
            }
        }
    }


    //Clears content of the file and write the root JSON object
    private void restDataFile(){
        File ridesJson = new File(rideDataFilePath);
        try{
            if (ridesJson.exists()) {
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                jsonObject.put(JSONKeyNames.RootKey, jsonArray);

                FileWriter file = new FileWriter(ridesJson);
                file.write(jsonObject.toString());
                file.flush();
                file.close();
            } else {
                Toast.makeText(this,"Data File Does Not Exist",Toast.LENGTH_LONG).show();
                return;
            }

        }catch (Exception ex){
            Toast.makeText(this,"Rest Failed " + ex.toString(),Toast.LENGTH_LONG).show();
        }
        Toast.makeText(this,"Data Reset Successfully",Toast.LENGTH_SHORT).show();
        updateView();
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
