package dev.simulated_team.simulated.mixin.hold_interaction;
import net.minecraft.client.KeyMapping;
public interface KeyMappingInvoker {
  static void click(KeyMapping mapping) {}
  void invokeClick();
}
