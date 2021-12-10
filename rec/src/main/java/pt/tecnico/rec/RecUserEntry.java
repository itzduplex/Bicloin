package pt.tecnico.rec;

import java.util.ArrayList;

public class RecUserEntry {
    private int balance;
    private int hasBike;
    private ArrayList<EntryTag> tags = new ArrayList<>();

    public RecUserEntry(int balance, int hasBike) {
        this.balance = balance;
        this.hasBike = hasBike;

        for (int i = 0; i < 2; i++) {
            this.tags.add(new EntryTag(0, 0));
        }
    }

    public EntryTag getTagByIndex(int index) {
        return this.tags.get(index);
    }

    public void setTagByIndex(int index, EntryTag tag) {
        this.tags.set(index, tag);
    }

    public int getBalance() {
        return this.balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
    
    public int getHasBike() {
        return this.hasBike;
    }

    public void setHasBike(int hasBike) {
        this.hasBike = hasBike;
    }

    public void setByColumn(int column, int value) {
        switch (column) {
            case 0:
                this.setBalance(value);
                break;
            case 1:
                this.setHasBike(value);
                break;
        }
    }

    public int getByColumn(int column) {
        int output = -1;
        switch (column) {
            case 0:
                output = this.getBalance();
                break;
            case 1:
                output = this.getHasBike();
                break;
        }
        return output;
    }
}