package net.azisaba.clsrefloc;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClassScanner {
    private final Set<String> classes;
    private final List<String> results = new ArrayList<>();
    private final List<Throwable> errors = new ArrayList<>();

    public ClassScanner(Set<String> classes) {
        this.classes = classes;
    }

    public List<String> getResults() {
        return results;
    }

    public List<Throwable> getErrors() {
        return errors;
    }

    public void scan(File file) throws IOException {
        if (file.isDirectory()) {
            File[] list = file.listFiles();
            if (list != null) {
                for (File f : list) {
                    scan(f);
                }
            }
            return;
        }
        if (file.getName().endsWith(".jar") || file.getName().endsWith(".zip")) {
            scanJar(file);
        }
    }

    private void scanJar(File file) throws IOException {
        if (!file.isFile()) {
            return;
        }
        if (!file.getName().endsWith(".jar") && !file.getName().endsWith(".zip")) {
            return;
        }
        try (var zip = new ZipFile(file)) {
            var e = zip.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                if (!entry.getName().endsWith(".class")) {
                    continue;
                }
                try {
                    scan(new ClassReader(zip.getInputStream(entry)));
                } catch (Exception ex) {
                    errors.add(new RuntimeException("Failed to scan " + entry.getName() + " in " + file, ex));
                }
            }
        }
    }

    private void scan(ClassReader cr) {
        cr.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        if (classes.contains(owner)) {
                            results.add(cr.getClassName() + " -> " + owner + "#" + name + descriptor);
                        }
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    }
                };
            }
        }, 0);
    }
}
