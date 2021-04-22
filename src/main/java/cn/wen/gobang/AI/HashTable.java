package cn.wen.gobang.AI;

import java.security.SecureRandom;

import cn.wen.gobang.Game;

public class HashTable {
    private final int TABLE_SIZE = 1 << 22;
    private final int TABLE_SIZE_MASK = (1 << 22) - 1;

    public static final int hashAlpha = 1;
    public static final int hashBeta = 2;
    public static final int hashExact = 3;

    private long[][] zobristTable = new long[3][256];
    private long[][] zobristCheckTable = new long[3][256];
    private long key;
    private long checkKey;
    private long playKey;
    private long playCheckKey;
    private Zobrist[] hashtable = new Zobrist[TABLE_SIZE];

    public HashTable(){
        SecureRandom random = new SecureRandom();
        key = random.nextLong();
        checkKey = random.nextLong();
        playKey = random.nextLong();
        playCheckKey = random.nextLong();
        for(int k = 1; k <= 2; k++){
            for(int i = 0; i < 256;i++){
                zobristTable[k][i] = random.nextLong();
                zobristCheckTable[k][i] = random.nextLong();
            }
        }
    }
    
    public void clear(){
        hashtable = new Zobrist[TABLE_SIZE];
    }

    public void update(int pos, int role){
        key ^= zobristTable[role][pos];
        key ^= playKey;
        checkKey ^= zobristCheckTable[role][pos];
        checkKey ^= playCheckKey;
    }

    /**
     * 读取置换表
     * @param depth
     * @param alpha
     * @param beta
     * @param move
     * @return
     */
    public int readHashTable(int depth, int step, int alpha, int beta){
        int index = (int) (key & TABLE_SIZE_MASK);
        Zobrist node = hashtable[index];
        
        if(node != null && node.getCheckKey() == checkKey){
            if(node.getDepth() >= depth){
                // 调整至相对值
                int score = node.getScore();
                if(score > Game.WIN_SCORE){
                    score -= step;
                }
                if(score < -Game.WIN_SCORE){
                    score += step;
                }
                // PV结点
                if(node.getType() == hashExact){
                    return score;
                }
                else if(node.getType() == hashAlpha && score <= alpha){
                    return alpha;
                }
                else if(node.getType() == hashBeta && score >= beta){
                    return beta;
                }
            }
        }
        return Game.NONE_SCORE;
    }

    /**
     * 存入置换表
     * @param depth
     * @param score
     * @param type
     * @param move
     */
    public void saveHashTable(int depth, int step, int score, int type){
        int index = (int) (key & TABLE_SIZE_MASK);
        Zobrist node = hashtable[index];
        Zobrist new_node =  new Zobrist(checkKey, score, depth, type);

        // 存入置换表前，将估值调整到绝对值
        if(score > Game.WIN_SCORE){
            new_node.setScore(score + step);
        }
        if(score < -Game.WIN_SCORE){
            new_node.setScore(score - step);
        }
        // 出现相同局面冲突
        if(node != null && node.getCheckKey() == checkKey){
            // 深度优先覆盖
            if(node.getDepth() <= depth){
                hashtable[index] = new_node;
            }
        }
        // 不同局面，直接覆盖
        else{
            hashtable[index] = new_node;
        }
    }
}
