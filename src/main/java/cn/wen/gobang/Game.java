package cn.wen.gobang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.wen.gobang.AI.HashTable;
import cn.wen.gobang.AI.Movement;
import cn.wen.gobang.AI.Util;

public class Game {
    private int[] GameMap = new int[256];
    private int[][] gameOut = new int[15][15];

    // 位行 位列 位斜 只保存棋子的存在信息
    private int[][] bitRow = new int[3][15];
    private int[][] bitCol = new int[3][15];
    private int[][] bitBiasLeft = new int[3][30];
    private int[][] bitBiasRight = new int[3][30];

    // 保存棋子的完全信息
    private StringBuilder[] strRow = new StringBuilder[15];
    private StringBuilder[] strCol = new StringBuilder[15];
    private StringBuilder[] strBiasLeft = new StringBuilder[30];
    private StringBuilder[] strBiasRight = new StringBuilder[30];
    private int currentRole = 1;                    // 当前执子的颜色 1黑2白

    private int[] rowScore = new int[15];
    private int[] colScore = new int[15];
    private int[] biasLeftScore = new int[30];
    private int[] biasRightScore = new int[30];
    private int currentScore;

    private List<Movement> hasChess = new ArrayList<Movement>();
    private int hasChessCnt = 0;
    private Movement bestMove;

    private HashTable hashTable;

    public static final int NONE_SCORE = -2000000000;
    public static final Movement NONE_MOVE = new Movement(-1, -1);
    public static final int MAX_SCORE = 1000000000;
    public static final int WIN_SCORE = MAX_SCORE - 24;
    private int step;
    private int maxDepth = 4;

    public Game(int difficulty){
        this.maxDepth = difficulty;
        hashTable = new HashTable();

        for(int role = 0; role < 3; role++){
            for(int i = 0; i < 14; i++){
                bitBiasLeft[role][i] = 1 >> (i + 1);
                bitBiasLeft[role][28 - i] = 1 >> (i + 1);
                bitBiasRight[role][i] = 1 >> (i + 1);
                bitBiasRight[role][28 - i] = 1 >> (i + 1);
            }
        }

        for(int i = 0; i < 15; i++){
            strRow[i] = new StringBuilder("000000000000000");
            strCol[i] = new StringBuilder("000000000000000");
        }
        for(int i = 0; i < 30; i++){
            strBiasLeft[i] = new StringBuilder("000000000000000");
            strBiasRight[i] = new StringBuilder("000000000000000");
        }
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
        return makeMove((y << 4) + x);
    }

    public boolean AIthink(){
        int score = 0;
        step = 0;
        hashhit = 0;
        all_beta = 0;
        all_node = 0;
        hashTable.clear();
        long start = System.currentTimeMillis();

        for(maxDepth = 1; maxDepth <= 6; maxDepth++){
            score = alphaBetaSearch(maxDepth, NONE_SCORE, -NONE_SCORE);
            System.out.println((System.currentTimeMillis() - start) + "ms");
            if(score > WIN_SCORE) break;
        }
        System.out.println("best score:" + score);
        System.out.println("node:" + all_node);
        System.out.println("beta:" + all_beta);
        gameOut[bestMove.getX()][bestMove.getY()] = currentRole;

        System.out.println(hashhit);
        
        return makeMove(bestMove.getPosition());
    }

    private boolean isWin(int pos, int role){
        int row = Util.getRow(pos), col = Util.getCol(pos);
        return Util.preWin[bitRow[role][row]] ||  Util.preWin[bitCol[role][col]]
                || Util.preWin[bitBiasLeft[role][Util.preBiasLeftIndex[pos]]]
                || Util.preWin[bitBiasRight[role][Util.preBiasRightIndex[pos]]];
    }

    //评估当前一方局势
    private int evaluate(){
        int res = currentScore;
        res = currentRole == 1? res:-res;
        return res;
    }

    //评估单个位置
    private int evaluate(int pos){
        int res = 0;
        int row = Util.getRow(pos), col = Util.getCol(pos);
        int lbindex = Util.preBiasLeftIndex[pos], rbindex = Util.preBiasRightIndex[pos];

        res += rowScore[row];
        res += colScore[col];
        res += biasLeftScore[lbindex];
        res += biasRightScore[rbindex];

        return res;
    }

    //生成可下子位置
    private List<Movement> generate(){
        List<Movement> moveLists = new ArrayList<>();
        boolean visit[] = new boolean[256];

        for(int i = 0; i < hasChessCnt; i++){
            int pos = hasChess.get(i).getPosition();
            int row = Util.getRow(pos), col = Util.getCol(pos);

            // 水平方向
            for(int j = Util.preGenerate[col][bitRow[0][row]][0]; j < col; j++){
                if(!visit[(row << 4) + j]){
                    Movement move = new Movement(0, (row << 4) + j);
                    move.setScore(evaluate((row << 4) + j));
                    moveLists.add(move);
                    visit[(row << 4) + j] = true;
                }
            }
            for(int j = col + 1; j <= Util.preGenerate[col][bitRow[0][row]][1]; j++){
                if(!visit[(row << 4) + j]){
                    Movement move = new Movement(0, (row << 4) + j);
                    move.setScore(evaluate((row << 4) + j));
                    moveLists.add(move);
                    visit[(row << 4) + j] = true;                    
                }
            }
            // 竖直方向
            for(int j = Util.preGenerate[row][bitCol[0][col]][0]; j < row; j++){
                if(!visit[(j << 4) + col]){
                    Movement move = new Movement(0, (j << 4) + col);
                    move.setScore(evaluate((j << 4) + col));
                    moveLists.add(move);
                    visit[(j << 4) + col] = true;
                }
            }
            for(int j = row + 1; j <= Util.preGenerate[row][bitCol[0][col]][1]; j++){
                if(!visit[(j << 4) + col]){
                    Movement move = new Movement(0, (j << 4) + col);
                    move.setScore(evaluate((j << 4) + col));
                    moveLists.add(move);
                    visit[(j << 4) + col] = true;
                }
            }
            // 左斜
            int blindex = Util.preBiasLeftIndex[pos];
            int l = Util.preGenerate[Util.preBiasLeftPos[pos]][bitBiasLeft[0][blindex]][0];
            int r = Util.preGenerate[Util.preBiasLeftPos[pos]][bitBiasLeft[0][blindex]][1];
            for(int j = l; j < Util.preBiasLeftPos[pos]; j++){
                int mpos = Util.biasLeftToPos[blindex][j];
                if(!visit[mpos]){
                    Movement move = new Movement(0, mpos);
                    move.setScore(evaluate(mpos));
                    moveLists.add(move);
                    visit[mpos] = true;
                }
            }
            for(int j = Util.preBiasLeftPos[pos] + 1; j <= r && j <= (blindex>14?28-blindex:blindex); j++){
                int mpos = Util.biasLeftToPos[blindex][j];
                if(!visit[mpos]){
                    Movement move = new Movement(0, mpos);
                    move.setScore(evaluate(mpos));
                    moveLists.add(move);
                    visit[mpos] = true;
                }
            }
            // 右斜
            int brindex = Util.preBiasRightIndex[pos];
            l = Util.preGenerate[Util.preBiasRightPos[pos]][bitBiasRight[0][brindex]][0];
            r = Util.preGenerate[Util.preBiasRightPos[pos]][bitBiasRight[0][brindex]][1];
            for(int j = l; j < Util.preBiasRightPos[pos]; j++){
                int mpos = Util.biasRightToPos[brindex][j];
                if(!visit[mpos]){
                    Movement move = new Movement(0, mpos);
                    move.setScore(evaluate(mpos));
                    moveLists.add(move);
                    visit[mpos] = true;
                }
            }
            for(int j = Util.preBiasRightPos[pos] + 1; j <= r && j <= (brindex>14?28-brindex:brindex); j++){
                int mpos = Util.biasRightToPos[brindex][j];
                if(!visit[mpos]){
                    Movement move = new Movement(0, mpos);
                    move.setScore(evaluate(mpos));
                    moveLists.add(move);
                    visit[mpos] = true;
                }
            }
        }
        return moveLists;
    }

    private boolean makeMove(int pos){
        int row = Util.getRow(pos), col = Util.getCol(pos);
        int lbindex = Util.preBiasLeftIndex[pos], rbindex = Util.preBiasRightIndex[pos];
        step++;
        GameMap[pos] = currentRole;
        // 更新位行位列位斜
        bitRow[currentRole][row] |= (1 << col);
        bitCol[currentRole][col] |= (1 << row);
        bitBiasLeft[currentRole][lbindex] |= (1 << Util.preBiasLeftPos[pos]);
        bitBiasRight[currentRole][rbindex] |= (1 << Util.preBiasRightPos[pos]);
        bitRow[0][row] = bitRow[1][row] | bitRow[2][row];
        bitCol[0][col] = bitCol[1][col] | bitCol[2][col];
        bitBiasLeft[0][lbindex] = bitBiasLeft[1][lbindex] | bitBiasLeft[2][lbindex];
        bitBiasRight[0][rbindex] = bitBiasRight[1][rbindex] | bitBiasRight[2][rbindex];
        // 更新棋盘详细信息
        strRow[row].setCharAt(col, (char) ('0' + currentRole));
        strCol[col].setCharAt(row, (char) ('0' + currentRole));
        strBiasLeft[lbindex].setCharAt(Util.preBiasLeftPos[pos], (char) ('0' + currentRole));
        strBiasRight[rbindex].setCharAt(Util.preBiasRightPos[pos], (char) ('0' + currentRole));
        // 更新分数
        updateScore(row, col, lbindex, rbindex);

        hasChessCnt++;
        hasChess.add(new Movement(0, pos));
        hashTable.update(pos, currentRole);

        currentRole = currentRole == 1? 2:1;
        return isWin(pos, GameMap[pos]);
    }

    private void unMakeMove(int pos){
        int row = Util.getRow(pos), col = Util.getCol(pos);
        int lbindex = Util.preBiasLeftIndex[pos], rbindex = Util.preBiasRightIndex[pos];
        int role = GameMap[pos];
        step--;
        GameMap[pos] = 0;
        // 更新位行位列位斜
        bitRow[role][row] &= ~(1 << col);
        bitCol[role][col] &= ~(1 << row);
        bitBiasLeft[role][lbindex] &= ~(1 << Util.preBiasLeftPos[pos]);
        bitBiasRight[role][rbindex] &= ~(1 << Util.preBiasRightPos[pos]);
        bitRow[0][row] = bitRow[1][row] | bitRow[2][row];
        bitCol[0][col] = bitCol[1][col] | bitCol[2][col];
        bitBiasLeft[0][lbindex] = bitBiasLeft[1][lbindex] | bitBiasLeft[2][lbindex];
        bitBiasRight[0][rbindex] = bitBiasRight[1][rbindex] | bitBiasRight[2][rbindex];
        // 更新棋盘详细信息
        strRow[row].setCharAt(col, '0');
        strCol[col].setCharAt(row, '0');
        strBiasLeft[lbindex].setCharAt(Util.preBiasLeftPos[pos], '0');
        strBiasRight[rbindex].setCharAt(Util.preBiasRightPos[pos], '0');
        // 更新分数
        updateScore(row, col, lbindex, rbindex);

        hasChess.remove(hasChessCnt - 1);
        hasChessCnt--;
        hashTable.update(pos, currentRole);

        currentRole = currentRole == 1? 2:1;
    }

    private void updateScore(int row, int col, int lbindex, int rbindex){
        int newRowScore = Util.presetScore[Integer.parseInt(strRow[row].toString(), 3)];
        currentScore += newRowScore - rowScore[row];
        rowScore[row] = newRowScore;
        int newColScore = Util.presetScore[Integer.parseInt(strCol[col].toString(), 3)];
        currentScore += newColScore - colScore[col];
        colScore[col] = newColScore;
        int newLeftScore = Util.presetScore[Integer.parseInt(strBiasLeft[lbindex].toString(), 3)];
        currentScore += newLeftScore - biasLeftScore[lbindex];
        biasLeftScore[lbindex] = newLeftScore;
        int newRightScore = Util.presetScore[Integer.parseInt(strBiasRight[rbindex].toString(), 3)];
        currentScore += newRightScore - biasRightScore[rbindex];
        biasRightScore[rbindex] = newRightScore;
    }

    int hashhit = 0;
    int all_node = 0;
    int all_beta = 0;
    private int alphaBetaSearch(int depth, int alpha, int beta){
        int score = 0;
        int best;
        Movement move = NONE_MOVE.clone(), gooMove = NONE_MOVE.clone();
        List<Movement> moveLists;

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

        moveLists = generate();
        if(!move.equals(NONE_MOVE) && GameMap[move.getPosition()] == 0) {
            Movement temp = move.clone();
            temp.setScore(MAX_SCORE);
            moveLists.add(temp);
        }
        Collections.sort(moveLists);
        int moveNum = moveLists.size();
        all_node += moveNum;

        for(int i = 0; i < moveNum; i++){
            move = moveLists.get(i);
            if(makeMove(move.getPosition())){
                score = MAX_SCORE - step;
            }
            else{
                score = -alphaBetaSearch(depth - 1, -beta, -alpha);
            }
            unMakeMove(move.getPosition());

            if(score >= beta){
                all_beta++;
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
