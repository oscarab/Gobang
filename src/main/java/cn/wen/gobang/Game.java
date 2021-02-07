package cn.wen.gobang;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Game {
    private int[][] GameMap = new int[15][15];
    private int[][] gamemap = new int[15][15];
    private int AI;
    private int[][][] dir = {{{-1,0},{1,0}}, {{0,1},{0,-1}}, {{-1,-1},{1,1}}, {{-1,1},{1,-1}}};

    private List<node> hasChess = new ArrayList<node>();
    private int hasChessCnt = 0;

    private final int TABLE_SIZE = 1<<24;
    private final int MIN = -2147483647;
    private final int MAX = 2147483647;
    private final int MAX_STEP = 6;

    private long[][] whiteZobrist = new long[15][15];
    private long[][] blackZobrist = new long[15][15];
    private long key;
    private zobrist[] hashtable = new zobrist[TABLE_SIZE];

    public Game(int AI){
        SecureRandom random = new SecureRandom();
        key = random.nextLong();
        for(int i = 0; i < 15;i++){
            for(int j = 0; j < 15; j++){
                whiteZobrist[i][j] = random.nextLong();
                blackZobrist[i][j] = random.nextLong();
            }
        }

        this.AI = AI;
    }

    public void setAI(int val){
        AI = val;
    }

    public void set(int x, int y, int val){
        put(x, y, val);
        gamemap[x][y] = val;
    }

    public int get(int x, int y){
        return gamemap[x][y];
    }

    public boolean peoplePut(int x, int y){
        put(x, y, ((AI + 1)&1) + 1);
        gamemap[x][y] = ((AI + 1)&1) + 1;
        return isWin(x, y);
    }

    public boolean AIput(){
        node best = dfs(0, MAX, MIN, 0, 0, AI + 1);
        put(best.x, best.y, AI + 1);
        gamemap[best.x][best.y] = AI + 1;
        return isWin(best.x, best.y);
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

    private int getScore(int line, int block){
        switch (line){
            case 9:
            case 8:
            case 7:
            case 6:
            case 5:
                return 900000;      //连5
            case 4:
                if(block == 0){     //活4
                    return 10000;
                }
                else if(block == 1){
                    return 1000;
                }
                break;
            case 3:
                if(block == 0){
                    return 1000;
                }
                else if(block == 1){
                    return 100;
                }
                break;
            case 2:
                if(block == 0){
                    return 100;
                }
                else if(block == 1){
                    return 10;
                }
                break;
            default:
                if(block == 0)
                    return 10;
            }
            return 0;
    }

    private int evaluate(int role){
        int res = 0;
        //bool visit[15][15] = {0};
        for(int i = 0; i < hasChessCnt; i++){
            int x = hasChess.get(i).x;
            int y = hasChess.get(i).y;
            if(GameMap[x][y] != role) continue;
            //visit[x][y] = true;
    
            for(int a = 0; a < 4; a++){     //四个方向
                short block = 0;            //被堵住的数量
                short line = 1;             //连子数量
                for(int b = 0; b < 2; b++){
                    for(int cnt = 1; cnt <= 4; cnt++){
                        if(x + dir[a][b][0]*cnt < 0 || y + dir[a][b][1]*cnt < 0 || x + dir[a][b][0]*cnt > 14 || y + dir[a][b][1]*cnt > 14){
                            block++;
                            break;
                        }
                        //visit[x + dir[a][b][0]*cnt][y + dir[a][b][1]*cnt] = true;
                        if(GameMap[x + dir[a][b][0]*cnt][y + dir[a][b][1]*cnt] == role){    //连子
                            line++;
                        }
                        else if(GameMap[x + dir[a][b][0]*cnt][y + dir[a][b][1]*cnt] == role % 2 + 1){  //堵塞
                            block++;
                            break;
                        }
                        else{       //留空
                            break;
                        }
                    }
                }
                res += getScore(line, block);
            }
        }
        return res;
    }

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
                    else if(GameMap[x + dir[a][b][0]*cnt][y + dir[a][b][1]*cnt] == (role&1) + 1){  //堵塞
                        block++;
                        break;
                    }
                    else{       //留空
                        break;
                    }
                }
            }
            res += getScore(line, block);
        }
        return res;
    }

    private List<node> generate(){
        List<node> genPlace = new ArrayList<>();
        boolean visit[][] = new boolean[15][15];

        for(int i = 0; i < hasChessCnt; i++){
            node p = hasChess.get(i);
            int x = p.x, y = p.y;
            for(int a = 0; a < 4; a++){
                for(int b = 0; b < 2; b++){
                    for(int cnt = 1; cnt <= 2; cnt++){
                        int x1 = x + dir[a][b][0]*cnt;
                        int y1 = y + dir[a][b][1]*cnt;
                        if(x1 < 0 || y1 < 0 || x1 > 14 || y1 > 14)
                            break;
    
                        if(GameMap[x1][y1] == 0 && !visit[x1][y1]){
                            visit[x1][y1] = true;
                            genPlace.add(new node(evaluate(x1, y1, AI+1) - evaluate(x1, y1, ((AI+1)&1) + 1), x1, y1));
                        }
                    }
                }
            }
        }
        return genPlace;
    }

    private void put(int x, int y, int role){
        GameMap[x][y] = role;
        hasChessCnt++;
        hasChess.add(new node(0, x, y));
        key ^= (role == 1? blackZobrist[x][y]:whiteZobrist[x][y]);
    }

    private void remove(int x, int y, int role){
        hasChess.remove(hasChessCnt - 1);
        hasChessCnt--;
        GameMap[x][y] = 0;
        key ^= (role == 1? blackZobrist[x][y]:whiteZobrist[x][y]);
    }

    private node dfs(int step, int alpha, int beta, int x, int y, int role){
        if(step > 0){
            zobrist zo = hashtable[(int) (key & (TABLE_SIZE-1))];
            if(zo != null && zo.deep == step && key == zo.key){
                return new node(zo.score, 0, 0);
            } 
        }
    
        node best = new node((AI == role - 1)? MIN:MAX, 0, 0);
        if(step > 0 && isWin(x, y)){
            int score = evaluate(role) - evaluate((role&1) + 1);
            best.score = (AI != role - 1)? -score:score;
            hashtable[(int) (key & (TABLE_SIZE-1))] = new zobrist(key, best.score, step);
            return best;
        }
        if(step == MAX_STEP){
            int aiScore = evaluate(role);
            int humanScore = evaluate((role&1) + 1);
            best.score = aiScore - humanScore;
            hashtable[(int) (key & (TABLE_SIZE-1))] = new zobrist(key, best.score, step);
            return best;
        }
    
        int max = MIN;
        int min = MAX;
        List<node> points = generate();      //生成可下子的位置
        int len = points.size();
        if(AI == role - 1)
            points.sort(new Comparator<node>(){
				public int compare(node o1, node o2) {
					return o2.score - o1.score;
				}
            });
        else
            points.sort(new Comparator<node>(){
                public int compare(node o1, node o2) {
                    return o1.score - o2.score;
                }
            });           
        for(int i = 0; i < len; i++){
            if(best.score > alpha && AI == role - 1)
                return best;
            if(best.score < beta && AI != role - 1)
                return best;
            node p = points.get(i);
    
            put(p.x, p.y, role);
            node point = dfs(step + 1, min, max, p.x, p.y, (role&1) + 1);
            remove(p.x, p.y, role);
            point.x = p.x;
            point.y = p.y;
    
            min = point.score < min? point.score:min;
            max = point.score > max? point.score:max;
    
            if(AI == role - 1){             //MAX层
                if(point.score > best.score){
                    best = point;
                }
            }
            else{                           //MIN层
                if(point.score < best.score){
                    best = point;
                }
            }
        }
        hashtable[(int) (key & (TABLE_SIZE-1))] = new zobrist(key, best.score, step);
        return best;
    }

    public class zobrist{
        public long key;
        public int score;
        public int deep;
        public zobrist(long key, int score, int deep){
            this.key = key;
            this.score = score;
            this.deep = deep;
        }
    }
    public class node{
        public int score;
        public int x, y;
        public node(int score, int x, int y){
            this.score = score;
            this.x = x;
            this.y = y;
        }
    }
}
