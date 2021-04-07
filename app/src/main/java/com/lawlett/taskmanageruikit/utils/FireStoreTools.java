package com.lawlett.taskmanageruikit.utils;

import com.google.firebase.firestore.FirebaseFirestore;

public class FireStoreTools extends App {

    @Override
    public void onCreate() {
        super.onCreate();
    }


    public static void deleteDataByFireStore(String documentId,String collectionName,FirebaseFirestore firebaseFirestore){
        firebaseFirestore.collection(collectionName).document(documentId).delete();
    }

    public static void writeOrUpdateDataByFireStore(String documentId, String collectionName, FirebaseFirestore firebaseFirestore, Object model) {
        firebaseFirestore.collection(collectionName).document(documentId).set(model);
    }
}
