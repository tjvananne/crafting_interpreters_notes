

#include <stdio.h>
#include <assert.h>
#include <stdbool.h>

// This is a supporting exploratory file for figuring out Ch06 challenge 2, which
// tells us to implement ternary operators.


int main() {

    // comma expressions cannot be used in a ternary in C
    // https://stackoverflow.com/a/16578279/3586093
    // int x = (0, 1) ? 1+1, 2+2 : 9+9, 10+10;  // <-- NOT ALLOWED

    // Ah, this makes sense, as comma expressions are the very lowest precedence:
    // https://en.cppreference.com/w/c/language/operator_precedence

    // Only "constant expressions" are allowed in the ternary.

    // Ok, so then can a ternary be used in a comma expression?
    int a = 1+1, b = 2+2;
    int x = (1) ? 1 : 3, y = (0) ? 2 : 4;


    // Conclusion:
    // A ternary expression can be used in a comma expression,
    // but a comma expression cannot be used in a ternary expression.

    // Nested ternary for associativity purposes...
    int z = 1 ? 1 ? 2 : 4 : 3; // this is fine

    int q0 = true ? 2 : 4 ? 1 ? 2 : 4 : 0 ? 2 : 3;
    int q2 = true ? 2 : 4 ? 1 ? 2 : 4 : 0 ? 2 : 3;
    int q3 = true ? 2 : 4 ? 1 ? 2 : 4 : (0 ? 2 : 3);
    int q4 = true ? 2 : 4 ? (1 ? 2 : 4) : (0 ? 2 : 3);
    int q5 = (true ? 2 : 4) ? (1 ? 2 : 4) : (0 ? 2 : 3);
    assert(q0 == q3);
    assert(q0 == q4);
    assert(q0 == q5);

    // this is actual "nesting" of ternary operators that
    // demonstrates how right-associativity works.
    int r0 = 1 ? 2 :  3 ? 1 :  2 ? 3 :  1 ? 2 : 3;
    int r1 = 1 ? 2 :  3 ? 1 :  2 ? 3 : (1 ? 2 : 3);
    int r2 = 1 ? 2 :  3 ? 1 : (2 ? 3 : (1 ? 2 : 3));
    int r3 = 1 ? 2 : (3 ? 1 : (2 ? 3 : (1 ? 2 : 3)));

}




