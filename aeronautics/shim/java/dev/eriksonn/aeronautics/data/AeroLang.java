package dev.eriksonn.aeronautics.data;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** Lang helper stub until full Create Lang datagen port. */
public final class AeroLang {
    private MutableComponent component;

    private AeroLang(MutableComponent component) { this.component = component; }

    public static AeroLang translate(String key, Object... args) {
        String k = key;
        if (!key.contains(".")) k = "aeronautics." + key;
        else if (!key.startsWith("aeronautics.") && !key.contains(":")) k = "aeronautics." + key;
        Object[] a = new Object[args.length];
        for (int i = 0; i < args.length; i++)
            a[i] = args[i] instanceof AeroLang ? ((AeroLang) args[i]).component() : args[i];
        return new AeroLang(Component.translatable(k, a));
    }

    public static AeroLang text(String text) { return new AeroLang(Component.literal(text)); }
    public static AeroLang number(Object n) { return new AeroLang(Component.literal(String.valueOf(n))); }
    public static AeroLang blockName(BlockState state) { return new AeroLang(state.getBlock().getName().copy()); }
    public static AeroLang kilopixelGram(double v) { return text(String.format("%.2f kg", v)); }
    public static AeroLang pixelNewton(double v) { return text(String.format("%.2f N", v)); }

    public static List<Component> translatedOptions(String prefix, String... keys) {
        List<Component> out = new ArrayList<>();
        for (String k : keys) out.add(translate(prefix + "." + k).component());
        return out;
    }

    public static void emptyLine(List<? super Component> tooltip) { tooltip.add(Component.empty()); }

    public AeroLang style(ChatFormatting formatting) { component = component.withStyle(formatting); return this; }
    public AeroLang color(int rgb) { component = component.withColor(rgb); return this; }
    public AeroLang add(Component other) { component = component.copy().append(other); return this; }
    public AeroLang add(AeroLang other) { return add(other.component()); }
    public AeroLang append(String s) { return add(Component.literal(s)); }
    public AeroLang space() { return append(" "); }

    public void forGoggles(List<? super Component> tooltip) { tooltip.add(component); }
    public void forGoggles(List<? super Component> tooltip, int indent) {
        tooltip.add(Component.literal(" ".repeat(Math.max(0, indent))).append(component));
    }

    public MutableComponent component() { return component; }
    public static void registrateLang(Object provider) {}
    public String string() { return component.getString(); }
    @Override public String toString() { return string(); }
}
