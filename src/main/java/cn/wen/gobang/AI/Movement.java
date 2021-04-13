package cn.wen.gobang.AI;

public class Movement implements Comparable<Movement>{
    private int score;
    private int x, y;
    public Movement(int score, int x, int y){
        this.score = score;
        this.x = x;
        this.y = y;
    }

    public void set(Movement move){
        score = move.score;
        x = move.x;
        y = move.y;
    }

    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }

    public boolean equals(Object obj) {
        if(obj instanceof Movement){
            Movement move = (Movement) obj;
            return move.x == x && move.y == y;
        }
        return false;
    }

    public Movement clone(){
        return new Movement(score, x, y);
    }

    public int compareTo(Movement move) {
        return move.score - score;
    }
}
