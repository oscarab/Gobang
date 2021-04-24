package cn.wen.gobang;

import cn.wen.gobang.gui.Menu;
import cn.wen.gobang.util.Util;
import cn.wen.gobang.gui.GameGUI;

public class App 
{
    public static Menu menu;
	public static GameGUI game;
    public static void main( String[] args )
    {
        Util.initPreset();
        menu = new Menu();
		game = new GameGUI();
        menu.open();
    }
}
