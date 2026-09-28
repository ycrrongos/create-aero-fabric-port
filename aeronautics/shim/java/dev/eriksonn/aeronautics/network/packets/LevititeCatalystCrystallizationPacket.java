package dev.eriksonn.aeronautics.network.packets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
public record LevititeCatalystCrystallizationPacket() implements CustomPacketPayload {
  public static final Type<LevititeCatalystCrystallizationPacket> TYPE =
      new Type<>(Identifier.fromNamespaceAndPath("aeronautics", "levitite_catalyst"));
  public static final StreamCodec<RegistryFriendlyByteBuf, LevititeCatalystCrystallizationPacket> STREAM_CODEC =
      StreamCodec.unit(new LevititeCatalystCrystallizationPacket());
  @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
  public static void handle(LevititeCatalystCrystallizationPacket packet, Object ctx) {}
}
