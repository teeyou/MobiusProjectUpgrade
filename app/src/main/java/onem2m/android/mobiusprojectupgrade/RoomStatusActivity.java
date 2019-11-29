package onem2m.android.mobiusprojectupgrade;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomStatusActivity extends AppCompatActivity {
    private List<String> mList;
    private TextView mStatus;

    private String student = "";
    private int count = 0;
    private int size = 0;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_status);

        mStatus = findViewById(R.id.textView_status);

        mList = new ArrayList<>();

        mProgressBar = findViewById(R.id.progress_bar);


        mProgressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("devices").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                size = list.size();
                for (int i = 0; i < list.size(); i++) {
                    String id = (String) list.get(i).getData().get("deviceId");
                    mList.add(id);
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("userInfo").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot != null) {
                                String name = (String) documentSnapshot.get("name");
                                String number = (String) documentSnapshot.get("number");
                                count++;

                                student += count + ". " + "학번 : " + number + "  이름 : " + name + '\n';
                                mStatus.setText(student);
                            }
                        }
                    });
                }
            }
        });

        while(size != count) {

        }

        mProgressBar.setVisibility(View.INVISIBLE);
    }
}
