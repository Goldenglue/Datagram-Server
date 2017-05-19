package serverpack;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IvanOP on 04.05.2017.
 */
public class ServerUI extends JFrame {
    private JFrame jFrame;
    private JPanel jPanel;
    private JTextField jTextField;
    private static String messageToClient = "";

    ServerUI() {
        jFrame = new JFrame("Server");
        setBackground(Color.BLACK);
        jPanel = new JPanel();
        jPanel.setSize(200, 200);
        createAndShowUI();
        jFrame.add(jPanel);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.pack();
        jFrame.setVisible(true);
        jFrame.setFocusable(true);

    }

    private void createAndShowUI() {
        jTextField = new JTextField(10);
        jTextField.addActionListener(actionEvent -> {
            messageToClient = jTextField.getText();
            jTextField.setText("");
        });
        jPanel.add(jTextField);
    }

    static String getMessageToClient() {
        String temp = messageToClient;
        messageToClient = "";
        return temp;
    }

}
