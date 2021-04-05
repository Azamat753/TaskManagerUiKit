package com.lawlett.taskmanageruikit.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class FireStoreTools extends App {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void readDataFromFireStore(FirebaseFirestore firebaseFirestore,String collectionName) {
        firebaseFirestore.collection(collectionName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Log.d("ololo", document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w("ololo", "Error getting documents.", task.getException());
                        }
                    }
                });
    }
    public static void deleteDataByFireStore(String documentId,String collectionName,FirebaseFirestore firebaseFirestore){
        firebaseFirestore.collection(collectionName).document(documentId).delete();
    }

    public static void updateDataByFireStore(String documentId,String collectionName,FirebaseFirestore firebaseFirestore,Object model) {
        firebaseFirestore.collection(collectionName).document(documentId).set(model);
    }
}
