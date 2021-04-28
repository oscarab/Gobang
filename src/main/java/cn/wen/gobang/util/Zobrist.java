package cn.wen.gobang.util;

public class Zobrist {
    private long checkKey;  // 校验值
    private int score;      // 分值
    private int depth;      // 搜索深度
    private int type;       // 结点类型
    
    public Zobrist(long checkKey, int score, int depth, int type) {
        this.checkKey = checkKey;
        this.score = score;
        this.depth = depth;
        this.type = type;
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
}
