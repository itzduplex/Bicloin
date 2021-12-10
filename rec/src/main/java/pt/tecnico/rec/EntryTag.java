package pt.tecnico.rec;

public class EntryTag implements Comparable<EntryTag> {
    private int seq;
    private int cid;

    public EntryTag(int seq, int cid) {
        this.seq = seq;
        this.cid = cid;
    }

    public int getSeq() {
        return this.seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public int getCid() {
        return this.cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    @Override
    public int compareTo(EntryTag b) {
        if (this.seq > b.seq || (this.seq == b.seq && this.cid > b.cid))
            return 1;
        if (this.seq < b.seq || (this.seq == b.seq && this.cid < b.cid))
            return -1;
        else
            return 0;
    }
}