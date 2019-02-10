package frontend;

import minijava.ast.MJProgram;

import java.io.FileReader;
import java.io.StringReader;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        String fileName;
        if (args.length > 0) {
            fileName = args[0];
        } else {
            System.out.println("Enter a filename: ");
            fileName = new Scanner(System.in).nextLine();
        }
        try (FileReader r = new FileReader(fileName)) {
            MJFrontend frontend = new MJFrontend();
            MJProgram prog = frontend.parse(r);
            System.out.println(prog);

            frontend.getSyntaxErrors().forEach(System.out::println);
        }
    }
}
