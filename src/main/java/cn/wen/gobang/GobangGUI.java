package cn.wen.gobang;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class GobangGUI extends JFrame{
    private static final long serialVersionUID = 1L;
    public JPanel mapUI;
    private Game game;
    private boolean isStart = false;
    private int AI = 0;
    private int player = 0;
    private int now = 0;
    private int[] highlight = {-1, -1};
	private int difficulty = 6;

    public GobangGUI(){
        Container maincon = getContentPane();
		Box box = Box.createHorizontalBox();
        // 添加容器界面的鼠标检测
		maincon.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					int x = e.getX();
					int y = e.getY();

					// 如若开始游戏且为玩家执子,检测玩家下子
					if (x <= 600 & y <= 600 && now == 0 && isStart) {
						outer: for (int i = 30; i <= 590; i = i + 40) {
							for (int j = 30; j <= 590; j = j + 40) {
								if (i - 15 <= x && x <= i + 15 && j - 15 <= y && y <= j + 15
										&& game.get((i - 30) / 40, (j - 30) / 40) == 0) {
									if (game.peoplePut((i - 30) / 40, (j - 30) / 40)) {
                                        mapUI.repaint();
										gameEnd(player);
                                        return;
									}
									now = 1; // 变为电脑执子
									break outer;
								}
							}
						}
						mapUI.repaint();
                        AIact();
						// 玩家选择棋子颜色
					} else if (x >= 660 && x <= 700 && y >= 200 && y <= 240 && !isStart) {
						player = 2;
						AI = 1;
						isStart = true;

						now = 1;
                        game.setAI(AI - 1);
                        game.set(7, 7, AI);
						now = 0;

						mapUI.repaint();
					} else if (x >= 760 && x <= 800 && y >= 200 && y <= 240 && !isStart) {
						player = 1;
						AI = 2;
						isStart = true;
						now = 0;
                        game.setAI(AI - 1);

						mapUI.repaint();
                        AIact();
					}

				}
			}
		});

		// 添加主面板
		mapUI = new DrawPanel();
		box.add(mapUI);
		maincon.add(box);

		// 基本设置
		setResizable(false);
		setSize(900, 700);
		setTitle("五子棋");
		setLocation(100, 50);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        game = new Game(0, difficulty);
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
        game = new Game(0, difficulty);
		mapUI.repaint();
    }

    public void AIact(){
		if (now == 1) { // 若为电脑执子，打开AI开始下棋
			Thread aithread = new Thread() {
				public void run() {
					if(game.AIput()){
                        mapUI.repaint();
                        gameEnd(AI);
                    }
					Game.node ai = game.getLast();
                    highlight[0] = ai.x;
					highlight[1] = ai.y;
                    now = 0;
                    mapUI.repaint();
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

			// 绘画基本按钮与显示
			g2.setColor(Color.BLACK);
			g2.setFont(new Font("aa", Font.BOLD, 25));
			g2.drawString("玩家：", 620, 50);
			g2.drawString("电脑：", 620, 120);
			if(isStart && now == 1)
				g2.drawString("正在思考...", 620, 190);
			else if(isStart && now == 0)
				g2.drawString("现在是你的回合", 620, 190);

			if (isStart) {
				drawCircle(player == 2 ? Color.WHITE : Color.BLACK, 720, 40, 15, g2);

				drawCircle(player == 1 ? Color.WHITE : Color.BLACK, 720, 110, 15, g2);
			} else {
				g2.drawString("请选择你下子的颜色", 620, 180);
				drawCircle(Color.WHITE, 680, 220, 20, g2);

				drawCircle(Color.BLACK, 780, 220, 20, g2);
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
