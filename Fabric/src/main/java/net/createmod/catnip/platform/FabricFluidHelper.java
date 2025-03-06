package net.createmod.catnip.platform;

import org.jetbrains.annotations.Nullable;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.createmod.catnip.platform.services.ModFluidHelper;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

public class FabricFluidHelper implements ModFluidHelper<FluidStack> {
	@Override
	public int getColor(Fluid fluid, long amount, @Nullable CompoundTag fluidData) {
		return FluidVariantRendering.getColor(FluidVariant.of(fluid, fluidData));
	}

	@Override
	public int getLuminosity(Fluid fluid, long amount, @Nullable CompoundTag fluidData) {
		return FluidVariantAttributes.getLuminance(FluidVariant.of(fluid, fluidData));
	}

	@Override
	@Nullable
	public TextureAtlasSprite getStillTexture(Fluid fluid, long amount, @Nullable CompoundTag fluidData) {
		// handle sprites[0] being null, FluidVariantRendering.getSprite runs Objects.requireNonNull on it.
		TextureAtlasSprite[] sprites = FluidVariantRendering.getSprites(FluidVariant.of(fluid, fluidData));
		return sprites != null ? sprites[0] : null;
	}

	@Override
	public boolean isLighterThanAir(Fluid fluid) {
		return FluidVariantAttributes.isLighterThanAir(FluidVariant.of(fluid));
	}

	@Override
	public FluidStack toStack(Fluid fluid, long amount, @Nullable CompoundTag fluidData) {
		FluidStack fluidStack = new FluidStack(fluid, amount);
		fluidStack.setTag(fluidData);
		return fluidStack;
	}
}
