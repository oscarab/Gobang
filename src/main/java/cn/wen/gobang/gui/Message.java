package cn.wen.gobang.gui;

import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

public class Message extends JFrame{
    private JTextArea text;
    public Message(){
        setTitle("搜索过程信息");

        Container container = getContentPane();
        text = new JTextArea();
        text.setText("----------开始----------\n");
        text.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(text);
		
        container.add(scrollPane);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setBounds(1000, 300, 500, 400);
    }

    public void appendMessage(String str){
        text.append(str + "\n");
        text.setCaretPosition(text.getText().length());
    }

    public void open() {
		setVisible(true);
	}

	public void close() {
		setVisible(false);
	}
}
