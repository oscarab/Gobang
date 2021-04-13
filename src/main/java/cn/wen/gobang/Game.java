package cn.wen.gobang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.wen.gobang.AI.HashTable;
import cn.wen.gobang.AI.Movement;
import cn.wen.gobang.AI.Util;

public class Game {
    private int[][] GameMap = new int[15][15];
    private int[][] gameOut = new int[15][15];
    private int currentRole = 1;    // 当前执子的颜色 1黑2白
    private int[][][] dir = {{{-1,0},{1,0}}, {{0,1},{0,-1}}, {{-1,-1},{1,1}}, {{-1,1},{1,-1}}};

    private List<Movement> hasChess = new ArrayList<Movement>();
    private int hasChessCnt = 0;
    private Movement bestMove;

    private HashTable hashTable;

    public static final int NONE_SCORE = -2000000000;
    public static final Movement NONE_MOVE = new Movement(-1, -1, -1);
    public static final int MAX_SCORE = 1000000000;
    public static final int WIN_SCORE = MAX_SCORE - 24;
    private int step;
    private int maxDepth = 6;

    public Game(int difficulty){
        this.maxDepth = difficulty;
        hashTable = new HashTable();
    }

    public void setMaxStep(int val){
        maxDepth = val;
    }

    public Movement getLastMove(){
        return bestMove;
    }

    public int get(int x, int y){
        return gameOut[x][y];
    }

    public boolean directMove(int x, int y){
        gameOut[x][y] = currentRole;
        return makeMove(x, y);
    }

    public boolean AIthink(){
        int score = 0;
        step = 0;
        hashhit = 0;
        hashTable.clear();
        long start = System.currentTimeMillis();

        for(maxDepth = 1; maxDepth <= 6; maxDepth++){
            score = alphaBetaSearch(maxDepth, NONE_SCORE, -NONE_SCORE);
            System.out.println((System.currentTimeMillis() - start) + "ms");
            if(score > WIN_SCORE) break;
        }
        System.out.println("best score:" + score);
        gameOut[bestMove.getX()][bestMove.getY()] = currentRole;

        System.out.println(hashhit);
        
        return makeMove(bestMove.getX(), bestMove.getY());
    }

    private boolean isWin(int x, int y){
        for(int a = 0; a < 4; a++){
            int line = 1;
            for(int b = 0; b < 2; b++){
                for(int cnt = 1; cnt <= 4; cnt++){
                    int x1 = x + dir[a][b][0]*cnt;
                    int y1 = y + dir[a][b][1]*cnt;
                    if(x1 < 0 || x1 > 14 || y1 < 0 || y1 > 14)
                        break;
                    if(GameMap[x1][y1] == GameMap[x][y])
                        line++;
                    else
                        break;
                }
            }
            if(line >= 5)
                return true;
        }
        return false;
    }

    //评估当前一方局势
    private int evaluate(){
        int res = 0;
        for(int i = 0; i < hasChessCnt; i++){
            int x = hasChess.get(i).getX();
            int y = hasChess.get(i).getY();
    
            for(int a = 0; a < 4; a++){     //四个方向
                short block = 0;            //被堵住的数量
                short line = 1;             //连子数量
                for(int b = 0; b < 2; b++){
                    for(int cnt = 1; cnt <= 4; cnt++){
                        if(x + dir[a][b][0]*cnt < 0 || y + dir[a][b][1]*cnt < 0 || x + dir[a][b][0]*cnt > 14 || y + dir[a][b][1]*cnt > 14){
                            block++;
                            break;
                        }
                        if(GameMap[x + dir[a][b][0]*cnt][y + dir[a][b][1]*cnt] == GameMap[x][y]){    //连子
                            line++;
                        }
                        else if(GameMap[x + dir[a][b][0]*cnt][y + dir[a][b][1]*cnt] == Util.getOpponent(GameMap[x][y])){  //堵塞
                            block++;
                            break;
                        }
                        else{       //留空
                            break;
                        }
                    }
                }
                if(GameMap[x][y] != currentRole)
                    res -= Util.getScore(line, block);
                else
                    res += Util.getScore(line, block);
            }
        }
        return res;
    }

    //评估单个位置
    private int evaluate(int x, int y, int role){
        int res = 0;
        for(int a = 0; a < 4; a++){
            short block = 0;            //被堵住的数量
            short line = 1;             //连子数量
            for(int b = 0; b < 2; b++){
                for(int cnt = 1; cnt <= 4; cnt++){
                    if(x + dir[a][b][0]*cnt < 0 || y + dir[a][b][1]*cnt < 0 || x + dir[a][b][0]*cnt > 14 || y + dir[a][b][1]*cnt > 14){
                        block++;
                        break;
                    }
                    if(GameMap[x + dir[a][b][0]*cnt][y + dir[a][b][1]*cnt] == role){    //连子
                        line++;
                    }
                    else if(GameMap[x + dir[a][b][0]*cnt][y + dir[a][b][1]*cnt] == Util.getOpponent(role)){  //堵塞
                        block++;
                        break;
                    }
                    else{       //留空
                        break;
                    }
                }
            }
            res += Util.getScore(line, block);
        }
        return res;
    }

    //生成可下子位置
    private List<Movement> generate(){
        List<Movement> moveLists = new ArrayList<>();
        boolean visit[][] = new boolean[15][15];

        for(int i = 0; i < hasChessCnt; i++){
            Movement p = hasChess.get(i);
            int x = p.getX(), y = p.getY();
            for(int a = 0; a < 4; a++){
                for(int b = 0; b < 2; b++){
                    for(int cnt = 1; cnt <= 2; cnt++){
                        int x1 = x + dir[a][b][0]*cnt;
                        int y1 = y + dir[a][b][1]*cnt;
                        if(x1 < 0 || y1 < 0 || x1 > 14 || y1 > 14)
                            break;
    
                        if(GameMap[x1][y1] == 0 && !visit[x1][y1]){
                            visit[x1][y1] = true;
                            Movement move = new Movement(evaluate(x1, y1, currentRole) - evaluate(x1, y1, Util.getOpponent(currentRole)), x1, y1);
                            moveLists.add(move);
                        }
                    }
                }
            }
        }
        return moveLists;
    }

    private boolean makeMove(int x, int y){
        step++;
        GameMap[x][y] = currentRole;
        hasChessCnt++;
        hasChess.add(new Movement(0, x, y));
        hashTable.update(x, y, currentRole);

        currentRole = currentRole == 1? 2:1;
        return isWin(x, y);
    }

    private void unMakeMove(int x, int y){
        step--;
        GameMap[x][y] = 0;
        hasChess.remove(hasChessCnt - 1);
        hasChessCnt--;
        hashTable.update(x, y, currentRole);

        currentRole = currentRole == 1? 2:1;
    }

    int hashhit = 0;
    private int alphaBetaSearch(int depth, int alpha, int beta){
        int score = 0;
        int best;
        Movement move = NONE_MOVE.clone(), gooMove = NONE_MOVE.clone();
        List<Movement> moveLists = new ArrayList<>();

        score = hashTable.readHashTable(depth, step, alpha, beta, move);
        if(score != NONE_SCORE){
            hashhit++;
            return score;
        }

        best = NONE_SCORE;
        boolean isAlpha = true;

        if(depth <= 0){
            score = evaluate();
            hashTable.saveHashTable(depth, step, score, HashTable.hashExact, NONE_MOVE);
            return score;
        }

        if(!move.equals(NONE_MOVE) && GameMap[move.getX()][move.getY()] == 0) {
            Movement temp = move.clone();
            temp.setScore(MAX_SCORE);
            moveLists.add(temp);
        }
        moveLists.addAll(generate());
        Collections.sort(moveLists);
        int moveNum = moveLists.size();

        for(int i = 0; i < moveNum; i++){
            move = moveLists.get(i);
            if(makeMove(move.getX(), move.getY())){
                score = MAX_SCORE - step;
            }
            else{
                score = -alphaBetaSearch(depth - 1, -beta, -alpha);
            }
            unMakeMove(move.getX(), move.getY());

            if(score >= beta){
                hashTable.saveHashTable(depth, step, beta, HashTable.hashBeta, move);
                return beta;
            }
            if(score > best){
                best = score;
                gooMove = move;
                if(score > alpha){
                    isAlpha = false;
                    alpha = score;
                    // 若为第一层，传出着法
                    if(depth == maxDepth)
                        bestMove = move;
                }
            }
        }

        if(isAlpha){
            hashTable.saveHashTable(depth, step, alpha, HashTable.hashAlpha, gooMove);
        }
        else{
            hashTable.saveHashTable(depth, step, best, HashTable.hashExact, gooMove);
        }
        return alpha;
    }

}
