import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class Solution {
    public static void main(String[] args) {
        Solution solution = new Solution();
        solution.compute("sin(2*(-5+1.5*4)+28)"); //expected output 0.5 6
        solution.compute("(5+3)-(6-5)"); //expected output 0.5 6
        solution.compute("sin((5+3)-(6-5))"); //expected output 0.5 6
        solution.compute("(6-5)"); //expected output 0.5 6
        solution.compute("tan(45)");  //System.out.println("1 1 - expected output");
        solution.compute("tan(-45)");  //System.out.println("-1 2 - expected output");
        solution.compute("0.305");  //System.out.println("0.3 0 - expected output");
        solution.compute("0.3051");  //System.out.println("0.31 - expected output");
        solution.compute("(0.3051)");  //System.out.println("0.31 - expected output");
        solution.compute("1+(1+(1+1)*(1+1))*(1+1)+1");  //System.out.println("12 8 - expected output");
        solution.compute("tan(44+sin(89-cos(180)^2))");  //System.out.println("1 6 - expected output");
        solution.compute("-2+(-2+(-2)-2*(2+2))");  //System.out.println("-14 8 - expected output");
        solution.compute("sin(80+(2+(1+1))*(1+1)+2)");  //System.out.println("1 7 - expected output");
        solution.compute("1+4/2/2+2^2+2*2-2^(2-1+1)");  //System.out.println("6 11 - expected output");
        solution.compute("10-2^(2-1+1)");  //System.out.println("6 4 - expected output");
        solution.compute("2^10+2^(5+5)");  //System.out.println("2048 4 - expected output");
        solution.compute("1.01+(2.02-1+1/0.5*1.02)/0.1+0.25+41.1");  //System.out.println("72.96 8 - expected output");
        solution.compute("0.000025+0.000012");  //System.out.println("0 1 - expected output");
        solution.compute("-2-(-2-1-(-2)-(-2)-(-2-2-(-2)-2)-2-2)");  //System.out.println("-3 16 - expected output");
        solution.compute("cos(3 + 19*3)");  //System.out.println("0.5 3 - expected output");
    }

    public void compute(final String expression) {
        MathParser mathParser = new MathParser();
        ArrayList<String> mathTokenList = mathParser.parse(expression);

        ReversePolishNotation reversePolishNotation = new ReversePolishNotation();
        ArrayList<String> reverseList = reversePolishNotation.parse(mathTokenList);

        String result = reversePolishNotation.calc(reverseList);
        System.out.println("Result: " + new BigDecimal(result).setScale(2, RoundingMode.HALF_UP));
    }
}

class MathParser {
    private int expInd;
    private String expression;

    public ArrayList<String> parse(String expression) {
        this.expression = expression;
        expInd = 0;
        ArrayList<String> tokenList = new ArrayList<>();
        while (expInd < expression.length()) {
            tokenList.add(getToken());
        }
        return tokenList;
    }

    private String getToken() {
        String token = "";
        if (isDelimiter(expression.charAt(expInd))) {
            token += expression.charAt(expInd);
            expInd++;
        } else if (Character.isDigit(expression.charAt(expInd))) {
            while (!isDelimiter(expression.charAt(expInd))) {
                token += expression.charAt(expInd);
                expInd++;
                if (expInd >= expression.length())
                    break;
            }
        } else if (Character.isLetter(expression.charAt(expInd))) {
            while (!isDelimiter(expression.charAt(expInd))) {
                token += expression.charAt(expInd);
                expInd++;
                if (expInd >= expression.length())
                    break;
            }
        } else {
            expInd++;
        }
        return token;
    }

    private boolean isDelimiter(char charAt) {
        if ("+-/*^() ".indexOf(charAt) != -1) {
            return true;
        }
        return false;
    }
}

class ReversePolishNotation {
    private ArrayList<String> tokenList = new ArrayList<>();

    public ArrayList<String> parse(ArrayList<String> mathTokenList) {
        Deque<String> operationStack = new ArrayDeque<>();
        ArrayList<String> postfix = new ArrayList<>();
        String prev = "";
        String curr = "";
        for (String token : mathTokenList) {
            curr = token;
            if (curr.equals(" ")) {
                continue;
            }
            if (isDigit(curr)) {
                postfix.add(curr);
            } else if (isFunction(curr)) {
                operationStack.push(curr);
//                postfix.add(curr);
            } else if (isDelimiter(curr)) {
                if (curr.equals("(")) {
                    operationStack.push(curr);
                    //Если скобка закрывающая
                } else if (curr.equals(")")) {
                    //Пока в стеке операций нам не встретилась открытая скобка
                    while (!operationStack.peek().equals("(")) {
                        //Выталкиваем операции из стека в результирующую строку
                        postfix.add(operationStack.pop());
                        //Если стек опустел, а открывающая скобка не найдена
                        if (operationStack.isEmpty()) {
                            System.out.println("Скобки не согласованы");
                            return null;
                        }
                    }
                    //Убираем открытую скобку из стека
                    operationStack.pop();
                    //Если формула закончилась скобкой, а в стеке осталась префиксная функция
                    if (!operationStack.isEmpty() && isFunction(operationStack.peek())) {
                        //Добавляем ее из стека в результирующую строку
                        postfix.add(operationStack.pop());
                    }
                }
            } else if (isOperator(curr)) {
                if (curr.equals("-") && (prev.equals("") || (isDelimiter(prev) && !prev.equals(")")))) {
                    curr = "u-";
                } else {
                    while (!operationStack.isEmpty() && (priority(curr) <= priority(operationStack.peek()))) {
                        postfix.add(operationStack.pop());
                    }
                }
                operationStack.push(curr);
            }
            prev = curr;
        }

        while (!operationStack.isEmpty()) {
            if (isOperator(operationStack.peek())) {
                postfix.add(operationStack.pop());
            } else {
                System.out.println("Скобки не согласованы.");
                return null;
            }
        }
        return postfix;
    }

    public String calc(ArrayList<String> postfix) {
        Deque<Double> stack = new ArrayDeque<Double>();
        Double b = 0.0;
        Double a = 0.0;
        for (String val : postfix) {
            switch (val) {
                case "sin":
                    stack.push(round(java.lang.Math.sin(java.lang.Math.toRadians(stack.pop()))));
                    break;
                case "cos":
                    stack.push(round(java.lang.Math.cos(java.lang.Math.toRadians(stack.pop()))));
                    break;
                case "tan":
                    stack.push(round(java.lang.Math.tan(java.lang.Math.toRadians(stack.pop()))));
                    break;
                case "^":
                    b = stack.pop();
                    a = stack.pop();
                    stack.push(round(java.lang.Math.pow(a, b)));
                    break;
                case "*":
                    stack.push(round(stack.pop() * stack.pop()));
                    break;
                case "/":
                    b = stack.pop();
                    a = stack.pop();
                    stack.push(round(a / b));
                    break;
                case "+":
                    stack.push(stack.pop() + stack.pop());
                    break;
                case "-":
                    b = stack.pop();
                    a = stack.pop();
                    stack.push(a - b);
                    break;
                case "u-":
                    stack.push(-stack.pop());
                    break;
                default: stack.push(round(Double.valueOf(val)));
            }
        }
        return Double.toString(stack.pop());
    }

    private Double round(Double value) {
        return new BigDecimal(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }

    private boolean isDigit(String token) {
        if (token.matches("\\d+\\.*\\d*")) {
            return true;
        }
        return false;
    }

    private boolean isDelimiter(String token) {
//        String delimiters = "+-/*^()";
        String delimiters = "()";
        for (int i = 0; i < delimiters.length(); i++) {
            if (token.charAt(0) == delimiters.charAt(i)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOperator(String token) {
        String operators = "+-/*^";

        if (token.equals("u-")) {
            return true;
        }

        for (int i = 0; i < operators.length(); i++) {
            if (token.charAt(0) == operators.charAt(i)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFunction(String token) {
        if (token.equals("sin") || token.equals("cos") || token.equals("tan")) return true;
        return false;
    }

    public int priority(String token) {
        switch (token) {
            case "(":
                return 1;
            case ")":
                return 2;
            case "+":
            case "-":
                return 3;
            case "*":
            case "/":
                return 4;
            case "^":
                return 5;
            default:
                return 6;
        }
    }
}
