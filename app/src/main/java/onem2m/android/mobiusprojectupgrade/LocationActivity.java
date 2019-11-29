package onem2m.android.mobiusprojectupgrade;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.GeoPoint;
import com.google.type.LatLng;

import java.util.Timer;
import java.util.TimerTask;

public class LocationActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1000;
    private GeoPoint myLocation;
    private FusedLocationProviderClient providerClient;

    //중도 위도 경도
    public static final double DGU_Lat = 37.5578993;
    public static final double DGU_Lng = 126.9992444;

    private TextView mTextView;

    private String diff = "";
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean flag = true;

        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "권한 필요", Toast.LENGTH_SHORT).show();
                    flag = false;
                }
            }
        }
        if (!flag)
            finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        mTextView = findViewById(R.id.textView);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }


        mTextView.setText("테스트");
        providerClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

//        getCurrentLocation();

        GeoPoint Library = new GeoPoint(DGU_Lat, DGU_Lng);
        GeoPoint SIN_GONG = new GeoPoint(37.558060, 126.998319);

        float distance [] = new float[3];
        Location.distanceBetween(DGU_Lat, DGU_Lng, 37.558011, 126.999561 ,distance);


//        for (int i = 0; i < distance.length; i++) {
//            Log.d("MYTAG", i + "번째 distance : " + distance[i]);
//        }

//        Location locationA = new Location("A");
//        locationA.setLatitude(DGU_Lat);
//        locationA.setLongitude(DGU_Lng);
//
//        Location locationB = new Location("B");
//        locationB.setLatitude(37.558011);
//        locationB.setLongitude(126.999561);
//        double disAB = locationA.distanceTo(locationB);
//        double disBA = locationB.distanceTo(locationA);
//        Log.d("MYTAG", "disAB : " + disAB);
//        Log.d("MYTAG", "disBA : " + disBA);

        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                getCurrentLocation();
            }
        };

        Timer timer = new Timer();
        timer.schedule(tt,0,1000 * 10);

    }

    public void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if(chkGpsService()) {
                providerClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        myLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                        Log.d("MYTAG", "getCurrentLocation에서 Location : " + myLocation.getLatitude() + ", " + myLocation.getLongitude());

                        float distance [] = new float[3];
                        Location.distanceBetween(DGU_Lat, DGU_Lng, location.getLatitude(), location.getLongitude() ,distance);
                        diff += Math.round(distance[0]) + "meter 차이"+ '\n';
                        mTextView.setText(diff);


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MYTAG", "Failure : " + e);
                    }
                });
            }
        }
    }
    public boolean chkGpsService() {
        String gps = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (!(gps.matches(".*gps.*") && gps.matches(".*network.*"))) {

            // GPS OFF 일때 Dialog 표시
            AlertDialog.Builder gsDialog = new AlertDialog.Builder(getApplicationContext());
            gsDialog.setTitle("GPS 설정");
            gsDialog.setMessage("설정?");
            gsDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // GPS설정 화면으로 이동
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            }).create().show();
            return false;

        } else {
            return true;
        }
    }
}
