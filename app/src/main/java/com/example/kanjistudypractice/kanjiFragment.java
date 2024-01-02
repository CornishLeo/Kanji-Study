package com.example.kanjistudypractice;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class kanjiFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<KanjiItem> kanjiItemArrayList;
    KanjiAdapter kanjiAdapter;
    FirebaseFirestore db;

    ProgressBar progressBar;


    public kanjiFragment() {
        // Required empty public constructor
    }

    public static kanjiFragment newInstance() {
        kanjiFragment fragment = new kanjiFragment();
        Bundle args = new Bundle();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_kanji, container, false);

        // Find views within the inflated layout
        progressBar = rootView.findViewById(R.id.progressBar);
        recyclerView = rootView.findViewById(R.id.kanjiList);

        // Setup RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize other variables and adapters
        db = FirebaseFirestore.getInstance();
        kanjiItemArrayList = new ArrayList<>();
        kanjiAdapter = new KanjiAdapter(requireContext(), kanjiItemArrayList);
        recyclerView.setAdapter(kanjiAdapter);


        // Call your method to fetch data
        KanjiChangeListener();

        return rootView;
    }

    private void KanjiChangeListener() {

        Integer buttonNumber = getArguments().getInt("buttonNumber", -1) - 1;

        if (buttonNumber == 0) {
            buttonNumber = null;
        }

        db.collection("kanji")
                .whereEqualTo("jlpt_new", buttonNumber)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot value) {
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                String kanji = dc.getDocument().getId(); // Assuming 'kanji' is the document key

                                List<String> meaningArray = (List<String>) dc.getDocument().get("meanings");
                                String meaning = (meaningArray != null && meaningArray.size() > 0) ? meaningArray.get(0) : "";
                                Long jlptLong = dc.getDocument().getLong("jlpt_new");

                                String jlptString;
                                if (jlptLong != null) {
                                    int jlpt = jlptLong.intValue();
                                    jlptString = String.valueOf(jlpt);
                                } else {
                                    // If "jlpt_new" is null, set a default string, for example, "None"
                                    jlptString = "None";
                                }

                                KanjiItem kanjiItem = new KanjiItem(kanji, meaning, jlptString);
                                kanjiItemArrayList.add(kanjiItem);
                            }
                        }

                        kanjiAdapter.notifyDataSetChanged();
                        if (progressBar.getVisibility() == View.VISIBLE)
                            progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Database failure", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}