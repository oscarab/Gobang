package cn.wen.gobang.gui;

import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import cn.wen.gobang.App;

public class Setting extends JDialog{
    public Setting(JFrame frame){
        super(frame, true);
        setTitle("选项");
        
        Container container = getContentPane();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JPanel panel = new JPanel();
        JLabel label = new JLabel("难度选择");
        label.setFont(new Font("Dialog ", 1, 15));
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setFont(new Font("Dialog ", 1, 15));
        comboBox.addItem("普通");
        comboBox.addItem("简单");
        panel.add(label);
        panel.add(comboBox);

        JPanel panel2 = new JPanel();
        JRadioButton select = new JRadioButton("显示搜索信息");
        select.setFont(new Font("Dialog ", 1, 15));
        panel2.add(select);

        JButton button = new JButton("保存");
        button.setFont(new Font("Dialog ", 1, 15));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                App.game.setMessageShow(select.isSelected());
                App.game.setHard(comboBox.getSelectedIndex() == 0);
                setVisible(false);
            }
		});

        container.add(Box.createVerticalGlue());
        container.add(panel);
        container.add(Box.createVerticalGlue());
        container.add(select);
        container.add(Box.createVerticalGlue());
        container.add(button);
        container.add(Box.createVerticalGlue());

        setBounds(300, 300, 200, 200);
        setResizable(false);
        setVisible(true);
    }
}
