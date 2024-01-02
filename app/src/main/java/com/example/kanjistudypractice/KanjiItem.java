package com.example.kanjistudypractice;

import java.util.List;

public class KanjiItem {

    String character;
    String meanings;
    String jlpt_new;

    public KanjiItem(){}

    public KanjiItem(String character, String meanings, String jlpt_new) {
        this.character = character;
        this.meanings = meanings;
        this.jlpt_new = jlpt_new;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public String getMeanings() {
        return meanings;
    }

    public void setMeanings(String meanings) {
        this.meanings = meanings;
    }

    public String getJlpt_new() {
        return jlpt_new;
    }

    public void setJlpt_new(String jlpt_new) {
        this.jlpt_new = jlpt_new;
    }

}
