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
    private int[] strRow = new int[15];
    private int[] strCol = new int[15];
    private int[] strBiasLeft = new int[30];
    private int[] strBiasRight = new int[30];
    private int currentRole = 1;                    // 当前执子的颜色 1黑2白

    // 行、列、斜线的所有评估分数
    private int[] rowScore = new int[15];
    private int[] colScore = new int[15];
    private int[] biasLeftScore = new int[30];
    private int[] biasRightScore = new int[30];
    private int currentScore;                       // 当前方的局势评估分

    private List<Movement> movesArray = new ArrayList<Movement>(100);
    private int chessCount = 0;
    private Movement bestMove;

    private HashTable hashTable;

    public static final int NONE_SCORE = -2000000000;
    public static final Movement NONE_MOVE = new Movement(-1, -1);
    public static final int MAX_SCORE = 1000000000;
    public static final int WIN_SCORE = MAX_SCORE - 24;
    private int step;
    private int maxDepth = 6;

    private long maxTime = 20000;
    private long startTime = 0;
    private boolean isLimited = false;

    public Game(){
        hashTable = new HashTable();

        for(int role = 0; role < 3; role++){
            for(int i = 0; i < 14; i++){
                bitBiasLeft[role][i] = 1 >> (i + 1);
                bitBiasLeft[role][28 - i] = 1 >> (i + 1);
                bitBiasRight[role][i] = 1 >> (i + 1);
                bitBiasRight[role][28 - i] = 1 >> (i + 1);
            }
        }
    }

    public Movement getLastMove(){
        return bestMove;
    }

    public int get(int x, int y){
        return gameOut[x][y];
    }

    /**
     * 由当前方直接下一步棋子
     * @param pos   下子位置
     * @return  true 如果胜利
     */
    public boolean directMove(int pos){
        gameOut[Util.getCol(pos)][Util.getRow(pos)] = currentRole;
        return makeMove(pos);
    }

    /**
     * 悔棋
     */
    public void regret(){
        if(chessCount < 2) return;
        int pos = movesArray.get(chessCount - 1).getPosition();
        gameOut[Util.getCol(pos)][Util.getRow(pos)] = 0;
        unMakeMove(pos);

        pos = movesArray.get(chessCount - 1).getPosition();
        gameOut[Util.getCol(pos)][Util.getRow(pos)] = 0;
        unMakeMove(pos);
    }

    /**
     * AI思考并下一步棋
     * @return true 如果AI获得胜利
     */
    public boolean AIthink(){
        int score = 0;
        step = 0;
        hashhit = 0;
        all_beta = 0;
        all_node = 0;
        isLimited = false;
        hashTable.clear();
        startTime = System.currentTimeMillis();
        Movement bestMoveSave = NONE_MOVE;

        for(maxDepth = 1; maxDepth <= 6; maxDepth++){
            score = alphaBetaSearch(maxDepth, NONE_SCORE, -NONE_SCORE);
            System.out.println((System.currentTimeMillis() - startTime) + "ms");
            if(isLimited) break;
            else bestMoveSave = bestMove;
            if(score > WIN_SCORE) break;
        }
        bestMove = bestMoveSave;
        System.out.println("best score:" + score);
        System.out.println("node:" + all_node);
        System.out.println("beta:" + all_beta);
        gameOut[bestMove.getX()][bestMove.getY()] = currentRole;

        System.out.println(hashhit);
        
        return makeMove(bestMoveSave.getPosition());
    }

    /**
     * 判断某一方是否胜利
     * @param pos   判断位置
     * @param role  黑或白方
     * @return  true 如果胜利
     */
    private boolean isWin(int pos, int role){
        int row = Util.getRow(pos), col = Util.getCol(pos);
        return Util.preWin[bitRow[role][row]] ||  Util.preWin[bitCol[role][col]]
                || Util.preWin[bitBiasLeft[role][Util.preBiasLeftIndex[pos]]]
                || Util.preWin[bitBiasRight[role][Util.preBiasRightIndex[pos]]];
    }

    /**
     * 评估当前一方局势
     * @return int 当前局势分数
     */
    private int evaluate(){
        int res = currentScore;
        res = currentRole == 1? res:-res;
        return res;
    }

    /**
     * 评估单个位置，用于启发性搜索
     * @param pos 评估的位置
     * @return  int 评估的分数
     */
    private int evaluate(int pos){
        int resBlack = 0, resWhite = 0, baseValue = 0;
        int row = Util.getRow(pos), col = Util.getCol(pos);
        int lbindex = Util.preBiasLeftIndex[pos], rbindex = Util.preBiasRightIndex[pos];

        // 记录原始分数
        baseValue += rowScore[row];
        baseValue += colScore[col];
        baseValue += biasLeftScore[lbindex];
        baseValue += biasRightScore[rbindex];

        // 下一步黑子，记录分数
        makePartMove(1, pos, row, col, lbindex, rbindex);
        updateScore(row, col, lbindex, rbindex);
        resBlack += rowScore[row];
        resBlack += colScore[col];
        resBlack += biasLeftScore[lbindex];
        resBlack += biasRightScore[rbindex];
        unMakePartMove(pos, row, col, lbindex, rbindex);
        updateScore(row, col, lbindex, rbindex);
        resBlack -= baseValue;

        // 下一步白子，记录分数
        makePartMove(2, pos, row, col, lbindex, rbindex);
        updateScore(row, col, lbindex, rbindex);
        resWhite -= rowScore[row];
        resWhite -= colScore[col];
        resWhite -= biasLeftScore[lbindex];
        resWhite -= biasRightScore[rbindex];
        unMakePartMove(pos, row, col, lbindex, rbindex);
        updateScore(row, col, lbindex, rbindex);
        resWhite += baseValue;

        // 评估分数为 黑方获得优势 + 白方获得优势
        return resBlack + resWhite;
    }

    /**
     * 生成可下子的位置
     * @return List 下子位置
     */
    private List<Movement> generate(){
        List<Movement> moveLists = new ArrayList<>();
        boolean visit[] = new boolean[256];

        for(int i = 0; i < chessCount; i++){
            int pos = movesArray.get(i).getPosition();
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

    /**
     * 下一步子
     * @param pos   下子位置
     * @return  true 如果胜利
     */
    private boolean makeMove(int pos){
        int row = Util.getRow(pos), col = Util.getCol(pos);
        int lbindex = Util.preBiasLeftIndex[pos], rbindex = Util.preBiasRightIndex[pos];
        step++;
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
        makePartMove(currentRole, pos, row, col, lbindex, rbindex);
        // 更新分数
        updateScore(row, col, lbindex, rbindex);

        chessCount++;
        movesArray.add(new Movement(0, pos));
        hashTable.update(pos, currentRole);

        currentRole = currentRole == 1? 2:1;
        return isWin(pos, GameMap[pos]);
    }

    /**
     * 撤销一步子
     * @param pos   撤销的位置
     */
    private void unMakeMove(int pos){
        int row = Util.getRow(pos), col = Util.getCol(pos);
        int lbindex = Util.preBiasLeftIndex[pos], rbindex = Util.preBiasRightIndex[pos];
        int role = GameMap[pos];
        step--;
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
        unMakePartMove(pos, row, col, lbindex, rbindex);
        // 更新分数
        updateScore(row, col, lbindex, rbindex);

        movesArray.remove(chessCount - 1);
        chessCount--;
        hashTable.update(pos, currentRole);

        currentRole = currentRole == 1? 2:1;
    }

    private void makePartMove(int role, int pos, int row, int col, int lbindex, int rbindex){
        strRow[row] += Util.presetPow[col][role];
        strCol[col] += Util.presetPow[row][role];
        strBiasLeft[lbindex] += Util.presetPow[Util.preBiasLeftPos[pos]][role];
        strBiasRight[rbindex] += Util.presetPow[Util.preBiasRightPos[pos]][role];
        GameMap[pos] = role;
    }
    private void unMakePartMove(int pos, int row, int col, int lbindex, int rbindex){
        strRow[row] -= Util.presetPow[col][GameMap[pos]];
        strCol[col] -= Util.presetPow[row][GameMap[pos]];
        strBiasLeft[lbindex] -= Util.presetPow[Util.preBiasLeftPos[pos]][GameMap[pos]];
        strBiasRight[rbindex] -= Util.presetPow[Util.preBiasRightPos[pos]][GameMap[pos]];
        GameMap[pos] = 0;
    }

    /**
     * 更新当前局势分数
     * @param row       要更新的行
     * @param col       要更新的列
     * @param lbindex   要更新的左斜线
     * @param rbindex   要更新的右斜线
     */
    private void updateScore(int row, int col, int lbindex, int rbindex){
        int newRowScore = Util.presetScore[strRow[row]];
        currentScore += newRowScore - rowScore[row];
        rowScore[row] = newRowScore;
        int newColScore = Util.presetScore[strCol[col]];
        currentScore += newColScore - colScore[col];
        colScore[col] = newColScore;
        int newLeftScore = Util.presetScore[strBiasLeft[lbindex]];
        currentScore += newLeftScore - biasLeftScore[lbindex];
        biasLeftScore[lbindex] = newLeftScore;
        int newRightScore = Util.presetScore[strBiasRight[rbindex]];
        currentScore += newRightScore - biasRightScore[rbindex];
        biasRightScore[rbindex] = newRightScore;
    }

    /**
     * 静态搜索
     * @param depth 限制深度
     * @param alpha alpha值
     * @param beta  beta值
     * @return  int 评估分数
     */
    private int quiescentSearch(int depth, int alpha, int beta){
        int score = 0, best = 0;
        Movement move;
        List<Movement> moveLists;

        best = step - MAX_SCORE;
        if(best > beta){
            return beta;
        }

        score = evaluate();
        if(score > beta)
            return beta;
        if(score > alpha)
            alpha = score;
        if(depth <= 0)
            return alpha;

        moveLists = generate();
        Collections.sort(moveLists);
        int moveNum = moveLists.size();
        for(int i = 0; i < moveNum; i++){
            move = moveLists.get(i);
            // 只考虑高分位置，能成活三活四或堵对方活三的位置
            if(move.getScore() < 1200) break;
            
            if(makeMove(move.getPosition())){
                score = MAX_SCORE - step;
            }
            else{
                score = -quiescentSearch(depth - 1, -beta, -alpha);
            }
            unMakeMove(move.getPosition());
            if(score >= beta){
                return beta;
            }
            if(score > best){
                best = score;
                if(score > alpha){
                    alpha = score;
                }
            }
        }
        return alpha;
    }

    int hashhit = 0;
    int all_node = 0;
    int all_beta = 0;
    private int alphaBetaSearch(int depth, int alpha, int beta){
        int score = 0;
        int best;
        Movement move;
        List<Movement> moveLists;

        // 检查是否超时
        if(isLimited || System.currentTimeMillis() - startTime > maxTime){
            isLimited = true;
            return NONE_SCORE;
        }

        score = hashTable.readHashTable(depth, step, alpha, beta);
        if(score != NONE_SCORE){
            hashhit++;
            return score;
        }

        best = NONE_SCORE;
        boolean isAlpha = true;

        if(depth <= 0){
            // 开始静态搜索
            score = quiescentSearch(4, alpha, beta);
            return score;
        }

        moveLists = generate();             // 生成着法
        Collections.sort(moveLists);        // 着法排序
        int moveNum = moveLists.size();
        all_node += moveNum;

        for(int i = 0; i < moveNum; i++){
            move = moveLists.get(i);
            // 剪掉一些分数较低的着法
            if(move.getScore() < 100) break;    

            if(makeMove(move.getPosition())){
                score = MAX_SCORE - step;
            }
            else{
                score = -alphaBetaSearch(depth - 1, -beta, -alpha);
            }
            unMakeMove(move.getPosition());

            if(score >= beta){
                all_beta++;
                hashTable.saveHashTable(depth, step, beta, HashTable.hashBeta);
                return beta;
            }
            if(score > best){
                best = score;
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
            hashTable.saveHashTable(depth, step, alpha, HashTable.hashAlpha);
        }
        else{
            hashTable.saveHashTable(depth, step, best, HashTable.hashExact);
        }
        return alpha;
    }

}
