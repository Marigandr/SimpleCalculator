package com.wukkerteam.simplecalculator;

import android.icu.math.BigDecimal;
import android.icu.text.DecimalFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.util.Log;

import java.math.BigInteger;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private TextView screen;
    private String display = "";
    private String currentOperator = "";
    private String result = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        screen = (TextView) findViewById(R.id.textView);
        screen.setText(display);
    }

    private void updateScreen() {
        screen.setText(display);
    }

    public void onClickNumber(View v) {
        if (result != "") {
            clear();
            updateScreen();
        }
        Button b = (Button) v;
        display += b.getText();
        updateScreen();
    }

    private boolean isOperator(char op) {
        switch (op) {
            case '+':
            case '-':
            case '×':
            case '÷':
            case '%':
                return true;
            default:
                return false;
        }
    }

    public void onClickOperator(View v) {
        if (display == "") return;
        Button b = (Button) v;

        if (result != "") {
            String _display = result;
            clear();
            display = _display;
        }

        if (currentOperator != "") {
            Log.d("Calc", "" + display.charAt(display.length() - 1));
            if (isOperator(display.charAt(display.length() - 1))) {
                display = display.replace(display.charAt(display.length() - 1), b.getText().charAt(0));
                updateScreen();
                return;
            } else {
                getResult();
                display = result;
                result = "";
            }
            currentOperator = b.getText().toString();
        }
        display += b.getText();
        currentOperator = b.getText().toString();
        updateScreen();
    }

    private void clear() {
        display = "";
        currentOperator = "";
        result = "";
    }

    public void onClickClear(View v) {
        clear();
        updateScreen();
    }

    private BigDecimal operate(String a, String b, String op) {
        switch (op) {
            case "+":
                return new BigDecimal(a).add(new BigDecimal(b));
            case "-":
                return new BigDecimal(a).subtract(new BigDecimal(b));
            case "×":
                return new BigDecimal(a).multiply(new BigDecimal(b));
            case "%":
                return new BigDecimal(a).multiply(new BigDecimal(b)).divide(new BigDecimal("100"),4, BigDecimal.ROUND_HALF_UP);
            case "÷":
                try {
                    return new BigDecimal(a).divide(new BigDecimal(b), 4, BigDecimal.ROUND_HALF_UP);
                } catch (ArithmeticException e) {
                    Log.d("Division by zero!", e.getMessage());
                    display += "\n Infinity";
                    screen.setText(display);
                }
            default:
                return new BigDecimal("0");
        }
    }

    private String resultFormat(BigDecimal res) {
        double resToDouble = res.doubleValue();
        if (String.valueOf(resToDouble).length() < 10) {
            if (resToDouble > (int) resToDouble) {
                return String.valueOf(new DecimalFormat("0.####").format(res));
            } else {
                return String.valueOf(res.toBigInteger());
            }
        } else return String.valueOf(new DecimalFormat("0.####E0").format(res));
    }

    private boolean getResult() {
        if (currentOperator == "") return false;
        String[] operation = display.split(Pattern.quote(currentOperator));
        if (operation.length < 2) return false;
        BigDecimal tempResult = operate(operation[0], operation[1], currentOperator);
        result = resultFormat(tempResult);
        return true;
    }

    public void onClickEqual(View v){
        if(display == "") return;
        if(!getResult()) return;
        screen.setText(String.valueOf(result));
    }

    public void onClickDot(View v) {
        if (result != "") {
            clear();
            updateScreen();
        }
        Button b = (Button) v;
        display += b.getText();
        updateScreen();
    }
}
