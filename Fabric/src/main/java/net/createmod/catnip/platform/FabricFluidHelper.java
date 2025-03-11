package net.createmod.catnip.platform;

import net.minecraft.core.component.DataComponentPatch;

import org.jetbrains.annotations.Nullable;

import net.createmod.catnip.platform.services.ModFluidHelper;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

public class FabricFluidHelper implements ModFluidHelper {
	@Override
	public int getColor(Fluid fluid, long amount, @Nullable DataComponentPatch fluidData) {
		return FluidVariantRendering.getColor(FluidVariant.of(fluid, fluidData));
	}

	@Override
	public int getLuminosity(Fluid fluid, long amount, @Nullable DataComponentPatch fluidData) {
		return FluidVariantAttributes.getLuminance(FluidVariant.of(fluid, fluidData));
	}

	@Override
	@Nullable
	public TextureAtlasSprite getStillTexture(Fluid fluid, long amount, @Nullable DataComponentPatch fluidData) {
		// handle sprites[0] being null, FluidVariantRendering.getSprite runs Objects.requireNonNull on it.
		TextureAtlasSprite[] sprites = FluidVariantRendering.getSprites(FluidVariant.of(fluid, fluidData));
		return sprites != null ? sprites[0] : null;
	}

	@Override
	public boolean isLighterThanAir(Fluid fluid) {
		return FluidVariantAttributes.isLighterThanAir(FluidVariant.of(fluid));
	}
}
