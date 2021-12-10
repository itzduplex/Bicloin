package pt.tecnico.rec;

import java.util.ArrayList;

public class RecStationEntry {
    private int numberDocks;
    private int numberBicycles;
    private int award;
    private int numberPickups;
    private int numberDrops;
    private ArrayList<EntryTag> tags = new ArrayList<>();

    
    public RecStationEntry(int numberDocks, int numberBicycles, int award) {
        this.numberDocks = numberDocks;
        this.numberBicycles = numberBicycles;
        this.award = award;
        this.numberPickups = 0;
        this.numberDrops = 0;

        for (int i = 0; i < 5; i++) {
            this.tags.add(new EntryTag(0, 0));
        }
    }
    
    public RecStationEntry(int numberDocks, int numberBicycles, int award, int numberPickups, int numberDrops) {
        this.numberDocks = numberDocks;
        this.numberBicycles = numberBicycles;
        this.award = award;
        this.numberPickups = numberPickups;
        this.numberDrops = numberDrops;

        for (int i = 0; i < 5; i++) {
            tags.set(i, new EntryTag(-1, -1));
        }
    }

    public EntryTag getTagByIndex(int index) {
        return this.tags.get(index);
    }

    public void setTagByIndex(int index, EntryTag tag) {
        this.tags.set(index, tag);
    }

    public int getNumberDocks() {
        return this.numberDocks;
    }

    public void setNumberDocks(int numberDocks) {
        this.numberDocks = numberDocks;
    }

    public int getNumberBicycles() {
        return this.numberBicycles;
    }

    public void setNumberBicycles(int numberBicycles) {
        this.numberBicycles = numberBicycles;
    }

    public int getAward() {
        return this.award;
    }

    public void setAward(int award) {
        this.award = award;
    }

    public int getNumberPickups() {
        return this.numberPickups;
    }

    public void setNumberPickups(int numberPickups) {
        this.numberPickups = numberPickups;
    }

    public int getNumberDrops() {
        return this.numberDrops;
    }

    public void setNumberDrops(int numberDrops) {
        this.numberDrops = numberDrops;
    }

    public void setByColumn(int column, int value) {
        switch (column) {
            case 0:
                this.setNumberDocks(value);
                break;
            case 1:
                this.setNumberBicycles(value);
                break;
            case 2:
                this.setAward(value);
                break;
            case 3:
                this.setNumberPickups(value);
                break;
            case 4:
                this.setNumberDrops(value);
                break;
        }
    }

    public int getByColumn(int column) {
        int output = -1;
        switch (column) {
            case 0:
                output = this.getNumberDocks();
                break;
            case 1:
                output = this.getNumberBicycles();
                break;
            case 2:
                output = this.getAward();
                break;
            case 3:
                output = this.getNumberPickups();
                break;
            case 4:
                output = this.getNumberDrops();
                break;
        }
        return output;
    }
    
}
