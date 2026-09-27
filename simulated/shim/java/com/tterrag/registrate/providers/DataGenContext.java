package com.tterrag.registrate.providers;

import net.minecraft.resources.Identifier;

/** Replaces the incomplete registrate shim so registration lambdas can call get/getEntry. */
public class DataGenContext<R, T extends R> {
    public T get() {
        return null;
    }

    public T getEntry() {
        return null;
    }

    public String getName() {
        return "";
    }

    public Identifier getId() {
        return Identifier.fromNamespaceAndPath("simulated", "missing");
    }
}
