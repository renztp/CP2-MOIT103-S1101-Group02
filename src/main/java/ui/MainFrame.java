package ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainFrame extends JFrame {

    public static final int WINDOW_WIDTH = 1000;
    public static final int WINDOW_HEIGHT = 680;

    public static final Color COLOR_BACKGROUND = new Color(15, 23, 42);
    public static final Color COLOR_SURFACE = new Color(30, 41, 59);
    public static final Color COLOR_CARD = new Color(36, 46, 68);
    public static final Color COLOR_ACCENT_PRIMARY = new Color(59, 130, 246);
    public static final Color COLOR_ACCENT_SUCCESS = new Color(16, 185, 129);
    public static final Color COLOR_ACCENT_DANGER = new Color(239, 68, 68);
    public static final Color COLOR_ACCENT_WARNING = new Color(245, 158, 11);
    public static final Color COLOR_TEXT_PRIMARY = new Color(226, 232, 240);
    public static final Color COLOR_TEXT_SECONDARY = new Color(148, 163, 184);
    public static final Color COLOR_BORDER = new Color(51, 65, 85);
    public static final Color COLOR_INPUT_BACKGROUND = new Color(15, 23, 42);
    public static final Color COLOR_TABLE_ROW_ODD = new Color(30, 41, 59);
    public static final Color COLOR_TABLE_ROW_EVEN = new Color(36, 46, 68);
    public static final Color COLOR_TABLE_ROW_SEL = new Color(59, 130, 246, 80);

    public static final String FONT_UI = "Segoe UI";
    public static final String FONT_MONO = "Consolas";

    private final CardLayout screenNavigator;
    private final JPanel rootContentPanel;

    public MainFrame() {
        super("MotorPH Payroll System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);

        screenNavigator = new CardLayout();
        rootContentPanel = new JPanel(screenNavigator);
        rootContentPanel.setBackground(COLOR_BACKGROUND);
        setContentPane(rootContentPanel);
    }

    public void registerScreen(JPanel panel, String screenName) {
        rootContentPanel.add(panel, screenName);
    }

    public void navigateTo(String screenName) {
        screenNavigator.show(rootContentPanel, screenName);
    }

    public static JPanel createStyledPanel(LayoutManager layout, Color backgroundColor) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(backgroundColor);
        return panel;
    }

    public static JLabel createLabel(String text, String fontName, int fontStyle, int fontSize, Color textColor) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(fontName, fontStyle, fontSize));
        label.setForeground(textColor);
        return label;
    }

    public static JTextField createStyledTextField() {
        JTextField field = new JTextField();
        styleTextField(field);
        return field;
    }

    public static void styleTextField(JTextComponent field) {
        field.setFont(new Font(FONT_UI, Font.PLAIN, 13));
        field.setBackground(COLOR_INPUT_BACKGROUND);
        field.setForeground(COLOR_TEXT_PRIMARY);
        field.setCaretColor(COLOR_TEXT_PRIMARY);
        field.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
    }

    public static JTextArea createStyledTextArea(String placeholderText) {
        JTextArea area = new JTextArea(placeholderText);
        area.setEditable(false);
        area.setFont(new Font(FONT_MONO, Font.PLAIN, 12));
        area.setBackground(COLOR_CARD);
        area.setForeground(COLOR_TEXT_PRIMARY);
        area.setCaretColor(COLOR_TEXT_PRIMARY);
        area.setBorder(new EmptyBorder(12, 12, 12, 12));
        area.setLineWrap(false);
        return area;
    }

    public static JScrollPane createStyledScrollPane(Component contentComponent) {
        JScrollPane scrollPane = new JScrollPane(contentComponent);
        scrollPane.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        scrollPane.getViewport().setBackground(COLOR_CARD);
        scrollPane.setBackground(COLOR_BACKGROUND);
        return scrollPane;
    }

    public static JButton createStyledButton(String buttonLabel, Color baseColor) {
        JButton button = new JButton(buttonLabel);
        button.setFont(new Font(FONT_UI, Font.BOLD, 12));
        button.setBackground(baseColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));

        final Color originalColor = baseColor;
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                button.setBackground(originalColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent event) {
                button.setBackground(originalColor);
            }
        });

        return button;
    }

    public static JSeparator createSeparatorLine() {
        JSeparator separator = new JSeparator();
        separator.setForeground(COLOR_BORDER);
        separator.setBackground(COLOR_BORDER);
        return separator;
    }

    public static JPanel buildApplicationHeader(String screenTitle, String screenSubtitle) {
        JPanel headerPanel = createStyledPanel(new BorderLayout(), COLOR_SURFACE);
        headerPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 2, 0, COLOR_ACCENT_PRIMARY),
                new EmptyBorder(14, 24, 14, 24)
        ));

        JPanel titleStack = createStyledPanel(new GridLayout(2, 1, 0, 2), COLOR_SURFACE);
        titleStack.add(createLabel("MotorPH Payroll System", FONT_UI, Font.BOLD, 20, COLOR_TEXT_PRIMARY));
        titleStack.add(createLabel(screenSubtitle, FONT_UI, Font.PLAIN, 12, COLOR_TEXT_SECONDARY));

        JLabel roleLabel = createLabel(screenTitle, FONT_UI, Font.BOLD, 13, COLOR_ACCENT_PRIMARY);

        headerPanel.add(titleStack, BorderLayout.WEST);
        headerPanel.add(roleLabel, BorderLayout.EAST);
        return headerPanel;
    }

    public static void applyTableStyling(JTable targetTable) {
        targetTable.setBackground(COLOR_TABLE_ROW_ODD);
        targetTable.setForeground(COLOR_TEXT_PRIMARY);
        targetTable.setFont(new Font(FONT_UI, Font.PLAIN, 13));
        targetTable.setRowHeight(28);
        targetTable.setShowGrid(false);
        targetTable.setIntercellSpacing(new Dimension(0, 0));
        targetTable.setSelectionBackground(COLOR_TABLE_ROW_SEL);
        targetTable.setSelectionForeground(COLOR_TEXT_PRIMARY);
        targetTable.setFillsViewportHeight(true);

        JTableHeader tableHeader = targetTable.getTableHeader();
        tableHeader.setBackground(COLOR_SURFACE);
        tableHeader.setForeground(COLOR_TEXT_SECONDARY);
        tableHeader.setFont(new Font(FONT_UI, Font.BOLD, 12));
        tableHeader.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_BORDER));
        tableHeader.setReorderingAllowed(false);

        targetTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int rowIndex, int columnIndex) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, columnIndex);
                setBackground(isSelected
                        ? COLOR_TABLE_ROW_SEL
                        : (rowIndex % 2 == 0 ? COLOR_TABLE_ROW_EVEN : COLOR_TABLE_ROW_ODD));
                setForeground(COLOR_TEXT_PRIMARY);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
    }

    public static void showWarningDialog(Component parent, String warningMessage) {
        JOptionPane.showMessageDialog(parent, warningMessage, "Input Validation", JOptionPane.WARNING_MESSAGE);
    }

    public static void showErrorDialog(Component parent, String errorMessage) {
        JOptionPane.showMessageDialog(parent, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
