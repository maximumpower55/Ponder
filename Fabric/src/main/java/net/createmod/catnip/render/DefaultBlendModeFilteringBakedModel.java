package net.createmod.catnip.render;

import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class DefaultBlendModeFilteringBakedModel extends ForwardingBakedModel {
	private static final ThreadLocal<DefaultBlendModeFilteringBakedModel> INSTANCE = ThreadLocal.withInitial(DefaultBlendModeFilteringBakedModel::new);

	public static BakedModel wrap(BakedModel model) {
		DefaultBlendModeFilteringBakedModel wrapper = INSTANCE.get();
		wrapper.wrapped = model;
		return wrapper;
	}

	public static boolean hasDefaultBlendMode(QuadView quad) {
		return quad.material().blendMode() == BlendMode.DEFAULT;
	}

	private DefaultBlendModeFilteringBakedModel() {
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		context.pushTransform(DefaultBlendModeFilteringBakedModel::hasDefaultBlendMode);
		super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		context.popTransform();
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
		context.pushTransform(DefaultBlendModeFilteringBakedModel::hasDefaultBlendMode);
		super.emitItemQuads(stack, randomSupplier, context);
		context.popTransform();
	}
}
