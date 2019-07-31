
package com.aidn5.hypixelutils.v1;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class HypixelUtilsMain implements Runnable {
  public static void main(String[] args) {
    EventQueue.invokeLater(new HypixelUtilsMain());
  }

  @Override
  public void run() {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setResizable(false);
    frame.setBounds(0, 9, 350, 250);
    JPanel panel = new JPanel();
    panel.add(new JLabel("Stackoverflow!"));
    frame.add(panel);

    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
