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
import cn.wen.gobang.ai.Game;
import cn.wen.gobang.ai.GameAlphaBeta;
import cn.wen.gobang.ai.GameMinMax;
import cn.wen.gobang.util.Movement;
import cn.wen.gobang.util.Util;

public class GameGUI extends JFrame{
    private static final long serialVersionUID = 1L;

    private JPanel board;						// 棋盘
	private JPanel gameInfo;					// 信息显示
    private Game game;							// 当前游戏
    private boolean isStart = false;			// 游戏是否开始
    private int AI = 2;							// AI执子颜色
    private int player = 1;						// 玩家执子颜色
    private int now = 0;						// 当前执子方
    private Movement AImove = Game.NONE_MOVE;	// AI走的位置

	private Message message;
	private boolean isHard = true;				// 是否为普通模式
	private boolean isOpenMessage = true;		// 是否打开信息展示

    public GameGUI(){
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
								finishGame(player);
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
				message = new Message();
				game = isHard? new GameAlphaBeta():new GameMinMax();
				if(isOpenMessage){
					game.setMessage(message);
					message.open();
				}
				board.repaint();

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
				AImove = Game.NONE_MOVE;
			}
		});
		JButton lose = new JButton("认输");
		lose.setFont(new Font("Dialog ", Font.CENTER_BASELINE, 25));
		lose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				finishGame(AI);
			}
		});
		JButton back = new JButton("返回");
		back.setFont(new Font("Dialog ", Font.CENTER_BASELINE, 25));
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
				App.menu.open();
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

		game = isHard? new GameAlphaBeta():new GameMinMax();
		updateInfo();
    }

	/**
	 * 获取按到的位置
	 * @param x	x坐标
	 * @param y	y坐标
	 * @return	int 位置
	 */
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

	/**
	 * 更新游戏状态面板
	 */
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

    public void open() {
		setVisible(true);
	}

	public void close() {
		setVisible(false);
	}

	public void setHard(boolean flag){
		isHard = flag;
	}

	public void setMessageShow(boolean flag){
		isOpenMessage = flag;
	}

	/**
	 * 游戏结束
	 * @param type	胜利方
	 */
    private void finishGame(int type){
        if(type == player){
            JOptionPane.showMessageDialog(null, "恭喜你胜利了");
        }
        else{
            JOptionPane.showMessageDialog(null, "很遗憾你输了");
        }
        AImove = Game.NONE_MOVE;
        isStart = false;
		updateInfo();
		board.repaint();
		message.close();
    }

	/**
	 * AI下子
	 * @param isFirst	是否为第一个
	 */
    public void AIact(boolean isFirst){
		// 若为电脑执子，打开AI开始下棋
		if (now == 1) { 
			if(isFirst){
				// AI下第一个子，下在中央
				game.directMove((7 << 4) + 7);
				board.repaint();
				now = 0;
				updateInfo();
				return;
			}
			Thread aithread = new Thread() {
				public void run() {
					if(game.AIthink()){
                        board.repaint();
                        finishGame(AI);
						return;
                    }
					Movement move = game.getLastMove();
                    AImove = move;
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

			drawCircle(Color.BLACK, (30 + 7 * 40), (30 + 7 * 40), 5, g2);
			drawCircle(Color.BLACK, (30 + 3 * 40), (30 + 3 * 40), 5, g2);
			drawCircle(Color.BLACK, (30 + 11 * 40), (30 + 11 * 40), 5, g2);
			drawCircle(Color.BLACK, (30 + 3 * 40), (30 + 11 * 40), 5, g2);
			drawCircle(Color.BLACK, (30 + 11 * 40), (30 + 3 * 40), 5, g2);

			// 绘画棋子
			for (int i = 0; i < 15; i++) {
				for (int j = 0; j < 15; j++) {
					if (AImove.getX() == i && AImove.getY() == j)
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
