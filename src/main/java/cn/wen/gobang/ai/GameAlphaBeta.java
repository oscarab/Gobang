package cn.wen.gobang.ai;

import java.util.Collections;
import java.util.List;

import cn.wen.gobang.util.HashTable;
import cn.wen.gobang.util.Movement;

public class GameAlphaBeta extends Game{
    private int searchNode = 0;
    private int betaNode = 0;

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

        score = evaluate();     // 执行一次评估
        if(score > beta)
            return beta;
        if(score > alpha)
            alpha = score;
        if(depth <= 0)          // 到达静态搜索深度限制
            return alpha;

        moveLists = generate();
        Collections.sort(moveLists);
        int moveNum = moveLists.size();

        for(int i = 0; i < moveNum; i++){
            move = moveLists.get(i);
            // 只考虑高分位置，能成活三活四或堵对方活三的位置
            if(move.getScore() < 1200 || i > 8) break;
            
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

    /**
     * 带alpha beta 剪枝的极大极小值搜素
     * @param depth 搜索深度
     * @param alpha
     * @param beta
     * @return  int 最好走法的分值
     */
    private int alphaBetaSearch(int depth, int alpha, int beta){
        int score = 0;              // 当前分值
        int best;                   // 最好的分值
        Movement move;              // 当前着法
        List<Movement> moveLists;   // 所有着法

        // 检查是否超时
        if(isLimited || System.currentTimeMillis() - startTime > maxTime){
            isLimited = true;
            return NONE_SCORE;
        }

        score = hashTable.readHashTable(depth, step, alpha, beta);
        if(score != NONE_SCORE){
            return score;
        }

        best = NONE_SCORE;
        boolean isAlpha = true;

        if(depth <= 0){
            // 开始静态搜索
            score = quiescentSearch(6, alpha, beta);
            return score;
        }

        moveLists = generate();             // 生成着法
        Collections.sort(moveLists);        // 着法排序
        int moveNum = moveLists.size();

        for(int i = 0; i < moveNum; i++){
            move = moveLists.get(i);
            // 剪掉一些分数较低的着法
            if(move.getScore() < 100 || i > 23) break;
            searchNode++;    

            // 执行一个走法
            if(makeMove(move.getPosition())){
                score = MAX_SCORE - step;   // 若胜利则返回一个极大值
            }
            else{
                score = -alphaBetaSearch(depth - 1, -beta, -alpha);
            }
            // 回溯一个走法
            unMakeMove(move.getPosition());

            if(score >= beta){
                betaNode++;
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
            // alpha结点
            hashTable.saveHashTable(depth, step, alpha, HashTable.hashAlpha);
        }
        else{
            // PV结点
            hashTable.saveHashTable(depth, step, best, HashTable.hashExact);
        }
        return alpha;
    }

    public boolean AIthink() {
        int score = 0, best = 0;
        step = 0;
        searchNode = 0;
        betaNode = 0;
        isLimited = false;
        hashTable.clear();
        startTime = System.currentTimeMillis();
        Movement bestMoveSave = NONE_MOVE;

        outMessage("开始迭代加深搜索");
        for(maxDepth = 1; maxDepth <= 6; maxDepth++){
            score = alphaBetaSearch(maxDepth, NONE_SCORE, -NONE_SCORE);
            outMessage("完成深度为" + maxDepth + "的搜索，总已花费时间" + (System.currentTimeMillis() - startTime) + "ms");

            // 检查是否时间超限
            if(isLimited) break;
            else {
                bestMoveSave = bestMove;
                best = score;
            }

            // 已找到必胜走法直接返回
            if(score > WIN_SCORE) break;
        }

        bestMove = bestMoveSave;
        outMessage("是否发生搜索超时: " + isLimited);
        outMessage("总搜索结点数: " + searchNode);
        outMessage("发生剪枝的结点数: " + betaNode);
        outMessage("最佳走法: " + bestMove.getX() + "," + bestMove.getY() + " 分值: " + best);
        outMessage("----------结束----------");
        gameOut[bestMove.getX()][bestMove.getY()] = currentRole;
        
        return makeMove(bestMove.getPosition());
    }

    private void outMessage(String str){
        if(message != null)
            message.appendMessage(str);
    }
}
