# crafting_interpreters_notes
My personal notes and code while following along in the Crafting Interpreters book.

Implementation notes (all from project root dir):

* Build out the `com/craftinginterpreters/lox/Lox.java` path
* Compile with `javac com/craftinginterpreters/lox/Lox.java`
* Execute with `java com.craftinginterpreters.lox.Lox [source-code-file]`
    * `[source-code-file]` is optional. Omitting it will enter the REPL in the terminal.


## Scanning

The scanner (aka "lexer") is responsible for reading each character of the raw text source code file (or REPL input) and converting that into Tokens to be used by the Parser in the next phase. Much of the scanner is simply a glorified switch statement with some extra logic. 

Part of that extra logic there disambiguates between the different types of tokens (e.g. recognizing a `<=` operator instead of a `<` and `=` separately). This is an example of the **maximal munch** principle.

> When two lexical grammar rules can both match a chunk of code that the scanner is looking at, *whichever one matches the most characters wins.*

Most of the time, we "consume" a character when reading the source code (i.e. through the `advance()` method). In order to satisfy the maximal munch principle, we sometimes need to peek ahead of the next character to determine the operator, identifier, or literal that is being referred to in the code. This is what the `peek()` and `peekNext()` methods are for.

> Maximal munch means we can't easily detect a reserved word until we've reached the end of what might instead be an identifier.

### Reflections

It makes much more sense now why an identifier such as a variable can't start with a number of symbol (other than `_`) in most languages. It would add a lot more complexity to identifying numeric literals. If I add `42x` to my source code, should this be flagged as a Lexical Error because the numeric literal contains an alpha character? Or should this be interpreted as an identifier by that name?

### Challenge 4 - nested block comments

I chose to take on the challenge of nested C-style block comments (`/* ... */`). I counted the occurrences of `/*` to keep track of how many layers of nesting I'm in at any given point. The tricky thing here is to make sure that I handle calling an extra `advance()` when my block comment counter gets to zero so that I consume the final `/`. Otherwise, the Scanner will add an errant `SLASH` token to the TokenArray of the program.

I, personally, find the nested comment blocks to be confusing when eventually writing JLox. It makes more sense for any instance of `*/` to end the block comment regardless of how many instances of `/*` there were. But I will leave the nested logic in my implementation for now.


## Representing Code (ch5)

The grammar of the Scanner was simple enough to only require a "Regular" language (in reference to the [Chomsky hierarchy](https://en.wikipedia.org/wiki/Chomsky_hierarchy), yes, _that_ Chomsky). In order to implement the parser, we'll need to go one step up in complexity and functionality, which will be a **context-free grammar** (CFG).

At this point it's useful to review what alphabet and string mean in each context so far along with which component implements that grammar.


| **Terminology**            | **Lexical grammar** | **Syntactic grammar** |
|----------------------------|---------------------|-----------------------|
| The "alphabet" is...       | Characters          | Tokens                |
| A "string" is...           | Lexeme or token     | Expression            |
| It's implemented by the... | Scanner (lexer)     | Parser                |


The Scanner looked at a linear sequence of tokens with an occasional one or two character "look-ahead". The Parser will allow for an arbitrarily deep nesting of expressions, which is why we need to move up to context-free grammar.

When considering an arithmetic expression such as `1 + 2 * 3 - 4`, it can be useful for format the order of operations into a tree:

![PEMDAS Order of Operations in tree-format](assets/Parser_PEMDAS_tree_expression.png)


> A formal grammar’s job is to specify which strings are valid and which aren’t. If we were defining a grammar for English sentences, “eggs are tasty for breakfast” would be in the grammar, but “tasty breakfast for are eggs” would probably not.
