package net.createmod.catnip.render;

import java.util.function.Supplier;

import org.jetbrains.annotations.UnknownNullability;

import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class BlendModeFilteringBakedModel extends ForwardingBakedModel {
	private static final ThreadLocal<BlendModeFilteringBakedModel> INSTANCE = ThreadLocal.withInitial(BlendModeFilteringBakedModel::new);

	public static BakedModel wrap(BakedModel model, BlendMode blendMode) {
		BlendModeFilteringBakedModel wrapper = INSTANCE.get();
		wrapper.wrapped = model;
		wrapper.blendMode = blendMode;
		return wrapper;
	}

	@UnknownNullability
	private BlendMode blendMode;

	private BlendModeFilteringBakedModel() {
	}

	private boolean transform(MutableQuadView quad) {
		return quad.material().blendMode() == this.blendMode;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		context.pushTransform(this::transform);
		super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		context.popTransform();
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
		context.pushTransform(this::transform);
		super.emitItemQuads(stack, randomSupplier, context);
		context.popTransform();
	}
}
