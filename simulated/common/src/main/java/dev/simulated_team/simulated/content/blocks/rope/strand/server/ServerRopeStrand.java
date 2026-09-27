package dev.simulated_team.simulated.content.blocks.rope.strand.server;
import java.util.*;
public class ServerRopeStrand {
    public static final double SEGMENT_LENGTH = 1.0;
    public UUID getUUID() { return new UUID(0,0); }
    public void discard() {}
    public double getExtension() { return 0; }
    public void setExtension(double e) {}
    public List<Object> getPoints() { return Collections.emptyList(); }
    public Iterable<RopeAttachment> getAttachments() { return Collections.emptyList(); }
    public RopeAttachment getAttachment(RopeAttachmentPoint point) { return new RopeAttachment(point, null, null); }
    public void setAttachment(RopeAttachment a) {}
}
