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
                checkNumberOperand(expr.operator, expr.right);
                return -(double)right;
        }

        // Unreachable.
        return null;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;

        // Calling .equals on null results in null pointer exception.
        // This protects against that.
        if (a == null) return false; 

        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    void interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
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
                checkNumberOperands(expr.operator, expr.left, expr.right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, expr.left, expr.right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, expr.left, expr.right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, expr.left, expr.right);
                return (double)left <= (double)right;
            case MINUS:
                checkNumberOperands(expr.operator, expr.left, expr.right);
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case SLASH:
                checkNumberOperands(expr.operator, expr.left, expr.right);
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, expr.left, expr.right);
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
