package ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class LoginPanel extends JPanel {

    private final JTextField loginUsernameField;
    private final JPasswordField loginPasswordField;
    private final JLabel loginErrorLabel;
    private final JButton loginButton;

    public LoginPanel() {
        super(new GridBagLayout());
        setBackground(MainFrame.COLOR_BACKGROUND);

        JPanel loginCard = MainFrame.createStyledPanel(new GridBagLayout(), MainFrame.COLOR_SURFACE);
        loginCard.setBorder(new CompoundBorder(
                new LineBorder(MainFrame.COLOR_BORDER, 1, true),
                new EmptyBorder(40, 48, 40, 48)
        ));
        loginCard.setPreferredSize(new Dimension(400, 340));

        GridBagConstraints cardConstraints = new GridBagConstraints();
        cardConstraints.fill = GridBagConstraints.HORIZONTAL;
        cardConstraints.insets = new Insets(6, 0, 6, 0);
        cardConstraints.gridx = 0;
        cardConstraints.weightx = 1;

        JLabel titleLabel = MainFrame.createLabel(
                "MotorPH Payroll System", MainFrame.FONT_UI, Font.BOLD, 20, MainFrame.COLOR_TEXT_PRIMARY);
        JLabel subtitleLabel = MainFrame.createLabel(
                "Sign in to continue", MainFrame.FONT_UI, Font.PLAIN, 13, MainFrame.COLOR_TEXT_SECONDARY);

        cardConstraints.gridy = 0;
        loginCard.add(titleLabel, cardConstraints);
        cardConstraints.gridy = 1;
        cardConstraints.insets = new Insets(0, 0, 20, 0);
        loginCard.add(subtitleLabel, cardConstraints);
        cardConstraints.insets = new Insets(6, 0, 6, 0);

        cardConstraints.gridy = 2;
        loginCard.add(MainFrame.createLabel(
                "Username", MainFrame.FONT_UI, Font.PLAIN, 12, MainFrame.COLOR_TEXT_SECONDARY), cardConstraints);
        loginUsernameField = MainFrame.createStyledTextField();
        cardConstraints.gridy = 3;
        loginCard.add(loginUsernameField, cardConstraints);

        cardConstraints.gridy = 4;
        loginCard.add(MainFrame.createLabel(
                "Password", MainFrame.FONT_UI, Font.PLAIN, 12, MainFrame.COLOR_TEXT_SECONDARY), cardConstraints);
        loginPasswordField = new JPasswordField();
        MainFrame.styleTextField(loginPasswordField);
        cardConstraints.gridy = 5;
        loginCard.add(loginPasswordField, cardConstraints);

        loginErrorLabel = MainFrame.createLabel(
                "", MainFrame.FONT_UI, Font.PLAIN, 12, MainFrame.COLOR_ACCENT_DANGER);
        loginErrorLabel.setVisible(false);
        cardConstraints.gridy = 6;
        loginCard.add(loginErrorLabel, cardConstraints);

        loginButton = MainFrame.createStyledButton("Sign In", MainFrame.COLOR_ACCENT_PRIMARY);
        cardConstraints.gridy = 7;
        cardConstraints.insets = new Insets(12, 0, 0, 0);
        loginCard.add(loginButton, cardConstraints);

        add(loginCard);
    }

    public JTextField getLoginUsernameField() {
        return loginUsernameField;
    }

    public JPasswordField getLoginPasswordField() {
        return loginPasswordField;
    }

    public JLabel getLoginErrorLabel() {
        return loginErrorLabel;
    }

    public JButton getLoginButton() {
        return loginButton;
    }
}
