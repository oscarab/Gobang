package cn.wen.gobang.ai;

import java.util.List;

import cn.wen.gobang.util.Movement;

public class GameMinMax extends Game{
    private int searchNode = 0;

    /**
     * 极小值搜索
     * @param depth 深度
     * @return  int 搜索到的最好分值
     */
    private int minSearch(int depth){
        int best = -NONE_SCORE;     // 当前最好分值
        int score = 0;              // 当前分值
        Movement move;              // 当前着法
        List<Movement> moveLists;   // 所有着法
        
        if(depth <= 0){
            score = evaluate();
            return score;
        }
        moveLists = generate();             // 生成着法
        int moveNum = moveLists.size();
        searchNode += moveNum;

        for(int i = 0; i < moveNum; i++){
            move = moveLists.get(i);

            if(makeMove(move.getPosition())){
                score = -MAX_SCORE;
            }
            else{
                score = maxSearch(depth - 1);
            }
            unMakeMove(move.getPosition());

            if(score < best){
                best = score;
            }
        }

        return best;
    }

    /**
     * 极大值搜索
     * @param depth 深度
     * @return  int 搜索到的最好分值
     */
    private int maxSearch(int depth){
        int best = NONE_SCORE;      // 当前最好分值
        int score = 0;              // 当前分值
        Movement move;              // 当前着法
        List<Movement> moveLists;   // 所有着法
        
        if(depth <= 0){
            score = evaluate();
            return score;
        }
        moveLists = generate();             // 生成着法
        int moveNum = moveLists.size();
        searchNode += moveNum;

        for(int i = 0; i < moveNum; i++){
            move = moveLists.get(i);

            if(makeMove(move.getPosition())){
                score = MAX_SCORE;
            }
            else{
                score = minSearch(depth - 1);
            }
            unMakeMove(move.getPosition());

            if(score > best){
                best = score;
                if(depth == 4)
                    bestMove = move;
            }
        }

        return best;
    }

    public boolean AIthink() {
        searchNode = 0;
        long startTime = System.currentTimeMillis();
        maxSearch(4);   // 仅搜索四层
        outMessage("消耗时间: " + (System.currentTimeMillis() - startTime) + "ms");
        outMessage("总搜索结点数: " + searchNode);
        outMessage("----------结束----------");

        gameOut[bestMove.getX()][bestMove.getY()] = currentRole;

        return makeMove(bestMove.getPosition());
    }
    
}
