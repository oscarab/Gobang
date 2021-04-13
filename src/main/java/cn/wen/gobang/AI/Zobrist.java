package cn.wen.gobang.AI;

public class Zobrist {
    private long checkKey;
    private int score;
    private int depth;
    private int type;
    private Movement goodMove;
    
    public Zobrist(long checkKey, int score, int depth, int type, Movement goodMove) {
        this.checkKey = checkKey;
        this.score = score;
        this.depth = depth;
        this.type = type;
        this.goodMove = goodMove;
    }
    public long getCheckKey() {
        return checkKey;
    }
    public void setCheckKey(long checkKey) {
        this.checkKey = checkKey;
    }
    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public int getDepth() {
        return depth;
    }
    public void setDepth(int depth) {
        this.depth = depth;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public Movement getGoodMove() {
        return goodMove;
    }
    public void setGoodMove(Movement goodMove) {
        this.goodMove = goodMove;
    }
}
