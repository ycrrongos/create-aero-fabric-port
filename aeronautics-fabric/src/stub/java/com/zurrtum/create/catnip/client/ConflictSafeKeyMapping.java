package com.zurrtum.create.catnip.client;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
public class ConflictSafeKeyMapping extends KeyMapping {
  public ConflictSafeKeyMapping(String name, int key, String category){ super(name, key, category); }
  public ConflictSafeKeyMapping(String name, InputConstants.Type type, int key, String category){ super(name, type, key, category); }
}
