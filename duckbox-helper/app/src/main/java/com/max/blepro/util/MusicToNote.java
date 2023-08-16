package com.max.blepro.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MusicToNote {
    private List<Note> notes = new ArrayList<>();
    public static final String[] NOTE_NAMES = {
            "NOTE_C4", "NOTE_CS4", "NOTE_D4", "NOTE_DS4", "NOTE_E4",
            "NOTE_F4", "NOTE_FS4", "NOTE_G4", "NOTE_GS4", "NOTE_A4",
            "NOTE_AS4", "NOTE_B4", "NOTE_C5", "NOTE_CS5", "NOTE_D5", "NOTE_DS5"};

    private static final HashMap<String, Integer> NOTE_FREQUENCIES = new HashMap<>();

    static {
        NOTE_FREQUENCIES.put("NOTE_C4", 261);
        NOTE_FREQUENCIES.put("NOTE_CS4", 277);
        NOTE_FREQUENCIES.put("NOTE_D4", 293);
        NOTE_FREQUENCIES.put("NOTE_DS4", 311);
        NOTE_FREQUENCIES.put("NOTE_E4", 329);
        NOTE_FREQUENCIES.put("NOTE_F4", 349);
        NOTE_FREQUENCIES.put("NOTE_FS4", 369);
        NOTE_FREQUENCIES.put("NOTE_G4", 392);
        NOTE_FREQUENCIES.put("NOTE_GS4", 415);
        NOTE_FREQUENCIES.put("NOTE_A4", 440);
        NOTE_FREQUENCIES.put("NOTE_AS4", 466);
        NOTE_FREQUENCIES.put("NOTE_B4", 493);
        NOTE_FREQUENCIES.put("NOTE_C5", 523);
        NOTE_FREQUENCIES.put("NOTE_CS5", 554);
        NOTE_FREQUENCIES.put("NOTE_D5", 587);
        NOTE_FREQUENCIES.put("NOTE_DS5", 622);
    }

    public void addNote(String name, int duration) {
        Note newNote = new Note(name, duration);
        notes.add(newNote);
    }

    public void clearNotes() {
        this.notes.clear();
    }

    public List<Note> getNotes() {
        return notes;
    }

    public class Note {
        private String name;
        private int duration;

        public Note(String name, int duration) {
            this.name = name;
            this.duration = duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public String getName() {
            return name;
        }

        public int getDuration() {
            return duration;
        }

        public String getInfo() {
            String res = "[" + NOTE_FREQUENCIES.get(getName()) + ", " + getDuration() + "]";
            Log.d("M", res);
            return res;
        }
    }
}