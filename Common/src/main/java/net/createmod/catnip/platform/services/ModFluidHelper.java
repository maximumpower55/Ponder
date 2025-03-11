package net.createmod.catnip.platform.services;

import net.minecraft.core.component.DataComponentPatch;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

public interface ModFluidHelper {
	int getColor(Fluid fluid, long amount, @Nullable DataComponentPatch fluidData);

	int getLuminosity(Fluid fluid, long amount, @Nullable DataComponentPatch fluidData);

	@Nullable
	TextureAtlasSprite getStillTexture(Fluid fluid, long amount, @Nullable DataComponentPatch fluidData);

	boolean isLighterThanAir(Fluid fluid);
}
