package cn.wen.gobang.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import cn.wen.gobang.App;
import cn.wen.gobang.Game;
import cn.wen.gobang.AI.Movement;
import cn.wen.gobang.AI.Util;

public class GobangGUI extends JFrame{
    private static final long serialVersionUID = 1L;

    public JPanel board;
	public JPanel gameInfo;
    private Game game;
    private boolean isStart = false;
    private int AI = 2;
    private int player = 1;
    private int now = 0;
    private int[] highlight = {-1, -1};
	private int difficulty = 6;

    public GobangGUI(){
        Container container = getContentPane();
		Box box = Box.createHorizontalBox();
		// 添加主面板
		board = new DrawPanel();
		board.setPreferredSize(new Dimension(600, 600));
        // 添加容器界面的鼠标检测
		board.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					int x = e.getX();
					int y = e.getY();

					// 如若开始游戏且为玩家执子,检测玩家下子
					if (now == 0 && isStart) {
						int pos = getClickPosition(x, y);
						if(pos != -1){
							if (game.directMove(pos)) {
								board.repaint();
								gameEnd(player);
								return;
							}
							now = 1; // 变为电脑执子
						}
						updateInfo();
						board.repaint();
                        AIact(false);
						// 玩家选择棋子颜色
					}

				}
			}
		});

		gameInfo = new JPanel();
		gameInfo.setLayout(new BoxLayout(gameInfo, BoxLayout.Y_AXIS));
		JLabel state = new JLabel("游戏状态: 尚未开始");
		state.setFont(new Font("Dialog ", Font.CENTER_BASELINE, 25));
		JLabel role = new JLabel("电脑: ⚪    你: ⚫");
		role.setFont(new Font("Dialog ", Font.CENTER_BASELINE, 25));
		JButton startGame = new JButton("开始");
		startGame.setFont(new Font("Dialog ", Font.CENTER_BASELINE, 25));
		startGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] options = { "先手", "后手" };
				int choose = JOptionPane.showOptionDialog(null, "请选择先手或后手：", "先后手选择", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
				if(choose == 0) player = 1;
				else			player = 2;
				now = player - 1;
				AI = Util.getOpponent(player);
				isStart = true;
				updateInfo();
				AIact(true);
			}
		});
		JButton regret = new JButton("悔棋");
		regret.setFont(new Font("Dialog ", Font.CENTER_BASELINE, 25));
		regret.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				game.regret();
				board.repaint();
				highlight[0] = -1;
				highlight[1] = -1;
			}
		});
		JButton lose = new JButton("认输");
		lose.setFont(new Font("Dialog ", Font.CENTER_BASELINE, 25));
		lose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gameEnd(AI);
			}
		});
		JButton back = new JButton("返回");
		back.setFont(new Font("Dialog ", Font.CENTER_BASELINE, 25));
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
				App.game1.open();
			}
		});
		gameInfo.add(Box.createVerticalGlue());
		gameInfo.add(state);
		gameInfo.add(Box.createVerticalGlue());
		gameInfo.add(role);
		gameInfo.add(Box.createVerticalGlue());
		gameInfo.add(startGame);
		gameInfo.add(Box.createVerticalGlue());
		gameInfo.add(regret);
		gameInfo.add(Box.createVerticalGlue());
		gameInfo.add(lose);
		gameInfo.add(Box.createVerticalGlue());
		gameInfo.add(back);
		gameInfo.add(Box.createVerticalGlue());

		box.add(board);
		box.add(Box.createHorizontalGlue());
		box.add(gameInfo);
		box.add(Box.createHorizontalGlue());
		container.add(box);
		// 基本设置
		setResizable(false);
		setSize(900, 700);
		setTitle("五子棋");
		setLocation(100, 50);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        game = new Game(difficulty);
		updateInfo();
    }

	private int getClickPosition(int x, int y){
		if(x <= 600 & y <= 600){
			for (int i = 30; i <= 590; i = i + 40) {
				for (int j = 30; j <= 590; j = j + 40) {
					if (i - 15 <= x && x <= i + 15 && j - 15 <= y && y <= j + 15
							&& game.get((i - 30) / 40, (j - 30) / 40) == 0) {
						return (((j - 30) / 40) << 4) + (i - 30) / 40;
					}
				}
			}
		}
		return -1;
	}

	private void updateInfo(){
		JLabel state = (JLabel) gameInfo.getComponent(1);
		gameInfo.getComponent(7).setEnabled(false);
		gameInfo.getComponent(9).setEnabled(false);
		gameInfo.getComponent(5).setEnabled(false);

		if(isStart){
			if(now == 0) {
				gameInfo.getComponent(7).setEnabled(true);
				gameInfo.getComponent(9).setEnabled(true);
				state.setText("游戏状态: 你的回合");
			}
			else
				state.setText("游戏状态: 电脑思考");
		}
		else{
			gameInfo.getComponent(5).setEnabled(true);
			state.setText("游戏状态: 尚未开始");
		}
		JLabel role = (JLabel) gameInfo.getComponent(3);
		if(AI == 2) role.setText("电脑: ⚪    你: ⚫");
		else		role.setText("电脑: ⚫    你: ⚪");
	}

	public void setDifficulty(int val){
		difficulty = val;
		game.setMaxStep(val);
	}

    public void open() {
		setVisible(true);
	}

	public void close() {
		setVisible(false);
	}

    public void gameEnd(int type){
        if(type == player){
            JOptionPane.showMessageDialog(null, "恭喜你胜利了");
        }
        else{
            JOptionPane.showMessageDialog(null, "很遗憾你输了");
        }
        highlight[0] = -1;
        highlight[1] = -1;
        isStart = false;
		updateInfo();
        game = new Game(difficulty);
		board.repaint();
    }

    public void AIact(boolean isFirst){
		// 若为电脑执子，打开AI开始下棋
		if (now == 1) { 
			if(isFirst){
				game.directMove((7 << 4) + 7);
				board.repaint();
				updateInfo();
				now = 0;
				return;
			}
			Thread aithread = new Thread() {
				public void run() {
					if(game.AIthink()){
                        board.repaint();
                        gameEnd(AI);
						return;
                    }
					Movement ai = game.getLastMove();
                    highlight[0] = ai.getX();
					highlight[1] = ai.getY();
                    now = 0;
                    board.repaint();
					updateInfo();
				}
			};
			aithread.start();
		}
    }

    class DrawPanel extends JPanel { // 棋盘类
        private static final long serialVersionUID = 1L;

        public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2 = (Graphics2D) g;

			BasicStroke stroke = new BasicStroke(4.0f);
			g2.setStroke(stroke);

			Shape line; // 绘画棋盘
			for (int i = 0; i <= 590; i = i + 40) {
				line = new Line2D.Double(30 + i, 30, 30 + i, 590);
				g2.draw(line);
			}
			for (int i = 0; i <= 590; i = i + 40) {
				line = new Line2D.Double(30, 30 + i, 590, 30 + i);
				g2.draw(line);
			}

			// 绘画棋子
			for (int i = 0; i < 15; i++) {
				for (int j = 0; j < 15; j++) {
					if (highlight[0] == i && highlight[1] == j)
						drawCircle(Color.RED, (30 + i * 40), (30 + j * 40), 17, g2);
					if (game.get(i, j) != 0) {
						drawCircle(game.get(i, j) == 2 ? Color.WHITE : Color.BLACK, (30 + i * 40), (30 + j * 40), 15, g2);
					}
				}
			}

		}

		// 绘画圆
		void drawCircle(Color color, double x, double y, double r, Graphics2D g2) {
			Ellipse2D circle = new Ellipse2D.Double();
			circle.setFrameFromCenter(x, y, x + r, y + r);
			g2.setColor(color);
			g2.draw(circle);
			g2.fill(circle);
		}
	}
}
