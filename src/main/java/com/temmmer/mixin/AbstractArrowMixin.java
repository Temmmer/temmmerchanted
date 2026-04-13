package com.temmmer.mixin;

import com.temmmer.CrossbowArrowDuck;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin implements CrossbowArrowDuck {

    @Unique
    private boolean temmmer$fromDoublecharge;

    @Unique
    private boolean temmmer$fromMultishot;

    @Unique
    private boolean temmmer$fromCrossbowExtra;

    @Override
    public void temmmer$setFromDoublecharge(boolean value) {
        this.temmmer$fromDoublecharge = value;
    }

    @Override
    public boolean temmmer$isFromDoublecharge() {
        return this.temmmer$fromDoublecharge;
    }

    @Override
    public void temmmer$setFromMultishot(boolean value) {
        this.temmmer$fromMultishot = value;
    }

    @Override
    public boolean temmmer$isFromMultishot() {
        return this.temmmer$fromMultishot;
    }

    @Override
    public void temmmer$setFromCrossbowExtra(boolean value) {
        this.temmmer$fromCrossbowExtra = value;
    }

    @Override
    public boolean temmmer$isFromCrossbowExtra() {
        return this.temmmer$fromCrossbowExtra;
    }
}