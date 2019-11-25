package onem2m.android.mobiusprojectupgrade;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Firestore {
    List<DocumentSnapshot> documentList;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    Firestore() {
//        documentList = new ArrayList<>();
    }

    public void fetchAllDocuments(FirestoreListener<Boolean> callback) {
        db.collection("devices").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                documentList = task.getResult().getDocuments();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    public void addDeviceId(String device_id) {
        Map<String, Object> data = new HashMap<>();
        data.put("deviceID", device_id);
        db.collection("devices").document().set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("MYTAG", "addDeviceId 완료");
            }
        });
    }
    public List<DocumentSnapshot> getDeviceList() {
        return documentList;
    }
}
