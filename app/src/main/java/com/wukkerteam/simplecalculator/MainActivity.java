package com.wukkerteam.simplecalculator;

import android.icu.math.BigDecimal;
import android.icu.text.DecimalFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private TextView screen;
    private String display = "";
    private String currentOperator = "";
    private String result = "";
    private String error = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        screen = (TextView) findViewById(R.id.textView);
        screen.setText(display);
    }

    public void onClickNumber(View v) {
        if (result != "") {
            clear();
            updateScreen();
        }
        Button btn = (Button) v;
        display += btn.getText();
        updateScreen();
    }

    // Действия для кнопок "+", "-", "×", "÷".
    public void onClickOperator(View v) {
        Button btn = (Button) v;
        if (display.isEmpty()) return;
        if (!result.isEmpty()) {
            String tempDisplay = result;
            clear();
            display = tempDisplay;
        }
        if (!currentOperator.isEmpty()) {
            Character lastChar = display.charAt(display.length() - 1);
            Character preLastChar = display.charAt(display.length() - 2);
            // Если операторов 2 подряд - удалить оба (всего таких возможных случая два: "×-" и "÷-").
            if (isOperator(lastChar) && isOperator(preLastChar)) {
                display = display.substring(0, display.length() - 2);
            } else if (isOperator(lastChar)) {
                // tempDisplay - вспомогательная строка для корректной смены оператора пользователем.
                // Решает проблему замены "-" на любой другой оператор: было "-9-" => "+9+", стало: "-9-" => "-9+"
                String tempDisplay = display.substring(1, display.length());
                display = display.charAt(0) + tempDisplay.replace(lastChar, btn.getText().charAt(0));
                updateScreen();
                return;
            } else {
                getResult();
                display = result;
                result = "";
            }
        }
        display += btn.getText().toString();
        currentOperator = btn.getText().toString();
        updateScreen();
    }

    public void onClickClear(View v) {
        clear();
        updateScreen();
    }

    public void onClickEqual(View v) {
        if (display.isEmpty() || !getResult()) return;
        if (!error.isEmpty()) {
            screen.setText(error);
            clear();
        } else {
            screen.setText(result);
        }
    }

    public void onClickDot(View v) {
        Button btn = (Button) v;
        // Проверка условия запрета на добавление ".", если в числе их больше одной.
        if ((!display.contains(".") && currentOperator.isEmpty())
                || (!currentOperator.isEmpty()
                    && display.split(Pattern.quote(currentOperator)).length > 1
                    && !display.split(Pattern.quote(currentOperator))[1].contains("."))) {
            display += btn.getText();
            updateScreen();
        }
    }

    public void onClickSignChanger(View v) {
        if (!result.isEmpty()) {
            String _display = result;
            clear();
            display = _display;
        }
        display = currentOperator.isEmpty() ? changeSignOfFirstNum() : changeSignOfSecondNum();
        updateScreen();
    }

    public void onClickPercent(View v) {
        if (display.isEmpty() || !getResult()) return;
        if (!error.isEmpty()) {
            screen.setText(error);
            clear();
        } else if (getResult()) {
            String tempDisplay = display.charAt(0) + display.substring(1, display.length()).replace(currentOperator, "!");
            String[] argums = tempDisplay.split("!");
            BigDecimal a = new BigDecimal(argums[0]);
            BigDecimal b = new BigDecimal(argums[1]);
            BigDecimal percent = a.multiply(b).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);
            switch (currentOperator) {
                case "+": result = (a.add(percent)).toString(); break;
                case "-": result = (a.subtract(percent)).toString(); break;
                case "×": result = percent.toString(); break;
                case "÷":
                    try {
                        result = (a.divide(percent, 2, BigDecimal.ROUND_HALF_UP).multiply(a)).toString();
                        break;
                    } catch (ArithmeticException e) {
                        Log.d("Попытка деления на ноль: " + a + " / 0", e.getMessage());
                        error += "Infinity";
                    }
            }
            result = chooseResultFormat(new BigDecimal(result));
            display = result;
            updateScreen();
        }
    }


    private void updateScreen() {
        if (!error.isEmpty()) {
            screen.setText(error);
            clear();
        } else {
            screen.setText(display);
        }
    }

    private void clear() {
        display = "";
        currentOperator = "";
        result = "";
        error = "";
    }

    private boolean isOperator(char op) {
        switch (op) {
            case '-':
            case '+':
            case '×':
            case '÷':
                return true;
            default:
                return false;
        }
    }

    private boolean getResult() {
        if (currentOperator.isEmpty()) return false;
        // tempDisplay - вспомогательная строка, помогающая корректно сделать split по оператору
        // В противном случае выражения вида "-X-Y" обрабатывались бы некорректно
        String tempDisplay = display.charAt(0) + display.substring(1, display.length()).replace(currentOperator, "!");
        String[] argums = tempDisplay.split("!");
        if (argums.length < 2) {
            return false;
        } else {
            BigDecimal tempResult = calculate(argums[0], argums[1], currentOperator);
            result = chooseResultFormat(tempResult);
            return true;
        }
    }

    private BigDecimal calculate(String a, String b, String op) {
        switch (op) {
            case "+": return new BigDecimal(a).add(new BigDecimal(b));
            case "-": return new BigDecimal(a).subtract(new BigDecimal(b));
            case "×": return new BigDecimal(a).multiply(new BigDecimal(b));
            case "÷":
                try {
                    return new BigDecimal(a).divide(new BigDecimal(b), 4, BigDecimal.ROUND_HALF_UP);
                } catch (ArithmeticException e) {
                    Log.d("Попытка деления на ноль: " + a + " / 0", e.getMessage());
                    error += "Infinity";
                }
            default: return new BigDecimal("0");
        }
    }

    // Определение формата вывода результата вычислений
    private String chooseResultFormat(BigDecimal res) {
        double resToDouble = res.doubleValue();
        if (String.valueOf(resToDouble).length() < 10) {
            if (Math.abs(resToDouble) > Math.abs((int) resToDouble)) {
                return String.valueOf(new DecimalFormat("0.####").format(res));
            } else {
                return String.valueOf(res.toBigInteger());
            }
        } else return String.valueOf(new DecimalFormat("0.####E0").format(res));
    }

    private String changeSignOfFirstNum() {
        if (display.isEmpty()) return "-";
        return isOperator(display.charAt(0)) ?
                display.substring(1, display.length())
                : new StringBuilder("-").append(display).toString();
    }

    private String changeSignOfSecondNum() {
        // Если один оператор, то ... , а если два оператора подряд, то ..., иначе ничего не менять.
        if (isOperator(display.charAt(display.length() - 1)) && !isOperator(display.charAt(display.length() - 2))) {
            switch (display.charAt(display.length() - 1)) {
                case '-':
                    currentOperator = "+";
                    return display.substring(0, display.length() - 1) + "+";
                case '+':
                    currentOperator = "-";
                    return display.substring(0, display.length() - 1) + "-";
                case '×':
                case '÷':
                    return display + "-";
                case '%':
                    return display;
            }
        } else if (isOperator(display.charAt(display.length() - 1)) && isOperator(display.charAt(display.length() - 2))) {
            switch (display.substring(display.length() - 2, display.length())) {
                case "×-":
                case "÷-":
                    return display.substring(0, display.length() - 1);
            }
        }
        return display;
    }
}
