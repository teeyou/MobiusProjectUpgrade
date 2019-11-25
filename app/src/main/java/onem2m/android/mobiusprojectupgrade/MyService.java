package onem2m.android.mobiusprojectupgrade;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyService extends Service {
    private boolean flag;
    private String deviceId;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MYTAG", "onCreate");
        flag = true;

        TelephonyManager telephonyManager;
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        deviceId = telephonyManager.getDeviceId();
//        foregroundService();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MYTAG", "Destroy");
        flag = false;
        stopForeground(true);
        this.stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        foregroundService(intent.getStringExtra("inORout"));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("devices").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                Log.d("MYTAG", "MyService에서... documents size : " + documents.size());

                boolean isExist = false;
                for (int i = 0; i < documents.size(); i++) {
                    if(documents.get(i).getData().get("deviceID").equals(deviceId)) {
                        isExist = true;
                        break;
                    }
                }
                if(isExist) {
                    new MyAsyncTask().execute();
                } else {
                    Log.d("MYTAG", "isExist = false");
                    //Toast.makeText(getApplicationContext(),"IN 누른 후 실행",Toast.LENGTH_SHORT).show();
                }

            }
        });
        return Service.START_REDELIVER_INTENT;
    }

    public void show_noti() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "intelli_T");
        builder.setContentTitle("히터 끄세요");
        builder.setContentText("실내 온도 20도 이상");
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setAutoCancel(true);

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(1000);
        }

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
        ringtone.play();


        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1000, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new NotificationChannel("intelli_T_notification", "intelli_T_notification", NotificationManager.IMPORTANCE_HIGH));
        }

        manager.notify(1, builder.build());
    }

    public void requestData() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request newRequest  = chain.request().newBuilder()
                                .addHeader("Authorization", MainActivity.authToken)
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
                Result ourData = resultList.get(0);

                long timestamp = (long) ((resultList.get(0).DEVICE_FIELD05 - (9 * 60 * 60)) * 1000 ); //unixtime -> 한국시간으로 변환
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = sdf.format(new Date(timestamp));

                Log.d("MYTAG","scode : " + resultList.get(0).getDEVICE_SCODE());
                Log.d("MYTAG","temperature : " + resultList.get(0).getDEVICE_FIELD01());
                Log.d("MYTAG","humidity : " + resultList.get(0).getDEVICE_FIELD02());
                Log.d("MYTAG","light : " + resultList.get(0).getDEVICE_FIELD03());
                Log.d("MYTAG","10min_moving_count: " + resultList.get(0).getDEVICE_FIELD04());
                Log.d("MYTAG","last_moving_time: " + date);
                Log.d("MYTAG","CO2 : " + resultList.get(0).getDEVICE_FIELD10());
                Log.d("MYTAG","tVOC : " + resultList.get(0).getDEVICE_FIELD11());
                Log.d("MYTAG","data_reg_dtm : " + resultList.get(0).getDEVICE_DATA_REG_DTM());


                if(ourData.getDEVICE_FIELD01() > 20) {
                    show_noti();
                }
            }

            @Override
            public void onFailure(Call<Sensor> call, Throwable t) {
                Log.d("MYTAG", t.getMessage());
            }
        });
    }

    public void foregroundService(String inORout) {
        Log.d("MYTAG", "foregroundService inORout : " + inORout);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "intelli_T"); //오레오부터는 채널id가 필요함
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);

        if(inORout.equals("in")) {
            builder.setContentTitle("센서데이터 5초마다 받아옴");
            builder.setContentText("20도 이상이 되면 알림");
        } else {
            builder.setContentTitle("IN 안눌러서 작동 안함");
            builder.setContentText("터치해서 IN 누르기");
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2000, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(new NotificationChannel("intelli_T", "intelli_T", NotificationManager.IMPORTANCE_LOW ));
        }

        startForeground(2, builder.build());
    }

    class MyAsyncTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            while(flag) {
                requestData();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }
}
