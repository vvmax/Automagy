import com.sun.source.util.JavacTask;
import javax.tools.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/** Syntax verification only: deliberately does not pretend to typecheck dependencies. */
public final class VerifySources {
    public static void main(String[] args) throws Exception {
        List<File> files;
        try (Stream<Path> paths = Files.walk(Paths.get(args[0]))) {
            files = paths.filter(p -> p.toString().endsWith(".java"))
                         .sorted().map(Path::toFile).collect(Collectors.toList());
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fm = compiler.getStandardFileManager(diagnostics, null, null)) {
            JavacTask task = (JavacTask) compiler.getTask(null, fm, diagnostics,
                Arrays.asList("--release", "8", "-proc:none"), null,
                fm.getJavaFileObjectsFromFiles(files));
            for (Object unit : task.parse()) { /* Parse every compilation unit. */ }
        }
        long errors = diagnostics.getDiagnostics().stream()
            .filter(d -> d.getKind() == Diagnostic.Kind.ERROR).count();
        for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) System.out.println(diagnostic);
        System.out.println("Java files parsed: " + files.size() + "; syntax errors: " + errors);
        if (errors != 0) System.exit(1);
    }
}
