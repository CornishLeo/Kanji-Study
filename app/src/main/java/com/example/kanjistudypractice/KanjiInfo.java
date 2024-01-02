package com.example.kanjistudypractice;

import static android.app.PendingIntent.getActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class KanjiInfo extends AppCompatActivity {

    TextView kanjiCharacter;
    TextView mainMeaning;
    TextView jlptLevel;
    FlexboxLayout otherMeaningLayoutAdding;
    LinearLayout otherMeaningLayout;
    TextView otherMeaningTitle;

    private FirebaseFirestore db;

    String chosenKanji;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kanji_info);

        db = FirebaseFirestore.getInstance();

        kanjiCharacter = findViewById(R.id.kanjiCharacter);
        mainMeaning = findViewById(R.id.mainMeaning);
        jlptLevel = findViewById(R.id.jlptLevel);
        otherMeaningLayoutAdding = findViewById(R.id.otherMeaningLayoutToAdd);
        otherMeaningLayout = findViewById(R.id.otherMeaningLayout);
        otherMeaningTitle= findViewById(R.id.otherMeaningTitle);

        Intent intent = getIntent();
        if (intent != null) {
            chosenKanji = intent.getStringExtra("kanji");
        }

        KanjiChangeListener();
    }

    private void KanjiChangeListener() {

        db.collection("kanji")
                .document(chosenKanji)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String kanji = documentSnapshot.getId(); // Assuming 'kanji' is the document key
                            List<String> meaningArray = (List<String>) documentSnapshot.get("meanings");
                            String meaning = (meaningArray != null && meaningArray.size() > 0) ? meaningArray.get(0) : "";
                            long jlptLong = documentSnapshot.getLong("jlpt_new");
                            int jlpt = (int) jlptLong; // Convert to int

                            String Sjlpt = String.valueOf(jlpt);

                            // Assuming you have TextView instances for displaying Kanji, meaning, and JLPT level
                            kanjiCharacter.setText(kanji);
                            mainMeaning.setText(meaning);
                            jlptLevel.setText(Sjlpt);


                            if (meaningArray.size() <= 1) {
                                // Set visibility to GONE
                                otherMeaningTitle.setVisibility(View.GONE);
                            } else {
                                for (int i = 1; i < meaningArray.size(); i++) {
                                    String otherMeaning = meaningArray.get(i);
                                    addMeaningTextView(otherMeaning);
                                }
                            }


                        } else {
                            Toast.makeText(KanjiInfo.this, "Document not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Database failure", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addMeaningTextView(String meaning) {
        TextView meaningTextView = new TextView(this);
        meaningTextView.setText(meaning);

        // You can customize the appearance of the TextView here (e.g., padding, text size, etc.)

        meaningTextView.setTextSize(25);
        meaningTextView.setBackgroundResource(R.drawable.rectangle);
        meaningTextView.setPadding(10, 10, 10, 10);
        FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
        );

        layoutParams.setMargins(10, 10, 10, 10);

        meaningTextView.setLayoutParams(layoutParams);

        otherMeaningLayoutAdding.addView(meaningTextView);
    }

}