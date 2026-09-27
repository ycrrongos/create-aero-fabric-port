package dev.simulated_team.simulated.content.blocks.rope;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ClientRopeStrand;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.ServerRopeStrand;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Compile stub — full rope logic restored after Simulated green. */
public class RopeStrandHolderBehavior extends BlockEntityBehaviour<SmartBlockEntity> {
    public static final BehaviourType<RopeStrandHolderBehavior> TYPE = new BehaviourType<>();

    public RopeStrandHolderBehavior(final SmartBlockEntity be) {
        super(be);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public boolean ownsRope() { return false; }
    @Nullable public ServerRopeStrand getOwnedStrand() { return null; }
    @Nullable public ServerRopeStrand getAttachedStrand() { return null; }
    @Nullable public ClientRopeStrand getClientStrand() { return null; }
    public Vec3 getAttachmentPoint() { return Vec3.ZERO; }
    public void receiveClientStrand(final ClientRopeStrand strand) {}
    public void receiveClientStrandStopped(final UUID id) {}
    public void attachTo(final RopeStrandHolderBehavior other) {}
    public void detach() {}

    @Override
    public void write(final ValueOutput tag, final boolean clientPacket) {
        super.write(tag, clientPacket);
    }

    @Override
    public void read(final ValueInput tag, final boolean clientPacket) {
        super.read(tag, clientPacket);
    }
}
