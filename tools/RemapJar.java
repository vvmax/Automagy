import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

/** Remaps globally unique MCP SRG member IDs, including subclass overrides. */
public final class RemapJar {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) throw new IllegalArgumentException("input.jar output.jar members.tsv");
        final Map<String, String> names = new HashMap<>();
        for (String line : Files.readAllLines(Paths.get(args[2]), StandardCharsets.UTF_8)) {
            String[] parts = line.split("\t");
            if (parts.length == 2) names.put(parts[0], parts[1]);
        }
        final Map<String, String> used = new TreeMap<>();
        final Set<String> unresolved = new TreeSet<>();
        Remapper remapper = new Remapper() {
            String member(String name) {
                String mapped = names.get(name);
                if (mapped != null) { used.put(name, mapped); return mapped; }
                if (name.matches("(?:func|field)_\\d+_.*")) unresolved.add(name);
                return name;
            }
            @Override public String mapMethodName(String owner, String name, String descriptor) {
                return member(name);
            }
            @Override public String mapFieldName(String owner, String name, String descriptor) {
                return member(name);
            }
        };
        int classes = 0, resources = 0;
        try (JarFile input = new JarFile(args[0]);
             JarOutputStream output = new JarOutputStream(new FileOutputStream(args[1]))) {
            Enumeration<JarEntry> entries = input.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;
                String name = entry.getName();
                // A rewritten archive cannot retain original signature digests.
                if (name.matches("(?i)META-INF/[^/]+\\.(SF|RSA|DSA|EC)")) continue;
                byte[] bytes;
                try (InputStream stream = input.getInputStream(entry);
                     ByteArrayOutputStream data = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[16384];
                    for (int count; (count = stream.read(buffer)) != -1;) data.write(buffer, 0, count);
                    bytes = data.toByteArray();
                }
                if (name.endsWith(".class")) {
                    ClassReader reader = new ClassReader(bytes);
                    // Rebuild the pool so unused original SRG names do not remain.
                    ClassWriter writer = new ClassWriter(0);
                    reader.accept(new ClassRemapper(writer, remapper), 0);
                    bytes = writer.toByteArray();
                    classes++;
                } else resources++;
                JarEntry rewritten = new JarEntry(name);
                rewritten.setTime(0);
                output.putNextEntry(rewritten);
                output.write(bytes);
                output.closeEntry();
            }
        }
        System.out.println("Classes: " + classes + "; resources: " + resources);
        System.out.println("Distinct mapped SRG members: " + used.size());
        for (Map.Entry<String, String> entry : used.entrySet())
            System.out.println("MAPPED\t" + entry.getKey() + "\t" + entry.getValue());
        for (String name : unresolved) System.out.println("UNRESOLVED\t" + name);
    }
}
