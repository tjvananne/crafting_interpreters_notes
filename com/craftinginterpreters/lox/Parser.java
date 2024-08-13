
package com.craftinginterpreters.lox;

import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;


class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        // We'll use the synchronize() method here later once we 
        // have statements in the language
        try {
            return comma_operator();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr comma_operator() {
        // Challenge 01
        // comma      → expression ( "," expression)* ;  (only return right-most expression value)
        Expr expr = expression();
        while (match(COMMA)) {
            // I don't think we capture the token and concatenate into
            // a Binary expression like we do for equality through factor
            // because we want to evaluate the left expression and then
            // discard the result.
            expr = expression();
        }

        return expr;
    }

    private Expr expression() {
        // Grammar:
        // expression       → ternary ;
        return ternary();
    }

    private Expr ternary() {
        // I wrote this for ch06 challenge 02
        // Grammar:
        // ternary     -> equality ( "?" ternary ":" ternary )*
        //             | equality ;
        Expr expr = equality();
        while (match(QUESTION)) {
            Expr if_true = ternary();  // does this need to be equality? Or can it be a ternary?
            consume(COLON, "Expect ':' after '?' in ternary operator.");
            Expr if_false = ternary();
            expr = new Expr.Ternary(expr, if_true, if_false);
        }

        return expr;
    }

    private Expr equality() {
        // Grammar:
        // equality       → comparison ( ( "!=" | "==" ) comparison )* ;

        // Ch06 Challenge 03: error productions for binary operators
        // that are missing a left hand operand.
        if (match(BANG_EQUAL, EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL, PLUS)) {
            Token bad_token = previous();
            Expr discard_this = comparison(); // parse and discard the right hand operator
            throw error(bad_token, "Binary operator '" + bad_token.lexeme +
                "' requires a left-hand operand.");
        }

        Expr expr = comparison();
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            // Left-associativity implementation
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        // Grammar:
        // comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
        Expr expr = term();
        
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        
        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(STAR, SLASH)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        // Grammar:
        // unary          → ( "!" | "-" ) unary
        //                | primary ;
        if (match(BANG, MINUS)) {
                Token operator = previous();
                Expr right = unary();
                return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... tokens) {
        // Thoughts: match conditionally consumes a token
        for (TokenType token : tokens) {
            if (check(token)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

}
