package cn.wen.gobang.AI;

import java.util.LinkedList;
import java.util.Queue;

public class ACautomata {
    private final String[] black = {
        "11111", "011110", "011100", "001110", "011010", "010110", "11110", "01111", 
        "11011", "10111", "11101", "001100", "001010", "010100", "11000", "00011", "00100", "00100"
    };
    private final String[] white = {
        "22222", "022220", "022200", "002220", "022020", "020220", "22220", "02222", 
        "22022", "20222", "22202", "002200", "002020", "020200", "22000", "00022", "00200", "00200"
    };
    private final int[] scoreTable = {
        50000, 4320, 720, 720, 720, 720, 720, 720, 720, 720, 720,
        120, 120, 120, 20, 20, 20, 20,
        -50000, -4320, -720, -720, -720, -720, -720, -720, -720, -720, -720,
        -120, -120, -120, -20, -20, -20, -20
    };
    private node root;

    public ACautomata(){
        root = new node(0);
        initTree();
        initFail();
    }

    private void initTree(){
        for(int i = 0; i < 18; i++){
            node p = root;
            int len = black[i].length();
            for(int j = 0; j < len; j++){
                int num = black[i].charAt(j) - '0';
                if(p.next[num] == null){
                    p.next[num] = new node(num);
                }
                p = p.next[num];
            }
            p.end = true;
            p.index = i;
        }
    
        for(int i = 0; i < 18; i++){
            node p = root;
            int len = white[i].length();
            for(int j = 0; j < len; j++){
                int num = white[i].charAt(j) - '0';
                if(p.next[num] == null){
                    p.next[num] = new node(num);
                }
                p = p.next[num];
            }
            p.end = true;
            p.index = i + 18;
        }
    }

    private void initFail(){
        Queue<node> queue = new LinkedList<node>();
        for(int i = 0; i < 3; i++){
            if(root.next[i] != null){
                root.next[i].fail = root;
                queue.offer(root.next[i]);
            }
        }
        while (!queue.isEmpty()){
            node p = queue.poll();
            node pf = p.fail;
            for(int i = 0; i < 3; i++){
                pf = p.fail;
                if(p.next[i] != null){
                    queue.offer(p.next[i]);
                    while(true){
                        if(pf.next[p.next[i].data] != null){
                            p.next[i].fail = pf.next[p.next[i].data];
                            break;
                        }
                        else if(pf == root){
                            p.next[i].fail = root;
                            break;
                        }
                        else
                            pf = pf.fail;
                    }
                }
            }
        }
    }

    public int matchScore(String str, int length){
        int res = 0;
        node p = root;
        int len = str.length();
        for(int i = 0; i < length; i++){
            int index = i > len - 1? 0:str.charAt(i) - '0';
            while(p.next[index] == null && p != root) p = p.fail;
            p = p.next[index];
            p = (p == null)? root:p;
            node temp = p;
            while(temp != root && temp.end){
                res += scoreTable[temp.index];
                temp = temp.fail;
            }
        }
        return res;
    }

    public class node{
        int data;
        boolean end;
        int index;
        node fail;
        node[] next = new node[3];
        public node(int data){
            this(data, false, 0);
        }
        public node(int data, boolean end, int index){
            this.data = data;
            this.end = end;
            this.index = index;
            this.fail = null;
        }
        public node clone() throws CloneNotSupportedException {
            return (node) super.clone();
        }
    }
}
