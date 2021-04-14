package cn.wen.gobang;

import cn.wen.gobang.AI.Util;

public class App 
{
    public static GameGUI game1;
	public static GobangGUI game2;
    public static void main( String[] args )
    {
        Util.initPreset();
        game1 = new GameGUI();
		game2 = new GobangGUI();
        game1.open();
    }
}
