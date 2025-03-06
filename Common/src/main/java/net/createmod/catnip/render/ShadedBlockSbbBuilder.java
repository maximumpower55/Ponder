package net.createmod.catnip.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.platform.CatnipClientServices;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface ShadedBlockSbbBuilder {
	static ShadedBlockSbbBuilder create() {
		return CatnipClientServices.CLIENT_HOOKS.createSbbBuilder();
	}


	BufferBuilder getBuffer(boolean shade);

	void begin();
	void bufferBlock(BlockAndTintGetter blockView, BakedModel model, BlockState state, BlockPos pos, PoseStack stack, RandomSource random);
	SuperByteBuffer end();
}
