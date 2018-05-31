package ru.ifmo.rain.khalimov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class Implementor implements JarImpler {
    /**
     * tabulation string
     */
    private final static String tab = "    ";

    /**
     * default constructor
     */
    public Implementor() {
    }


    /**
     * produces code implementing interface provided in arguments
     *
     * <p>if first argument is "-jar" then call method {@link #implementJar(Class, Path)}. <tt>args[0]</tt> - class name, <tt>args[2]</tt> - root path</p>
     * <p>else call method {@link #implement(Class, Path)}. <tt>args[0]</tt> - class name, <tt>args[1]</tt> - root path</p>
     *
     * @param args programm arguments
     */
    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.out.println("Incorrect arguments");
            return;
        }
        if (args[0].equals("-jar")) {
            if (args.length < 3) {
                System.out.println("Incorrect arguments");
                return;
            }
            try {
                new Implementor().implementJar(Class.forName(args[1]), Paths.get(args[2]));
            } catch (ClassNotFoundException e) {
                System.out.println("Class " + args[1] + " not found");
            } catch (ImplerException e) {
                System.out.println(e.getMessage());
            }
        } else {
            try {
                new Implementor().implement(Class.forName(args[0]), Paths.get(args[1]));
            } catch (ClassNotFoundException e) {
                System.out.println("Class " + args[0] + " not found");
            } catch (ImplerException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Produces code implementing interface specified by provided <tt>token</tt>.
     * <p>
     * Generated class full name should be same as full name of the type token with <tt>Impl</tt> suffix
     * added. Generated source code should be placed in the correct subdirectory of the specified
     * <tt>root</tt> directory and have correct file name. For example, the implementation of the
     * interface {@link java.util.List} should go to <tt>$root/java/util/ListImpl.java</tt>
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException when implementation cannot be
     *                                                                 generated.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("Incorrect arguments");
        }
        if (!token.isInterface()) {
            throw new ImplerException(token.getCanonicalName() + " is not interface");
        }
        if (token.getPackage() != null) {
            root = root.resolve(token.getPackage().getName().replace('.', File.separatorChar));
        }
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new ImplerException("Can't create directories : " + root.toString());
        }
        root = root.resolve(token.getSimpleName() + "Impl.java");

        try (BufferedWriter writer = new BufferedWriter(Files.newBufferedWriter(root))) {
            writePackage(token.getPackage(), writer);
            writeClass(token, writer);
        } catch (IOException e) {
            throw new ImplerException("Can't write to file");
        }
    }

    /**
     * writes package path using writer
     *
     * @param path   package path to write
     * @param writer a {@link java.io.Writer}
     * @throws IOException if writer can't write to file
     */
    private void writePackage(Package path, Writer writer) throws IOException {
        if (path != null) {
            writer.write("package " + path.getName().replace(File.separatorChar, '.') + ";\n\n");
        }
    }

    /**
     * writes class code using writer
     *
     * @param clazz  class to write
     * @param writer a {@link java.io.Writer}
     * @throws IOException if writer can't write to file
     */
    private void writeClass(Class<?> clazz, Writer writer) throws IOException {
        writer.write("public class "
                + clazz.getSimpleName()
                + "Impl implements "
                + clazz.getCanonicalName()
                + " {\n"
        );
        for (Method method : clazz.getMethods()) {
            writeMethod(method, writer);
        }

        writer.write("}\n");
    }

    /**
     * writes method code using writer
     *
     * @param method method to write
     * @param writer a {@link java.io.Writer}
     * @throws IOException if writer can't write to file
     */
    private void writeMethod(Method method, Writer writer) throws IOException {
        writer.write(tab + Modifier.toString(method.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT) + " ");
        writer.write(method.getReturnType().getCanonicalName() + " " + method.getName());
        writeParameters(method.getParameters(), writer);
        writeExceptions(method.getExceptionTypes(), writer);
        writer.write(" {\n" + tab + tab + "return"
                + getDefaultReturnValue(method.getReturnType())
                + ";\n" + tab + "}\n\n"
        );
    }

    /**
     * Writes parameters separated by comma using writer
     *
     * @param parameters parameters to write
     * @param writer     a {@link Writer}
     * @throws IOException if writer can't write to file
     */
    private void writeParameters(Parameter[] parameters, Writer writer) throws IOException {
        writer.write("(");
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                writer.write(", ");
            }
            writer.write(parameters[i].getType().getCanonicalName() + " "
                    + parameters[i].getName()
            );
        }
        writer.write(")");
    }

    /**
     * Writes exceptions separated by comma
     *
     * @param exceptions exceptions to write
     * @param writer     a {@link Writer}
     * @throws IOException if writer can't write to file
     */
    private void writeExceptions(Class<?>[] exceptions, Writer writer) throws IOException {
        for (int i = 0; i < exceptions.length; i++) {
            if (i > 0) {
                writer.write(", ");
            } else {
                writer.write(" throws ");
            }
            writer.write(exceptions[i].getCanonicalName());
        }
    }

    /**
     * returns default value of class
     *
     * @param clazz class to get default value for
     * @return <ul>
     * <li>"" - for void</li>
     * <li>" true" - for boolean</li>
     * <li>" 0" - for primitives</li>
     * <li>" null" for other types</li>
     * </ul>
     */
    private String getDefaultReturnValue(Class<?> clazz) {
        if (clazz.equals(void.class)) {
            return "";
        } else if (clazz.equals(boolean.class)) {
            return " true";
        } else if (clazz.isPrimitive()) {
            return " 0";
        } else {
            return " null";
        }
    }

    /**
     * Produces <tt>.jar</tt> file implementing interface specified by provided <tt>token</tt>.
     * <p>
     * Generated class full name is same as full name of the type token with <tt>Impl</tt> suffix
     * added.
     *
     * @param token   type token to create implementation for.
     * @param jarFile target <tt>.jar</tt> file.
     * @throws ImplerException when implementation cannot be generated.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path tmp;
        try {
            tmp = Files.createTempDirectory(".");
        } catch (IOException e) {
            throw new ImplerException();
        }
        implement(token, tmp);
        compile(tmp, token);
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream writer = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            writer.putNextEntry(new ZipEntry(token.getName().replace('.', '/') + "Impl.class"));
            Files.copy(getPath(token, tmp, ".class"), writer);
        } catch (IOException e) {
            throw new ImplerException("Can't write to file : " + jarFile.toString());
        }
    }

    /**
     * compiles implementation of token
     *
     * @param root  directory where is the source file
     * @param token type token to compile implementation for
     * @throws ImplerException if compiler doesn't exist or compilation error occurred
     */
    private void compile(Path root, Class<?> token) throws ImplerException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("JavaCompiler not found");
        }
        int exitCode = compiler.run(null, null, null, getPath(token, root, ".java").toString());
        if (exitCode != 0) {
            throw new ImplerException("Nonzero compiler exit code : " + exitCode);
        }
    }

    /**
     * returns full path to class
     *
     * @param token     type token to get full path to implementation for
     * @param root      root directory of implementation
     * @param extension specify type of implementation file (.class or .java)
     * @return full path to implementation of token
     */
    private Path getPath(Class<?> token, Path root, String extension) {
        final String path = token.getName().replace('.', File.separatorChar) + "Impl" + extension;
        if (root == null) {
            return Paths.get(path);
        }
        return root.resolve(path);
    }
}
