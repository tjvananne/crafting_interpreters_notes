package com.craftinginterpreters.lox;


public class ReversePolish implements Expr.Visitor<String> {
    
    String print(Expr expr) {
        return expr.accept(this);
    }

    // TODO: ch06 challenge 02 - Revisit this once we know how if statements work
    @Override
    public String visitTernaryExpr(Expr.Ternary expr) {
        return build_string("ternary", expr.predicate,
                            expr.if_true, expr.if_false);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return build_string(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return build_string(expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return build_string(expr.operator.lexeme, expr.right);
    }

    private String build_string(String op, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        for (Expr expr : exprs) {
            builder.append(expr.accept(this));
            builder.append(" ");
        }
        builder.append(op);
        
        return builder.toString();
    }

    private String build_string(Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        for (Expr expr : exprs) {
            builder.append(expr.accept(this));
        }

        return builder.toString();
    }

    public static void main(String[] args) {
        // input: (1 + 2) * (4 - 3)
        // desired output: 1 2 + 4 3 - *
        Expr expression = new Expr.Binary(
            new Expr.Grouping(
                new Expr.Binary(
                    new Expr.Literal(1),
                    new Token(TokenType.PLUS, "+", null, 1),
                    new Expr.Literal(2)
                )
            ),
            new Token(TokenType.STAR, "*", null, 1),
            new Expr.Grouping(
                new Expr.Binary(
                    new Expr.Literal(4),
                    new Token(TokenType.MINUS, "-", null, 1),
                    new Expr.Literal(3)
                )
            )
        );
        System.out.println(new ReversePolish().print(expression));
    }

}
