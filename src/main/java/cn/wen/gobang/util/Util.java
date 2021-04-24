package cn.wen.gobang.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import cn.wen.gobang.App;

public class Util {
    public static char preGenerate[][][] = new char[15][32768][2];
    public static boolean preWin[] = new boolean[32768];

    public static char preBiasLeftIndex[] = new char[256];
    public static char preBiasRightIndex[] = new char[256];
    public static char preBiasLeftPos[] = new char[256];
    public static char preBiasRightPos[] = new char[256];

    public static short biasLeftToPos[][] = new short[30][15];
    public static short biasRightToPos[][] = new short[30][15];

    public static int presetScore[] = new int[14348907];

    public static int presetPow[][] = new int[15][3];

    public static void initPreset(){
        // 生成着法预置数组
        for(int i = 0; i < 15; i++){
            for(int bit = 0; bit < 32768; bit++){
                int maskLeft = 1 << i, maskRight = 1 << i;
                preGenerate[i][bit][0] = (char)i;
                preGenerate[i][bit][1] = (char)i;
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
                preBiasLeftIndex[(i << 4) + j] = (char)(i + j);
                preBiasRightIndex[(i << 4) + j] = (char)(14 - j + i);
            }
        }

        // 斜线上的定位数组
        for(int i = 0; i < 15; i++){
            for(int j = 0; j < 15 - i; j++){
                preBiasLeftPos[(i << 4) + j] = (char)j;
            }
            for(int j = 15 - i; j < 15; j++){
                preBiasLeftPos[(i << 4) + j] = (char)(14 - i);
            }
        }
        for(int i = 0; i < 15; i++){
            for(int j = 0; j < i; j++){
                preBiasRightPos[(i << 4) + j] = (char)j;
            }
            for(int j = i; j < 15; j++){
                preBiasRightPos[(i << 4) + j] = (char)i;
            }
        }

        // 斜线定位转实际定位
        for(int i = 0; i < 29; i++){
            for(int j = 0; j < 15; j++){
                int row = i < 15? i:14;
                row -= j;
                int col = i < 15? j:j + i - 14;
                biasLeftToPos[i][j] = (short) ((row << 4) + col);
            }
        }
        for(int i = 0; i < 29; i++){
            for(int j = 0; j < 15; j++){
                int row = i < 15? j:j + i - 14;
                int col = i < 15? 14 - i:0;
                col += j; 
                biasRightToPos[i][j] = (short) ((row << 4) + col);
            }
        }

        // 预置次幂
        for(int i = 0; i < 15; i++){
            for(int j = 0; j < 3; j++){
                presetPow[i][j] = (int) Math.pow(3, i);
                presetPow[i][j] *= j;
            }
        }

        try {
            ObjectInputStream f = new ObjectInputStream(App.class.getClassLoader().getResourceAsStream("data.txt"));
            presetScore = (int[]) f.readObject();
            f.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void saveScore(){
        ACautomata acauto = new ACautomata();
        for(int i = 0; i <= 14348906; i++){
            presetScore[i] = acauto.matchScore(new StringBuilder(Integer.toString(i, 3)).reverse().toString(), 15);
        }
        
        ObjectOutputStream f;
        try {
            f = new ObjectOutputStream(new FileOutputStream("F:/data.txt"));
            f.writeObject(presetScore);
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
