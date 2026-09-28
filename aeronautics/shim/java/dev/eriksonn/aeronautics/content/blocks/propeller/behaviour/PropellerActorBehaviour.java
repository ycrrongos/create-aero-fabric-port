package dev.eriksonn.aeronautics.content.blocks.propeller.behaviour;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import java.util.Collections; import java.util.List;
public class PropellerActorBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {
  public static final BehaviourType<PropellerActorBehaviour> TYPE = new BehaviourType<>();
  public PropellerActorBehaviour(SmartBlockEntity be) { super(be); }
  @Override public BehaviourType<?> getType() { return TYPE; }
  public List<PropellerLayer> getLayers() { return Collections.emptyList(); }
  public static class PropellerLayer {}
}
