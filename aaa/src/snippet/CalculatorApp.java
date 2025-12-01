package snippet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Stack;

public class CalculatorApp extends JFrame implements ActionListener, KeyListener {

    private JTextField display;
    private StringBuilder expression = new StringBuilder();

    public CalculatorApp() {
        setTitle("계산기");
        setSize(400, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        // ===== 화면 표시 =====
        display = new JTextField();
        display.setEditable(false);
        display.setFont(new Font("Arial", Font.BOLD, 36));
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setBackground(Color.BLACK);
        display.setForeground(Color.GREEN);
        display.setPreferredSize(new Dimension(400, 80));
        display.addKeyListener(this);
        add(display, BorderLayout.NORTH);

        // ===== 버튼 영역(GridBagLayout 사용) =====
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(50, 50, 50));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5); // 버튼 사이 간격

        String[][] buttons = {
                {"C", "CE", "←", "%"},
                {"7", "8", "9", "/"},
                {"4", "5", "6", "*"},
                {"1", "2", "3", "-"},
                {"0", ".", "x²", "+"},
                {"(", ")", "=", "="} // = 버튼 2칸 차지
        };

        for (int row = 0; row < buttons.length; row++) {
            for (int col = 0; col < buttons[row].length; col++) {
                String text = buttons[row][col];
                JButton btn = new JButton(text);
                btn.setFont(new Font("Arial", Font.BOLD, 24));
                btn.setBackground(new Color(80, 80, 80));
                btn.setForeground(Color.WHITE);
                btn.setFocusPainted(false);
                btn.addActionListener(this);

                c.gridx = col;
                c.gridy = row;

                // = 버튼이 마지막 행 2칸 차지
                if (text.equals("=") && col == 2) {
                    c.gridwidth = 2;
                    panel.add(btn, c);
                    break; // 다음 칸은 = 버튼에 포함되므로 skip
                } else if (!(text.equals("=") && col == 3)) {
                    c.gridwidth = 1;
                    panel.add(btn, c);
                }
            }
        }

        add(panel, BorderLayout.CENTER);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        switch (cmd) {
            case "C":
                expression.setLength(0);
                display.setText("");
                break;
            case "CE":
            case "←":
                if (expression.length() > 0) {
                    expression.setLength(expression.length() - 1);
                    display.setText(expression.toString());
                }
                break;
            case "=":
                try {
                    double result = evaluateExpression(expression.toString());
                    display.setText(String.valueOf(result));
                    expression.setLength(0);
                    expression.append(result);
                } catch (Exception ex) {
                    display.setText("Error");
                }
                break;
            case "x²":
                expression.append("^2");
                display.setText(expression.toString());
                break;
            default:
                expression.append(cmd);
                display.setText(expression.toString());
                break;
        }
    }

    // ===== 계산 로직 =====
    public double evaluateExpression(String exp) {
        Stack<Double> nums = new Stack<>();
        Stack<Character> ops = new Stack<>();
        StringBuilder token = new StringBuilder();

        for (int i = 0; i < exp.length(); i++) {
            char ch = exp.charAt(i);
            if (Character.isDigit(ch) || ch == '.') token.append(ch);
            else {
                if (token.length() > 0) { nums.push(Double.parseDouble(token.toString())); token.setLength(0); }
                if (ch == '(') ops.push(ch);
                else if (ch == ')') { while (ops.peek() != '(') compute(nums, ops); ops.pop(); }
                else { while (!ops.isEmpty() && priority(ops.peek()) >= priority(ch)) compute(nums, ops); ops.push(ch); }
            }
        }
        if (token.length() > 0) nums.push(Double.parseDouble(token.toString()));
        while (!ops.isEmpty()) compute(nums, ops);
        return nums.pop();
    }

    private int priority(char op){
        if(op == '^') return 2;
        if(op == '*' || op == '/' || op == '%') return 1;
        if(op == '+' || op == '-') return 0;
        return -1;
    }

    private void compute(Stack<Double> nums, Stack<Character> ops){
        char op = ops.pop();
        if(op == '^') {
            double a = nums.pop();
            nums.push(Math.pow(a, 2));
        } else {
            double b = nums.pop();
            double a = nums.pop();
            switch(op){
                case '+': nums.push(a + b); break;
                case '-': nums.push(a - b); break;
                case '*': nums.push(a * b); break;
                case '/': nums.push(a / b); break;
                case '%': nums.push(a % b); break;
            }
        }
    }

    // ===== 키보드 이벤트 =====
    @Override
    public void keyTyped(KeyEvent e){
        char ch = e.getKeyChar();
        if(Character.isDigit(ch) || "+-*/().%".indexOf(ch)>=0) processKey(String.valueOf(ch));
        else if(ch=='\n') processKey("=");
        else if(ch=='\b') processKey("←");
    }

    private void processKey(String cmd){ actionPerformed(new ActionEvent(this,0,cmd)); }

    @Override public void keyPressed(KeyEvent e){}
    @Override public void keyReleased(KeyEvent e){}

    public static void main(String[] args){
        SwingUtilities.invokeLater(CalculatorApp::new);
    }
}
