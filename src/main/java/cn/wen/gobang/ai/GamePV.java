package cn.wen.gobang.ai;

import java.util.Collections;
import java.util.List;

import cn.wen.gobang.util.HashTable;
import cn.wen.gobang.util.Movement;

public class GamePV extends GameAlphaBeta{

    protected int search(int depth, int alpha, int beta){
        int score = 0;              // 当前分值
        int best;                   // 最好的分值
        Movement move;              // 当前着法
        List<Movement> moveLists;   // 所有着法

        // 检查是否超时
        if(isLimited || System.currentTimeMillis() - startTime > maxTime){
            isLimited = true;
            return NONE_SCORE;
        }

        // 读取置换表
        score = hashTable.readHashTable(depth, step, alpha, beta);
        if(score != NONE_SCORE){
            return score;
        }

        best = NONE_SCORE;
        boolean isAlpha = true;
        boolean isPV = false;

        if(depth <= 0){
            // 开始静态搜索
            score = quiescentSearch(killSearchDepth, alpha, beta);
            return score;
        }

        moveLists = generate();             // 生成着法
        Collections.sort(moveLists);        // 着法排序
        int moveNum = moveLists.size();

        for(int i = 0; i < moveNum; i++){
            move = moveLists.get(i);
            // 剪掉一些分数较低的着法
            if(i > 20) break;
            searchNode++;    

            // 执行一个走法
            if(makeMove(move.getPosition())){
                score = MAX_SCORE - step;   // 若胜利则返回一个极大值
            }
            else{
                if(isPV){
                    score = -search(depth - 1, -alpha - 1, -alpha);
                    if(score > alpha && score < beta)
                        score = -search(depth - 1, -beta, -alpha);
                }
                else{
                    score = -search(depth - 1, -beta, -alpha);
                }
                
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
                    isPV = true;
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
}
