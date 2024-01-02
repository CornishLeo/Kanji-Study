package com.example.kanjistudypractice;

import static com.google.common.reflect.Reflection.getPackageName;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;


public class JlptChooser extends Fragment {

    View rootView;

    FirebaseFirestore db;

    public JlptChooser() {
        // Required empty public constructor
    }


    public static JlptChooser newInstance() {
        JlptChooser fragment = new JlptChooser();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_jlpt_chooser, container, false);
        db = FirebaseFirestore.getInstance();

        updateButtonText("6", "五", "JLPT 5", "79");
        updateButtonText("5", "四", "JLPT 4", "166");
        updateButtonText("4", "三", "JLPT 3", "367");
        updateButtonText("3", "二", "JLPT 2", "367");
        updateButtonText("2", "一", "JLPT 1", "1232");
        updateButtonText("1", "他", "Other", "787");

        setOnClickListeners();

        // Return the rootView, not a new instance of the layout resource
        return rootView;
    }


    private void updateButtonText(String buttonIndex, String characterText, String jlptLabelText, String size) {
        int resourceId = getResources().getIdentifier("button" + buttonIndex, "id", getContext().getPackageName());
        View button = rootView.findViewById(resourceId);

        if (button != null) {
            TextView characterTextView = button.findViewById(R.id.character);
            TextView jlptLabelTextView = button.findViewById(R.id.jlptLabel);
            TextView jlptSizeTextView = button.findViewById(R.id.jlptSize);

            if (characterTextView != null && jlptLabelTextView != null) {
                characterTextView.setText(characterText);
                jlptLabelTextView.setText(jlptLabelText);
                jlptSizeTextView.setText(size);
            }
        }
    }

    private void setOnClickListeners() {
        for (int i = 1; i <= 6; i++) {
            final int buttonNumber = i;
            int resourceId = getResources().getIdentifier("button" + i, "id", getContext().getPackageName());
            View button = rootView.findViewById(resourceId);

            if (button != null) {
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        kanjiFragment nextFragment = new kanjiFragment();

                        // Pass the button number to the next fragment
                        Bundle args = new Bundle();
                        args.putInt("buttonNumber", buttonNumber);
                        nextFragment.setArguments(args);

                        // Replace the current fragment with the next fragment
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragmentContainer, nextFragment)
                                .addToBackStack(null)
                                .commit();
                    }

                });
            }
        }
    }

}