package cn.wen.gobang.util;

public class Movement implements Comparable<Movement>{
    private int score;
    private int position;
    public Movement(int score, int position){
        this.score = score;
        this.position = position;
    }

    public void set(Movement move){
        score = move.score;
        position = move.position;
    }

    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
    public int getX(){
        return Util.getCol(position);
    }
    public int getY(){
        return Util.getRow(position);
    }

    public boolean equals(Object obj) {
        if(obj instanceof Movement){
            Movement move = (Movement) obj;
            return move.position == position;
        }
        return false;
    }

    public Movement clone(){
        return new Movement(score, position);
    }

    public int compareTo(Movement move) {
        return move.score - score;
    }
}
