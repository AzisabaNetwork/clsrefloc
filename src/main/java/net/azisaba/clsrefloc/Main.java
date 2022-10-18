package net.azisaba.clsrefloc;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

public class Main {
    private static final boolean VERBOSE = Boolean.getBoolean("verbose");

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java -jar clsrefloc.jar <classes, separated by comma, like java/lang/Object> <jar files>");
            return;
        }
        String[] dropFirst = Arrays.copyOfRange(args, 1, args.length);
        var files = Arrays.stream(dropFirst).map(File::new).collect(Collectors.toSet());
        files.stream().filter(f -> !f.exists()).forEach(f -> {
            System.err.println("File not found: " + f);
            System.exit(1);
        });
        var classes = new HashSet<>(Arrays.asList(args[0].split(",")));
        var scanner = new ClassScanner(classes);
        for (File file : files) {
            scanner.scan(file);
        }
        scanner.getResults().forEach(System.out::println);
        if (!scanner.getResults().isEmpty()) System.out.println();
        System.out.println(scanner.getResults().size() + " occurrences found.");
        if (!scanner.getErrors().isEmpty()) {
            System.out.println();
            if (!VERBOSE) {
                System.out.println(scanner.getErrors().size() + " errors suppressed, run the tool again with -Dverbose=true to show.");
            } else {
                scanner.getErrors().forEach(t -> {
                    t.printStackTrace();
                    System.out.println();
                });
            }
        }
    }
}
