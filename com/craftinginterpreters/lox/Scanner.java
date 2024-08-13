package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// Note: static imports are not best practice!
import static com.craftinginterpreters.lox.TokenType.*;

class Scanner {
    private final String source;

    // The "<>()" is "the diamond"
    // https://docs.oracle.com/javase/tutorial/java/generics/types.html#diamond
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int comment_block_count = 0;

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    // This is the real "heart" of the scanner:
    private void scanToken() {
        char c = advance();
        switch (c) {
            // single-char tokens
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            // multi-char tokens; I'm guessing match will be a look-ahead
            // that doesn't "consume" the character in the same manner as advance()?
            // Oh, no need to look ahead because "current" is already the index of
            // the next character in the source.
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            // Ah, ok so peek() is what I was thinking match would be. peek()
            // is a non-consuming look-ahead.
            case '/':
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    comment_block();
                } else {
                    addToken(SLASH);
                }
                break;
            
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace
                break;
            
            case '\n':
                line++;
                break;
            
            case '"': string(); break;
            case '?': addToken(QUESTION); break;
            case ':': addToken(COLON); break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }


    private void comment_block() {
        // Challenge 4; implement C-style /* ... */ comment blocks.
        // Bonus: allow them to nest within each other.
        // Thoughts: I need to track the count of occurrences of `/*`
        // for nesting. I also need to track newlines, much like we do
        // in the multi-line string logic.

        comment_block_count++;
        while (comment_block_count > 0) {
            // first iteration will be the char immediately after 
            // the '*' of the opening comment block
            char c = advance();
            if (isAtEnd()) {
                Lox.error(line, "Unterminated comment block.");
                return;
            }
            if (c == '\n') line++;
            if (c == '/' && peek() == '*') comment_block_count++;
            if (c == '*' && peek() == '/') {
                comment_block_count--;

                // consume the '/' in the final '*/' which closes
                // out this instance of a block comment
                if (comment_block_count == 0) advance();
            }
        }
    }

    private void identifier() {
        // Load the whole identifier. Determine if it's a reserved
        // keyword in the language. Return either the type of that
        // reserved keyword or the IDENTIFIER type.
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "." and continue consuming numbers
            advance();
            while (isDigit(peek())) advance();
        }

        addToken(NUMBER,
            Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        // If Lox supported escapce sequences like \n, then we'd
        // unescape those here.
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private boolean match(char expected) {
        // match is like a conditional advance()
        // only advance if the character is what we expected
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        // Inspect the next character to be consumed without actually
        // consuming it.
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        // Inspect 1 character after the next character to consume
        // without actually consuming it. Useful for parsing numeric
        // literals.
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }
}
