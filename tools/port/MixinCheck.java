import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.zip.*;

/**
 * Static checker for mixin classes against the (named) game classpath.
 * <p>
 * Usage: java -cp asm.jar:asm-tree.jar MixinCheck.java <mixins.json>... -- <compiled classes dir> <classpath entries...>
 * <p>
 * Verifies mixin target classes, injector target methods, INVOKE / FIELD / NEW injection points (with ordinals),
 * shadows, accessors, invokers and overwrites. It cannot validate everything Mixin does at runtime (locals, slices,
 * constant matching), but it catches the vast majority of targets broken by a Minecraft update.
 */
public class MixinCheck {

    static final Map<String, byte[]> CLASSES = new HashMap<>();
    static final Map<String, ClassNode> NODES = new HashMap<>();
    static int problems = 0;
    static int checkedMixins = 0, checkedInjectors = 0, checkedPoints = 0, checkedShadows = 0;

    public static void main(String[] args) throws Exception {
        List<String> jsons = new ArrayList<>();
        int i = 0;
        for (; i < args.length && !args[i].equals("--"); i++) {
            jsons.add(args[i]);
        }
        i++;
        Path compiled = Paths.get(args[i++]);
        loadDir(compiled);
        for (; i < args.length; i++) {
            Path p = Paths.get(args[i]);
            if (Files.isDirectory(p)) {
                loadDir(p);
            } else if (Files.exists(p)) {
                loadJar(p);
            }
        }

        for (String json : jsons) {
            String text = Files.readString(Paths.get(json));
            String pkg = extract(text, "\"package\"\\s*:\\s*\"([^\"]+)\"").get(0);
            for (String section : new String[]{"mixins", "client", "server"}) {
                for (String name : extractArray(text, section)) {
                    String cls = (pkg + "." + name).replace('.', '/');
                    checkMixin(cls);
                }
            }
        }
        System.out.println("checked " + checkedMixins + " mixins, " + checkedInjectors + " injectors, " + checkedPoints + " injection points, " + checkedShadows + " shadows/accessors");
        System.out.println(problems + " problem(s)");
        System.exit(problems == 0 ? 0 : 1);
    }

    static List<String> extract(String text, String regex) {
        List<String> out = new ArrayList<>();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(regex).matcher(text);
        while (m.find()) out.add(m.group(1));
        return out;
    }

    static List<String> extractArray(String text, String key) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*\\[([^\\]]*)\\]").matcher(text);
        if (!m.find()) return List.of();
        return extract(m.group(1), "\"([^\"]+)\"");
    }

    static void loadDir(Path dir) throws IOException {
        try (Stream<Path> s = Files.walk(dir)) {
            for (Path p : (Iterable<Path>) s::iterator) {
                if (p.toString().endsWith(".class")) {
                    String name = dir.relativize(p).toString().replace(File.separatorChar, '/');
                    name = name.substring(0, name.length() - 6);
                    CLASSES.putIfAbsent(name, Files.readAllBytes(p));
                }
            }
        }
    }

    static void loadJar(Path jar) throws IOException {
        try (ZipInputStream zin = new ZipInputStream(new BufferedInputStream(Files.newInputStream(jar)))) {
            ZipEntry e;
            while ((e = zin.getNextEntry()) != null) {
                String n = e.getName();
                if (n.endsWith(".class") && !n.startsWith("META-INF")) {
                    CLASSES.putIfAbsent(n.substring(0, n.length() - 6), zin.readAllBytes());
                } else if (n.endsWith(".jar")) {
                    // nested jars (jar-in-jar)
                    byte[] data = zin.readAllBytes();
                    Path tmp = Files.createTempFile("mixincheck", ".jar");
                    Files.write(tmp, data);
                    loadJar(tmp);
                    Files.delete(tmp);
                }
            }
        }
    }

    static ClassNode node(String name) {
        if (NODES.containsKey(name)) return NODES.get(name);
        byte[] data = CLASSES.get(name);
        ClassNode node = null;
        if (data != null) {
            node = new ClassNode();
            new ClassReader(data).accept(node, 0);
        }
        NODES.put(name, node);
        return node;
    }

    static void report(String mixin, String msg) {
        problems++;
        System.out.println(mixin.replace('/', '.') + ": " + msg);
    }

    @SuppressWarnings("unchecked")
    static Object value(AnnotationNode a, String key) {
        if (a.values == null) return null;
        for (int i = 0; i < a.values.size(); i += 2) {
            if (a.values.get(i).equals(key)) return a.values.get(i + 1);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    static List<Object> list(AnnotationNode a, String key) {
        Object v = value(a, key);
        if (v == null) return List.of();
        if (v instanceof List) return (List<Object>) v;
        return List.of(v);
    }

    static List<AnnotationNode> annotations(List<AnnotationNode> a, List<AnnotationNode> b) {
        List<AnnotationNode> out = new ArrayList<>();
        if (a != null) out.addAll(a);
        if (b != null) out.addAll(b);
        return out;
    }

    static final Set<String> INJECTORS = Set.of(
            "Lorg/spongepowered/asm/mixin/injection/Inject;",
            "Lorg/spongepowered/asm/mixin/injection/Redirect;",
            "Lorg/spongepowered/asm/mixin/injection/ModifyArg;",
            "Lorg/spongepowered/asm/mixin/injection/ModifyArgs;",
            "Lorg/spongepowered/asm/mixin/injection/ModifyVariable;",
            "Lorg/spongepowered/asm/mixin/injection/ModifyConstant;",
            "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;",
            "Lcom/llamalad7/mixinextras/injector/v2/WrapWithCondition;",
            "Lcom/llamalad7/mixinextras/injector/WrapWithCondition;",
            "Lcom/llamalad7/mixinextras/injector/ModifyExpressionValue;",
            "Lcom/llamalad7/mixinextras/injector/ModifyReturnValue;",
            "Lcom/llamalad7/mixinextras/injector/ModifyReceiver;",
            "Lcom/llamalad7/mixinextras/injector/wrapmethod/WrapMethod;"
    );

    static void checkMixin(String mixinName) {
        ClassNode mixin = node(mixinName);
        if (mixin == null) {
            report(mixinName, "mixin class not found in compiled output");
            return;
        }
        List<String> targets = new ArrayList<>();
        boolean isPseudo = false;
        for (AnnotationNode a : annotations(mixin.invisibleAnnotations, mixin.visibleAnnotations)) {
            if (a.desc.equals("Lorg/spongepowered/asm/mixin/Pseudo;")) isPseudo = true;
            if (a.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")) {
                for (Object t : list(a, "value")) targets.add(((Type) t).getInternalName());
                for (Object t : list(a, "targets")) targets.add(((String) t).replace('.', '/'));
            }
        }
        if (targets.isEmpty()) {
            report(mixinName, "no @Mixin targets");
            return;
        }
        for (String target : targets) {
            ClassNode tn = node(target);
            if (tn == null) {
                if (!isPseudo) report(mixinName, "target class missing: " + target);
                continue;
            }
            checkAgainst(mixin, tn);
        }
    }

    static void checkAgainst(ClassNode mixin, ClassNode target) {
        checkedMixins++;
        String m = mixin.name;
        for (FieldNode f : mixin.fields) {
            for (AnnotationNode a : annotations(f.invisibleAnnotations, f.visibleAnnotations)) {
                if (a.desc.equals("Lorg/spongepowered/asm/mixin/Shadow;")) {
                    checkedShadows++;
                    String prefix = (String) value(a, "prefix");
                    String name = f.name;
                    if (prefix != null && name.startsWith(prefix)) name = name.substring(prefix.length());
                    else if (name.startsWith("shadow$")) name = name.substring(7);
                    List<String> names = new ArrayList<>(List.of(name));
                    for (Object alias : list(a, "aliases")) names.add((String) alias);
                    boolean found = false;
                    for (String n : names) found |= findField(target.name, n, f.desc) != null;
                    if (!found) report(m, "@Shadow field missing: " + target.name + "." + name + " " + f.desc);
                }
            }
        }

        for (MethodNode mn : mixin.methods) {
            for (AnnotationNode a : annotations(mn.invisibleAnnotations, mn.visibleAnnotations)) {
                String d = a.desc;
                if (d.equals("Lorg/spongepowered/asm/mixin/Shadow;")) {
                    checkedShadows++;
                    String prefix = (String) value(a, "prefix");
                    String name = mn.name;
                    if (prefix != null && name.startsWith(prefix)) name = name.substring(prefix.length());
                    else if (name.startsWith("shadow$")) name = name.substring(7);
                    List<String> names = new ArrayList<>(List.of(name));
                    for (Object alias : list(a, "aliases")) names.add((String) alias);
                    boolean found = false;
                    for (String n : names) found |= findMethod(target.name, n, mn.desc) != null;
                    if (!found) report(m, "@Shadow method missing: " + target.name + "." + name + mn.desc);
                } else if (d.equals("Lorg/spongepowered/asm/mixin/Overwrite;")) {
                    if (declared(target, mn.name, mn.desc) == null) {
                        report(m, "@Overwrite target missing: " + target.name + "." + mn.name + mn.desc);
                    }
                } else if (d.equals("Lorg/spongepowered/asm/mixin/gen/Accessor;")) {
                    String name = (String) value(a, "value");
                    if (name == null || name.isEmpty()) name = accessorName(mn.name);
                    Type t = Type.getMethodType(mn.desc);
                    String fdesc = t.getArgumentTypes().length == 1 ? t.getArgumentTypes()[0].getDescriptor() : t.getReturnType().getDescriptor();
                    FieldNode fn = findFieldAnyDesc(target.name, name);
                    if (fn == null) report(m, "@Accessor field missing: " + target.name + "." + name);
                    else if (!fn.desc.equals(fdesc)) report(m, "@Accessor type mismatch: " + target.name + "." + name + " is " + fn.desc + " but accessor uses " + fdesc);
                } else if (d.equals("Lorg/spongepowered/asm/mixin/gen/Invoker;")) {
                    String name = (String) value(a, "value");
                    if (name == null || name.isEmpty()) name = invokerName(mn.name);
                    String desc = mn.desc;
                    if (name.equals("<init>")) {
                        desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getArgumentTypes(mn.desc));
                    }
                    if (findMethod(target.name, name, desc) == null) report(m, "@Invoker target missing: " + target.name + "." + name + desc);
                } else if (INJECTORS.contains(d)) {
                    checkInjector(m, target, mn, a);
                }
            }
        }
    }

    static String accessorName(String n) {
        for (String p : new String[]{"get", "set", "is"}) {
            if (n.startsWith(p) && n.length() > p.length()) {
                String r = n.substring(p.length());
                if (r.equals(r.toUpperCase())) return r;
                return Character.toLowerCase(r.charAt(0)) + r.substring(1);
            }
        }
        return n;
    }

    static String invokerName(String n) {
        for (String p : new String[]{"call", "invoke", "new", "create"}) {
            if (n.startsWith(p) && n.length() > p.length()) {
                if (p.equals("new") || p.equals("create")) return "<init>";
                String r = n.substring(p.length());
                return Character.toLowerCase(r.charAt(0)) + r.substring(1);
            }
        }
        return n;
    }

    static void checkInjector(String m, ClassNode target, MethodNode handler, AnnotationNode a) {
        checkedInjectors++;
        List<Object> selectors = list(a, "method");
        boolean optional = Integer.valueOf(0).equals(value(a, "require"));
        List<MethodNode> methods = new ArrayList<>();
        for (Object s : selectors) {
            List<MethodNode> found = resolveSelector(target, (String) s);
            if (found.isEmpty()) {
                if (!optional) report(m, "injector " + handler.name + ": target method missing: " + target.name + " :: " + s);
            }
            methods.addAll(found);
        }
        if (methods.isEmpty()) return;

        List<AnnotationNode> ats = new ArrayList<>();
        for (Object at : list(a, "at")) ats.add((AnnotationNode) at);
        for (AnnotationNode at : ats) {
            String value = (String) value(at, "value");
            String tgt = (String) value(at, "target");
            Integer ordinal = (Integer) value(at, "ordinal");
            if (value == null) continue;
            String kind = value.contains(":") ? value.substring(0, value.indexOf(':')) : value;
            int count = 0;
            boolean supported = true;
            for (MethodNode method : methods) {
                if (tgt == null || tgt.isEmpty()) {
                    supported = false;
                    break;
                }
                switch (kind) {
                    case "INVOKE", "INVOKE_ASSIGN", "INVOKE_STRING" -> count += countInvokes(method, tgt);
                    case "FIELD" -> count += countFields(method, tgt);
                    case "NEW" -> count += countNews(method, tgt);
                    default -> supported = false;
                }
            }
            if (!supported) continue;
            checkedPoints++;
            if (count == 0 && !optional) {
                report(m, "injector " + handler.name + ": @At(" + value + ") target not found in " + target.name + " :: " + methods.stream().map(x -> x.name).distinct().collect(Collectors.joining(",")) + " -> " + tgt);
            } else if (ordinal != null && ordinal >= 0 && ordinal >= count / Math.max(1, methods.size()) && ordinal >= count && !optional) {
                report(m, "injector " + handler.name + ": ordinal " + ordinal + " out of range (" + count + " matches) for " + tgt);
            }
        }
    }

    static List<MethodNode> resolveSelector(ClassNode target, String sel) {
        String owner = null;
        String s = sel.trim();
        if (s.startsWith("L") && s.contains(";")) {
            owner = s.substring(1, s.indexOf(';'));
            s = s.substring(s.indexOf(';') + 1);
        }
        String name = s;
        String desc = null;
        int p = s.indexOf('(');
        if (p >= 0) {
            name = s.substring(0, p);
            desc = s.substring(p);
        } else if (s.contains(":")) {
            name = s.substring(0, s.indexOf(':'));
            desc = s.substring(s.indexOf(':') + 1);
        }
        boolean wildcard = name.endsWith("*");
        if (wildcard) name = name.substring(0, name.length() - 1);
        List<MethodNode> out = new ArrayList<>();
        for (MethodNode mn : target.methods) {
            boolean nameMatch = wildcard ? mn.name.startsWith(name) : mn.name.equals(name);
            if (!nameMatch) continue;
            if (desc != null && !desc.isEmpty()) {
                if (desc.endsWith(")") ? !mn.desc.startsWith(desc) : !mn.desc.equals(desc)) continue;
            }
            out.add(mn);
        }
        return out;
    }

    static String[] parseMember(String t) {
        String owner = null;
        String s = t.trim();
        if (s.startsWith("L") && s.contains(";")) {
            owner = s.substring(1, s.indexOf(';'));
            s = s.substring(s.indexOf(';') + 1);
        }
        String name = s, desc = null;
        int p = s.indexOf('(');
        if (p >= 0) {
            name = s.substring(0, p);
            desc = s.substring(p);
        } else if (s.contains(":")) {
            name = s.substring(0, s.indexOf(':'));
            desc = s.substring(s.indexOf(':') + 1);
        }
        return new String[]{owner, name, desc};
    }

    static int countInvokes(MethodNode method, String target) {
        String[] t = parseMember(target);
        int c = 0;
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode mi) {
                if ((t[0] == null || t[0].equals(mi.owner)) && t[1].equals(mi.name) && (t[2] == null || t[2].equals(mi.desc))) c++;
            }
        }
        return c;
    }

    static int countFields(MethodNode method, String target) {
        String[] t = parseMember(target);
        int c = 0;
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof FieldInsnNode fi) {
                if ((t[0] == null || t[0].equals(fi.owner)) && t[1].equals(fi.name) && (t[2] == null || t[2].equals(fi.desc))) c++;
            }
        }
        return c;
    }

    static int countNews(MethodNode method, String target) {
        String type = target.trim();
        if (type.startsWith("L") && type.endsWith(";")) type = type.substring(1, type.length() - 1);
        if (type.contains("(")) {
            // <init> descriptor style target: Lowner;<init>(...)V
            type = parseMember(target)[0];
        }
        int c = 0;
        for (AbstractInsnNode insn : method.instructions) {
            if (insn.getOpcode() == Opcodes.NEW && ((TypeInsnNode) insn).desc.equals(type)) c++;
        }
        return c;
    }

    static MethodNode declared(ClassNode cn, String name, String desc) {
        for (MethodNode mn : cn.methods) if (mn.name.equals(name) && mn.desc.equals(desc)) return mn;
        return null;
    }

    static MethodNode findMethod(String cls, String name, String desc) {
        Deque<String> queue = new ArrayDeque<>(List.of(cls));
        Set<String> seen = new HashSet<>();
        while (!queue.isEmpty()) {
            String c = queue.poll();
            if (c == null || !seen.add(c)) continue;
            ClassNode cn = node(c);
            if (cn == null) continue;
            MethodNode mn = declared(cn, name, desc);
            if (mn != null) return mn;
            queue.add(cn.superName);
            queue.addAll(cn.interfaces);
        }
        return null;
    }

    static FieldNode findField(String cls, String name, String desc) {
        String c = cls;
        while (c != null) {
            ClassNode cn = node(c);
            if (cn == null) return null;
            for (FieldNode f : cn.fields) if (f.name.equals(name) && f.desc.equals(desc)) return f;
            c = cn.superName;
        }
        return null;
    }

    static FieldNode findFieldAnyDesc(String cls, String name) {
        String c = cls;
        while (c != null) {
            ClassNode cn = node(c);
            if (cn == null) return null;
            for (FieldNode f : cn.fields) if (f.name.equals(name)) return f;
            c = cn.superName;
        }
        return null;
    }
}
