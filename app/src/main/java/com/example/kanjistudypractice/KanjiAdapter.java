package com.example.kanjistudypractice;

import android.content.Context;
import android.content.Intent;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class KanjiAdapter extends RecyclerView.Adapter<KanjiAdapter.KanjiViewHolder> {

    Context context;
    ArrayList<KanjiItem> kanjiArrayList;
    SparseBooleanArray selectedItems = new SparseBooleanArray();

    public KanjiAdapter(Context context, ArrayList<KanjiItem> kanjiArrayList) {
        this.context = context;
        this.kanjiArrayList = kanjiArrayList;
    }

    @NonNull
    @Override
    public KanjiAdapter.KanjiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.kanji_item, parent, false);
        return new KanjiViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull KanjiAdapter.KanjiViewHolder holder, int position) {
        KanjiItem kanjiItem = kanjiArrayList.get(position);

        holder.character.setText(kanjiItem.character);
        holder.meanings.setText(kanjiItem.meanings);
        holder.jlpt_new.setText(String.valueOf(kanjiItem.jlpt_new));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KanjiItem clickedKanji = kanjiArrayList.get(holder.getAdapterPosition());

                Intent intent = new Intent(context, KanjiInfo.class);

                intent.putExtra("kanji", clickedKanji.getCharacter());

                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return kanjiArrayList.size();
    }

    public static class KanjiViewHolder extends RecyclerView.ViewHolder {

        TextView character, meanings, jlpt_new;

        public KanjiViewHolder(@NonNull View itemView) {
            super(itemView);

            character = itemView.findViewById(R.id.kanjiTextView);
            meanings = itemView.findViewById(R.id.tveditmeaning);
            jlpt_new = itemView.findViewById(R.id.tveditjlpt);
        }
    }
}
