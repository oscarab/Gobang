package cn.wen.gobang.AI;

public class Util {
    public static int preGenerate[][][] = new int[15][32768][2];
    public static boolean preWin[] = new boolean[32768];

    public static int preBiasLeftIndex[] = new int[256];
    public static int preBiasRightIndex[] = new int[256];
    public static int preBiasLeftPos[] = new int[256];
    public static int preBiasRightPos[] = new int[256];

    public static int biasLeftToPos[][] = new int[30][15];
    public static int biasRightToPos[][] = new int[30][15];

    public static int presetScore[] = new int[14348907];

    public static int presetPow[][] = new int[15][3];

    public static void initPreset(){
        // 生成着法预置数组
        for(int i = 0; i < 15; i++){
            for(int bit = 0; bit < 32768; bit++){
                int maskLeft = 1 << i, maskRight = 1 << i;
                preGenerate[i][bit][0] = i;
                preGenerate[i][bit][1] = i;
                maskLeft >>= 1;
                while(maskLeft > 0 && (bit & maskLeft) == 0 && i - preGenerate[i][bit][0] < 2 && preGenerate[i][bit][0] > 0){ 
                    preGenerate[i][bit][0]--;
                    maskLeft >>= 1;
                }
                maskRight <<= 1;
                while(maskRight < 32768 && (bit & maskRight) == 0 && preGenerate[i][bit][1] - i < 2 && preGenerate[i][bit][1] < 14){
                    preGenerate[i][bit][1]++;
                    maskRight <<= 1;
                }
            }
        }

        // 生成胜利局面预置
        for(int bit = 0; bit < 32768; bit++){
            int mask = 16384;
            int cnt = 0;
            while(mask > 0){ 
                if((bit & mask) != 0)
                    cnt++;
                else 
                    cnt = 0;
                if(cnt >= 5) break;
                mask >>= 1;
            }
            preWin[bit] = cnt >= 5;
        }

        // 斜线定位数组
        for(int i = 0; i < 15; i++){
            for(int j = 0; j < 15; j++){
                preBiasLeftIndex[(i << 4) + j] = i + j;
                preBiasRightIndex[(i << 4) + j] = 14 - j + i;
            }
        }

        // 斜线上的定位数组
        for(int i = 0; i < 15; i++){
            for(int j = 0; j < 15 - i; j++){
                preBiasLeftPos[(i << 4) + j] = j;
            }
            for(int j = 15 - i; j < 15; j++){
                preBiasLeftPos[(i << 4) + j] = 14 - i;
            }
        }
        for(int i = 0; i < 15; i++){
            for(int j = 0; j < i; j++){
                preBiasRightPos[(i << 4) + j] = j;
            }
            for(int j = i; j < 15; j++){
                preBiasRightPos[(i << 4) + j] = i;
            }
        }

        // 斜线定位转实际定位
        for(int i = 0; i < 29; i++){
            for(int j = 0; j < 15; j++){
                int row = i < 15? i:14;
                row -= j;
                int col = i < 15? j:j + i - 14;
                biasLeftToPos[i][j] = (row << 4) + col;
            }
        }
        for(int i = 0; i < 29; i++){
            for(int j = 0; j < 15; j++){
                int row = i < 15? j:j + i - 14;
                int col = i < 15? 14 - i:0;
                col += j; 
                biasRightToPos[i][j] = (row << 4) + col;
            }
        }

        // 预置次幂
        for(int i = 0; i < 15; i++){
            for(int j = 0; j < 3; j++){
                presetPow[i][j] = (int) Math.pow(3, i);
                presetPow[i][j] *= j;
            }
        }

        long start = System.currentTimeMillis();
        ACautomata acauto = new ACautomata();
        for(int i = 0; i <= 14348906; i++){
            presetScore[i] = acauto.matchScore(new StringBuilder(Integer.toString(i, 3)).reverse().toString(), 15);
        }
        System.out.println(System.currentTimeMillis() - start);
    }

    public static int getOpponent(int role){
        return (role & 1) + 1;
    }

    public static int getRow(int pos){
        // 对应y
        return pos >> 4;
    }

    public static int getCol(int pos){
        // 对应x
        return pos & 15;
    }
}
