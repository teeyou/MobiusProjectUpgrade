package onem2m.android.mobiusprojectupgrade;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    public static final String authToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6ImIxM2M3YzhmMzYwMDAzNGExZDVhNDkzZWI5NWVkZGY4MDIwMzI4YzU4ZGM1ODMxY2JhYWI5YTU1ZTE2YTA4YTk5YWUyNzVmYmVlM2NlYTc2In0.eyJhdWQiOiIxIiwianRpIjoiYjEzYzdjOGYzNjAwMDM0YTFkNWE0OTNlYjk1ZWRkZjgwMjAzMjhjNThkYzU4MzFjYmFhYjlhNTVlMTZhMDhhOTlhZTI3NWZiZWUzY2VhNzYiLCJpYXQiOjE1NzI0Mjc0NTAsIm5iZiI6MTU3MjQyNzQ1MCwiZXhwIjoxNTg4MjM4NjUwLCJzdWIiOiIxMDAwMDAwMDAwMSIsInNjb3BlcyI6W119.IQj7AjsyRpX9Y8jJI2HJJOL221m95YRbbbX_VpvH-Nfb2NjF6w1E43qbv7tzLJqOPlsz0OkzmEDbp0405FMMan8K8Z1NdBhjaRPFDAdCaosudMUZXsovOP0buJWtoR-pcaG5MQ46wVbjBeSBJFqMzDgSrFQyjf_71Tk0MH4JLVPQVyVuTKdh_a3AWYi0BOAf6Mu31erd7i0ArkOSXeRvGnsh64qWHMuoLThy83wN7D2eTnKqHeOAbhXIJhRYWJrLI0pEzsQTy1-TC0oftKntAVVJIFx2HTOyHnCacgA2MVv8SKDu_Y6ZAoFkDv9t0KjsB7ZQKesoGUA5VHDOVdyQvtivCaNBJRLqF6r6DJhM8qP4AyDooZ5x9kfBV607MeKGm6dSFx-2EBKyqB9HSyjEBq-kD5S_iJ4Vw7MGHsh8qHjivUNXMYXcY70jktfk-OMeQ4EZz1J5WMur1jsU4rTaVFipWaF7l4-Q4kfsnBS4nMt6Gq3mCFgjEkgF0QfhpPYiNEUcpmUqG61wfgl1TQ6q2OPvYtpsxVff89TLvXriV0CfBePlw6rfr3hg8wZnkH0P7BirGA6RfTHDlXOG6432528pgZeowYpJtQBmey1iP7P1aQGmIeeeWrI2RbM8Eat_oQMoT0RShx66lmKlg8zxaXsDDSWcfdYlRC53s_0RfNE";
    private static final int REQUEST_CODE = 1000;

    private TextView scode;
    private TextView temperature;
    private TextView humidity;
    private TextView light;
    private TextView moving_count_ten;
    private TextView last_moving_time;
    private TextView co2;
    private TextView tVOC;
    private TextView data_reg_dtm;
    private TextView inORout;
    private Button mButton;

    public static List<Result> sensorDataList = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String deviceId;
    private String doc_id;

    private Button btn_in;
    private Button btn_out;

    private ProgressBar mProgressBar;

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
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE);
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String id = pref.getString("doc_id",null);
        if(id != null) {
            doc_id = id;
            Log.d("MYTAG", "pref doc_id : " + doc_id);
        }

        TelephonyManager telephonyManager;
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        deviceId = telephonyManager.getDeviceId();

        scode = findViewById(R.id.divce_scode);
        temperature = findViewById(R.id.temperature);
        humidity = findViewById(R.id.humidity);
        light = findViewById(R.id.light);
        data_reg_dtm = findViewById(R.id.data_reg_dtm);
        mButton = findViewById(R.id.btn);
        moving_count_ten = findViewById(R.id.moving_count_ten);
        last_moving_time = findViewById(R.id.last_moving_time);
        co2 = findViewById(R.id.co2);
        tVOC = findViewById(R.id.tVOC);

        btn_in = findViewById(R.id.btn_in);
        btn_out = findViewById(R.id.btn_out);
        inORout = findViewById(R.id.inORout);

        mProgressBar = findViewById(R.id.progress_bar);

        if(doc_id == null) {
            inORout.setText("강의실 밖");
        } else {
            inORout.setText("강의실 안");
        }

        requestData();

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestData();
            }
        });

        btn_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                db.collection("devices").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        Log.d("MYTAG", "size : " + documents.size());

                        boolean isExist = false;

                        for (int i = 0; i < documents.size(); i++) {
                            if (documents.get(i).getData().get("deviceID").equals(deviceId)) {
                                doc_id = documents.get(i).getId();
                                isExist = true;
                                break;
                            }
                        }

                        if (!isExist) {
                            Map<String, Object> user = new HashMap<>();
                            user.put("deviceID", deviceId);
                            db.collection("devices").add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    doc_id = documentReference.getId();

                                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("doc_id", doc_id);
                                    editor.apply();

                                    Log.d("MYTAG", "아이디 없어서 저장 성공");
                                    inORout.setText("강의실 안");

                                    Intent intent = new Intent(getApplicationContext(), MyService.class);
                                    String inORout = "out";
                                    if(doc_id != null)
                                        inORout = "in";

                                    intent.putExtra("inORout", inORout);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        startForegroundService(intent);
                                    } else {
                                        startService(intent);
                                    }

                                    mProgressBar.setVisibility(View.INVISIBLE);
                                }
                            });

                        } else {
                            Log.d("MYTAG", "이미 존재!");
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });

        btn_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MYTAG", "doc_id : " + doc_id);
                if (doc_id != null) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    db.collection("devices").document(doc_id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("MYTAG", "삭제 성공");
                            doc_id = null;

                            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("doc_id", doc_id);
                            editor.apply();

                            inORout.setText("강의실 밖");
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                    Intent intent = new Intent(getApplicationContext(), MyService.class);
                    Log.d("MYTAG", "stopService");
                    stopService(intent);
                }
            }
        });
    }

    public void requestData() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("Authorization", authToken)
                                .build();
                        return chain.proceed(newRequest);
                    }
                }).build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(API.BASEURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        API api = retrofit.create(API.class);
        api.getAllData().enqueue(new Callback<Sensor>() {
            @Override
            public void onResponse(Call<Sensor> call, retrofit2.Response<Sensor> response) {
                Log.d("MYTAG", response.body().getCode());
                Log.d("MYTAG", response.body().getMessage());

                List<Result> resultList = response.body().getResult();
                sensorDataList.addAll(resultList);

                long timestamp = (long) ((resultList.get(0).DEVICE_FIELD05 - (9 * 60 * 60)) * 1000); //unixtime -> 한국시간으로 변환

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = sdf.format(new Date(timestamp));

                scode.setText("scode : " + resultList.get(0).getDEVICE_SCODE());
                temperature.setText("temperature : " + resultList.get(0).getDEVICE_FIELD01());
                humidity.setText("humidity : " + resultList.get(0).getDEVICE_FIELD02());
                light.setText("light : " + resultList.get(0).getDEVICE_FIELD03());
                moving_count_ten.setText("10min_moving_count: " + resultList.get(0).getDEVICE_FIELD04());
                last_moving_time.setText("last_moving_time: " + date);
                co2.setText("CO2 : " + resultList.get(0).getDEVICE_FIELD10());
                tVOC.setText("tVOC : " + resultList.get(0).getDEVICE_FIELD11());
                data_reg_dtm.setText("data_reg_dtm : " + resultList.get(0).getDEVICE_DATA_REG_DTM());
            }

            @Override
            public void onFailure(Call<Sensor> call, Throwable t) {
                Log.d("MYTAG", t.getMessage());
            }
        });
    }
}
