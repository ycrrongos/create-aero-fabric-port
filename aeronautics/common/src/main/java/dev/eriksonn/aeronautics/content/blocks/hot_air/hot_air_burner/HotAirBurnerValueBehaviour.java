package dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import dev.eriksonn.aeronautics.data.AeroLang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

public class HotAirBurnerValueBehaviour extends ScrollValueBehaviour {
    private static final MutableComponent TITLE = AeroLang.translate("generic.hot_air").component();

    private int interval = 5;
    private Supplier<Integer> minSupplier = () -> 0;
    private Supplier<Integer> maxSupplier = () -> 100;

    public HotAirBurnerValueBehaviour(final Component label, final SmartBlockEntity be, final ValueBoxTransform slot) {
        super(label, be, slot);
    }

    private void updateInterval() {
        this.interval = (this.maxSupplier.get() - this.minSupplier.get() + 250) / 500;
        this.interval *= 5;
        this.interval = Math.max(1, this.interval);
    }

    @Override
    public ValueSettingsBoard createBoard(final Player player, final BlockHitResult hitResult) {
        this.updateInterval();
        return new ValueSettingsBoard(this.label, this.maxSupplier.get() / this.interval, 10, ImmutableList.of(TITLE),
                new ValueSettingsFormatter(this::format));
    }

    private MutableComponent format(final ValueSettings valueSettings) {
        this.updateInterval();
        int value = Mth.clamp(valueSettings.value() * this.interval, this.minSupplier.get(), this.maxSupplier.get());
        return AeroLang.translate("unit.meter_cubed", value).component();
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(0, this.behaviour.getValue() / this.interval);
    }

    @Override
    public void setValueSettings(final Player player, final ValueSettings valueSetting, final boolean ctrlDown) {
        this.updateInterval();
        this.setValue(valueSetting.value() * this.interval);
    }

    public HotAirBurnerValueBehaviour between(final Supplier<Integer> min, final Supplier<Integer> max) {
        this.minSupplier = min;
        this.maxSupplier = max;
        this.behaviour.between(min.get(), max.get());
        return this;
    }

    public void setValue(final int value) {
        final int clamped = Mth.clamp(value, this.minSupplier.get(), this.maxSupplier.get());
        if (clamped == this.behaviour.getValue()) return;
        this.behaviour.setValue(clamped);
        this.blockEntity.setChanged();
        this.blockEntity.sendData();
    }

    public int getValue() {
        return this.behaviour.getValue();
    }

    public String getClipboardKey() {
        return "Hot Air";
    }
}
