package cn.wen.gobang.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import cn.wen.gobang.App;

public class Menu extends JFrame{
    private static final long serialVersionUID = 1L;

    public Menu(){
        Container maincon = getContentPane();
		Box box = Box.createVerticalBox();
		box.add(Box.createVerticalStrut(30));
		box.add(Box.createHorizontalStrut(90));

		JLabel topic = new JLabel("五子棋");
		topic.setFont(new Font("Dialog ", 1, 50));
		topic.setForeground(Color.BLUE);
		box.add(topic);

		JButton start = new JButton("开始游戏");
		start.setFont(new Font("Dialog ", 1, 25));
		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				close();
				App.game.open();
			}
		});
		box.add(Box.createVerticalStrut(20));
		box.add(start);

		JButton setting = new JButton("选        项");
		setting.setFont(new Font("Dialog ", 1, 25));
		setting.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Setting(App.menu);
			}
		});
		box.add(Box.createVerticalStrut(20));
		box.add(setting);

		JButton quit = new JButton("退        出");
		quit.setFont(new Font("Dialog ", 1, 25));
		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		box.add(Box.createVerticalStrut(20));
		box.add(quit);

		maincon.add(box, BorderLayout.NORTH);
		setResizable(false);
		setSize(300, 400);
		setTitle("五子棋");
		setLocation(500, 100);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
	
    public void open() {
		setVisible(true);
	}

	public void close() {
		setVisible(false);
	}
}
