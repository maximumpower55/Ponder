package net.createmod.catnip.platform.services;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

public interface ModFluidHelper<R> {
	int getColor(Fluid fluid, long amount, @Nullable CompoundTag fluidData);

	int getLuminosity(Fluid fluid, long amount, @Nullable CompoundTag fluidData);

	@Nullable
	TextureAtlasSprite getStillTexture(Fluid fluid, long amount, @Nullable CompoundTag fluidData);

	boolean isLighterThanAir(Fluid fluid);

	default R toStack(Fluid fluid, long amount) {
		return toStack(fluid, amount, null);
	}

	R toStack(Fluid fluid, long amount, @Nullable CompoundTag fluidData);
}
