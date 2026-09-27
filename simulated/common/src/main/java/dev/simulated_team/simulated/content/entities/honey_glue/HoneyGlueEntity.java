package dev.simulated_team.simulated.content.entities.honey_glue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
public class HoneyGlueEntity extends Entity {
    public HoneyGlueEntity(EntityType<?> type, Level level) { super(type, level); }
    public static HoneyGlueEntity create(EntityType<?> type, Level level) { return new HoneyGlueEntity(type, level); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override protected void readAdditionalSaveData(ValueInput input) {}
    @Override protected void addAdditionalSaveData(ValueOutput output) {}
}
