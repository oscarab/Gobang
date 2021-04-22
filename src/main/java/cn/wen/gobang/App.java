package cn.wen.gobang;

import cn.wen.gobang.AI.Util;
import cn.wen.gobang.gui.Menu;
import cn.wen.gobang.gui.GameGUI;

public class App 
{
    public static Menu menu;
	public static GameGUI game;
    public static void main( String[] args )
    {
        Util.load();
    }
}
