package cn.wen.gobang.AI;

public class Util {
    public static int getScore(int line, int block){
        switch (line){
            case 9:
            case 8:
            case 7:
            case 6:
            case 5:
                return 100000;      //连5
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

    public static int getOpponent(int role){
        return (role & 1) + 1;
    }
}
