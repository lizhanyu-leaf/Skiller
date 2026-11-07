package com.leaf.skiller.util;

/**
 * A tiny recursive-descent parser and evaluator for cost rate expressions.
 * 用于消耗变化率表达式的微型递归下降解析器与求值器。
 * <p>
 * An expression is an algebraic formula over the single variable {@code x} with
 * the four basic operators, so resource costs can be scaled per instance, e.g.
 * {@code "x*1.5+2"} doubles a base cost of {@code x} and adds a flat fee.
 * 表达式是关于单个未知数 {@code x} 的代数式，支持四则运算，
 * 因此可以按实例缩放资源消耗，例如 {@code "x*1.5+2"} 表示基础成本 {@code x}
 * 上浮 50% 并附加固定费用。
 * </p>
 * <p>
 * Grammar (precedence: {@code *} and {@code /} bind tighter than {@code +} and
 * {@code -}; unary minus and parentheses are supported):
 * 语法（优先级：{@code *} 与 {@code /} 高于 {@code +} 与 {@code -}；
 * 支持一元负号与括号）：
 * </p>
 * <pre>
 * expression := term (('+' | '-') term)*
 * term       := factor (('*' | '/') factor)*
 * factor     := '-' factor | '+' factor | '(' expression ')' | 'x' | number
 * number     := digits [ '.' digits ]
 * </pre>
 * <p>
 * Division by zero yields {@code Infinity}/{@code NaN} as in plain floating-point
 * math; callers decide how to handle such results (the resource owner clamps them).
 * 除零与普通浮点运算一样产生 {@code Infinity}/{@code NaN}；
 * 由调用方决定如何处理这类结果（资源拥有者会进行钳制）。
 * </p>
 *
 * @see com.leaf.skiller.foundation.SkillResourceOwner
 * @since 1.0.0
 * @author Leaf
 */
public final class RateExpression {

    /**
     * The parsed AST root. Evaluation is a plain tree walk, so re-parsing is only
     * needed when the source text changes.
     * 解析后的AST根节点。求值就是简单的树遍历，只有源文本变化时才需要重新解析。
     */
    private final Node root;

    /**
     * The original source text, kept for diagnostics.
     * 保留原始源文本用于诊断。
     */
    private final String source;

    /**
     * Parses the given expression source.
     * 解析给定的表达式源文本。
     *
     * @param source The algebraic expression over {@code x} - 关于{@code x}的代数式
     * @return A reusable expression object - 可复用的表达式对象
     * @throws IllegalArgumentException on any syntax error, with position information - 出现语法错误时抛出，附带位置信息
     * @see #eval(double)
     * @since 1.0.0
     */
    public static RateExpression parse(String source) {
        return new RateExpression(source);
    }

    /**
     * Use {@link #parse(String)}; construction validates the whole source up front.
     * 请使用 {@link #parse(String)}；构造时会预先校验整个源文本。
     */
    private RateExpression(String source) {
        this.source = source;
        Parser parser = new Parser(source);
        this.root = parser.parseExpression();
        parser.expectEnd();
    }

    /**
     * Evaluates the expression for the given variable value.
     * 对给定的变量值求表达式的值。
     *
     * @param x The value substituted for the {@code x} variable - 替换{@code x}变量的值
     * @return The evaluated result (may be {@code Infinity}/{@code NaN} on division by zero) - 求值结果（除零时可能为{@code Infinity}/{@code NaN}）
     * @since 1.0.0
     */
    public double eval(double x) {
        return root.eval(x);
    }

    /**
     * Returns the original source text this expression was parsed from.
     * 返回本表达式解析自的原始源文本。
     *
     * @return The source text / 源文本
     * @since 1.0.0
     */
    public String source() {
        return source;
    }

    /**
     * An AST node: evaluates to a double for the given {@code x}.
     * AST节点：对给定{@code x}求值为一个double。
     */
    private interface Node {
        double eval(double x);
    }

    /**
     * Binary operator node; operator dispatch happens once per evaluation.
     * 二元运算节点；每次求值进行一次运算符分发。
     */
    private record BinaryOp(Node left, Node right, char operator) implements Node {
        @Override
        public double eval(double x) {
            return switch (operator) {
                case '+' -> left.eval(x) + right.eval(x);
                case '-' -> left.eval(x) - right.eval(x);
                case '*' -> left.eval(x) * right.eval(x);
                case '/' -> left.eval(x) / right.eval(x);
                default -> throw new IllegalStateException("Unknown operator: " + operator);
            };
        }
    }

    /**
     * The recursive-descent parser. Each grammar rule is one method, precedence by
     * call depth (expression → term → factor).
     * 递归下降解析器。每条文法规则对应一个方法，优先级由调用深度表达
     * （expression → term → factor）。
     */
    private static final class Parser {
        private final String source;
        private int pos;

        Parser(String source) {
            this.source = source;
        }

        /** expression := term (('+' | '-') term)* */
        Node parseExpression() {
            Node left = parseTerm();
            while (true) {
                skipWhitespace();
                char c = peek();
                if (c == '+' || c == '-') {
                    pos++;
                    left = new BinaryOp(left, parseTerm(), c);
                } else {
                    return left;
                }
            }
        }

        /** term := factor (('*' | '/') factor)* */
        private Node parseTerm() {
            Node left = parseFactor();
            while (true) {
                skipWhitespace();
                char c = peek();
                if (c == '*' || c == '/') {
                    pos++;
                    left = new BinaryOp(left, parseFactor(), c);
                } else {
                    return left;
                }
            }
        }

        /** factor := '-' factor | '+' factor | '(' expression ')' | 'x' | number */
        private Node parseFactor() {
            skipWhitespace();
            char c = peek();
            if (c == '-') {
                pos++;
                Node operand = parseFactor();
                return x -> -operand.eval(x);
            }
            if (c == '+') {
                pos++;
                return parseFactor();
            }
            if (c == '(') {
                pos++;
                Node inner = parseExpression();
                skipWhitespace();
                expect(')');
                return inner;
            }
            if (c == 'x' || c == 'X') {
                pos++;
                return x -> x;
            }
            if (Character.isDigit(c) || c == '.') {
                return parseNumber();
            }
            throw error("unexpected character '" + c + "'");
        }

        /** number := digits [ '.' digits ] — parsed via Double.parseDouble on the slice. */
        private Node parseNumber() {
            int start = pos;
            while (pos < source.length()
                    && (Character.isDigit(source.charAt(pos)) || source.charAt(pos) == '.')) {
                pos++;
            }
            try {
                double value = Double.parseDouble(source.substring(start, pos));
                return x -> value;
            } catch (NumberFormatException e) {
                pos = start;
                throw error("invalid number");
            }
        }

        private char peek() {
            return pos < source.length() ? source.charAt(pos) : '\0';
        }

        private void skipWhitespace() {
            while (pos < source.length() && Character.isWhitespace(source.charAt(pos))) {
                pos++;
            }
        }

        private void expect(char expected) {
            if (peek() != expected) {
                throw error("expected '" + expected + "'");
            }
            pos++;
        }

        void expectEnd() {
            skipWhitespace();
            if (pos != source.length()) {
                throw error("unexpected trailing input");
            }
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(
                    "Invalid rate expression \"" + source + "\" at " + pos + ": " + message);
        }
    }
}
