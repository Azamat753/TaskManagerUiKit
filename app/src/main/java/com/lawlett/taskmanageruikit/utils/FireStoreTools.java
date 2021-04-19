package com.lawlett.taskmanageruikit.utils;

import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class FireStoreTools extends App {

    @Override
    public void onCreate() {
        super.onCreate();
    }


    public static void deleteDataByFireStore(String documentId, String collectionName, FirebaseFirestore firebaseFirestore, ProgressBar progressBar){
        firebaseFirestore.collection(collectionName).document(documentId).delete().addOnSuccessListener(aVoid -> progressBar.setVisibility(View.GONE));
    }

    public static void writeOrUpdateDataByFireStore(String documentId, String collectionName, FirebaseFirestore firebaseFirestore, Object model) {
        firebaseFirestore.collection(collectionName).document(documentId).set(model);
    }
}
