package dev.simulated_team.simulated.data;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class SimLang {
    public static Builder translate(String key, Object... args) { return new Builder(Component.literal(key)); }
    public static Builder number(Object n) { return new Builder(Component.literal(String.valueOf(n))); }
    public static void registrateLang(Object provider) {}

    public static class Builder {
        private MutableComponent c;
        public Builder(MutableComponent c) { this.c = c; }
        public MutableComponent component() { return c; }
        public Builder color(int rgb) { return this; }
        public Builder style(Object s) { return this; }
        public Builder add(Component other) { c = c.copy().append(other); return this; }
        public String string() { return c.getString(); }
    }
}
