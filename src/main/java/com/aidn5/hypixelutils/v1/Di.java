
package com.aidn5.hypixelutils.v1;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Di extends JFrame {
  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    File a = new File("sd/sad1.0");
    System.out.println(a.getAbsolutePath());
    System.out.println(new File(a, "asd").getAbsolutePath());
    System.exit(0);
    try {
      Di dialog = new Di();

      dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      dialog.setLocationRelativeTo(null);

      dialog.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Create the dialog.
   */
  public Di() {
    setTitle(HypixelUtils.NAME);
    setResizable(false);
    setBounds(0, 9, 350, 250);

    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout(0, 0));


    // create the north side of the dialog
    JPanel northPanel = new JPanel();
    JLabel title = new JLabel(HypixelUtils.NAME);
    title.setFont(new Font(title.getFont().getFamily(), Font.BOLD, 24));

    northPanel.add(title);
    panel.add(northPanel, BorderLayout.NORTH);


    // create the south side of the dialog
    JPanel southPanel = new JPanel();
    JButton okButton = new JButton("Ok");
    okButton.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        System.exit(0);
      }
    });
    southPanel.add(okButton);
    panel.add(southPanel, BorderLayout.SOUTH);

    getContentPane().add(panel);
  }
}
