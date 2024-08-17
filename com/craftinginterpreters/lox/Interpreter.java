package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.TokenType.*;

class Interpreter implements Expr.Visitor<Object> {

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                return -(double)right;
        }

        // Unreachable.
        return null;
    }

    public boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    public boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;

        // Calling .equals on null results in null pointer exception.
        // This protects against that.
        if (a == null) return false; 

        return a.equals(b);
    }

    public Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case GREATER:
                return (double)left > (double)right;
            case GREATER_EQUAL:
                return (double)left >= (double)right;
            case LESS:
                return (double)left < (double)right;
            case LESS_EQUAL:
                return (double)left <= (double)right;
            case MINUS:
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
            case SLASH:
                return (double)left / (double)right;
            case STAR:
                return (double)left * (double)right;
        }

        // Unreachable.
        return null;
    }


    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        // This is my best-effort attempt at this. This is now a part
        // of the interface, so I have to implement something in order
        // to compile the project at all.

        Boolean predicate = (boolean)evaluate(expr.predicate);

        // Do we have to evaluate both sides? I guess we can just
        // evaluate the side that we need instead? I don't think
        // we'd need to retain side effects in both branches?
        if (predicate) {
            return evaluate(expr.if_true);
        }
        return evaluate(expr.if_false);
    }


    // NOTE I was on "Runtime Errors"
    // https://craftinginterpreters.com/evaluating-expressions.html#runtime-errors


}
