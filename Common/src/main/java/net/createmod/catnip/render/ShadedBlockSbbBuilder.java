package net.createmod.catnip.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.createmod.catnip.platform.CatnipClientServices;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface ShadedBlockSbbBuilder extends VertexConsumer {
	static ShadedBlockSbbBuilder create() {
		return CatnipClientServices.CLIENT_HOOKS.createSbbBuilder();
	}


	BufferBuilder unwrap(boolean shade);

	void begin();
	void renderBlock(BlockAndTintGetter blockView, BakedModel model, BlockState state, BlockPos pos, PoseStack stack, RandomSource random);
	SuperByteBuffer end();
}
