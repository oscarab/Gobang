package cn.wen.gobang;

import cn.wen.gobang.gui.Menu;
import cn.wen.gobang.util.Util;

import javax.swing.UIManager;

import cn.wen.gobang.gui.GameGUI;

public class App {
    public static Menu menu;
    public static GameGUI game;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        Util.initPreset();
        menu = new Menu();
        game = new GameGUI();
        menu.open();
    }
}
