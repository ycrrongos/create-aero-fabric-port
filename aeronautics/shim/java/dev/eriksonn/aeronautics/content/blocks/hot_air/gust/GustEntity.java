package dev.eriksonn.aeronautics.content.blocks.hot_air.gust;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
public class GustEntity extends Entity {
  public GustEntity(EntityType<?> type, Level level) { super(type, level); }
  public static GustEntity create(EntityType<?> type, Level level) { return new GustEntity(type, level); }
  @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
  @Override protected void readAdditionalSaveData(ValueInput input) {}
  @Override protected void addAdditionalSaveData(ValueOutput output) {}
  @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) { return false; }
}
