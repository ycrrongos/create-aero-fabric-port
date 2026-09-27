package dev.simulated_team.simulated.content.blocks.rope.strand.server;
import net.minecraft.core.BlockPos;
import java.util.UUID;
public class RopeAttachment {
    public RopeAttachment() {}
    public RopeAttachment(RopeAttachmentPoint point, UUID subLevel, BlockPos pos) {}
    public RopeAttachmentPoint getPoint() { return RopeAttachmentPoint.START; }
    public UUID getSubLevelId() { return null; }
    public BlockPos getPos() { return BlockPos.ZERO; }
}
