
// chapter 01; challenge 2

// package jloc;

class HelloWorld {
    public static void main(String []args) {
        // adding extra lines to practice step-through debugging
        System.out.println("My First Java Program.");
        System.out.println("My First Java Program2.");
        int x = 37;
        System.out.println(x);
        System.out.println("My First Java Program3.");
        System.out.println("My First Java Program4.");
    }
}


/*
 * Compile and run without a package:
 * `javac HelloWorld.java`
 * `java HelloWorld.class`
 * 
 * Compile and run with a package:
 * `javac -d . HelloWorld.java` (create package in current directory)
 * `java jloc.HelloWorld.class`
 */

