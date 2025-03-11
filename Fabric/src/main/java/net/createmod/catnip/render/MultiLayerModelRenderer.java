package net.createmod.catnip.render;

import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.lib.model.baked.EmptyVirtualBlockGetter;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class MultiLayerModelRenderer implements VertexConsumer {
	private static final ThreadLocal<MultiLayerModelRenderer> INSTANCE = ThreadLocal.withInitial(MultiLayerModelRenderer::new);

	public static void render(BakedModel model, PoseStack ms, BlockState state, Function<RenderType, VertexConsumer> bufferMap) {
		MultiLayerModelRenderer instance = INSTANCE.get();
		instance.prepare(bufferMap, ItemBlockRenderTypes.getChunkRenderType(state));

		Minecraft.getInstance().getBlockRenderer().getModelRenderer()
			.tesselateBlock(EmptyVirtualBlockGetter.FULL_BRIGHT, instance.wrapModel(model), state, BlockPos.ZERO, ms, instance, false, RandomSource.create(), 42L, OverlayTexture.NO_OVERLAY);

		instance.clear();
	}

	private final WrapperModel wrapperModel = new WrapperModel();
	@UnknownNullability
	private Function<RenderType, VertexConsumer> bufferMap;
	@UnknownNullability
	private RenderType defaultLayer;

	@UnknownNullability
	private VertexConsumer bufferDelegate;

	private void prepare(Function<RenderType, VertexConsumer> bufferMap, RenderType defaultLayer) {
		this.bufferMap = bufferMap;
		this.defaultLayer = defaultLayer;
	}

	private void clear() {
		this.wrapperModel.setWrapped(null);
	}

	private BakedModel wrapModel(BakedModel model) {
		this.wrapperModel.setWrapped(model);
		return this.wrapperModel;
	}

	private void prepareForGeometry(RenderMaterial material) {
		BlendMode blendMode = material.blendMode();
		this.bufferDelegate = this.bufferMap.apply(blendMode == BlendMode.DEFAULT ? this.defaultLayer : blendMode.blockRenderLayer);
	}

	public VertexConsumer addVertex(float x, float y, float z) {
		this.bufferDelegate.addVertex(x, y, z);
		return this;
	}

	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		this.bufferDelegate.setColor(red, green, blue, alpha);
		return this;
	}

	public VertexConsumer setUv(float u, float v) {
		this.bufferDelegate.setUv(u, v);
		return this;
	}

	public VertexConsumer setUv1(int u, int v) {
		this.bufferDelegate.setUv1(u, v);
		return this;
	}

	public VertexConsumer setUv2(int u, int v) {
		this.bufferDelegate.setUv2(u, v);
		return this;
	}

	public VertexConsumer setNormal(float x, float y, float z) {
		this.bufferDelegate.setNormal(x, y, z);
		return this;
	}

	public void addVertex(float x, float y, float z, int color, float u, float v, int packedOverlay, int packedLight, float normalX, float normalY, float normalZ) {
		this.bufferDelegate.addVertex(x, y, z, color, u, v, packedOverlay, packedLight, normalX, normalY, normalZ);
	}

	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
		this.bufferDelegate.putBulkData(pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
	}

	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightness, float red, float green, float blue, float alpha, int[] lightmap, int packedOverlay, boolean readAlpha) {
		this.bufferDelegate.putBulkData(pose, quad, brightness, red, green, blue, alpha, lightmap, packedOverlay, readAlpha);
	}

	private final class WrapperModel extends ForwardingBakedModel {
		private void setWrapped(@Nullable BakedModel wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public boolean isVanillaAdapter() {
			return false;
		}

		@Override
		public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
			context.pushTransform(quad -> {
				MultiLayerModelRenderer.this.prepareForGeometry(quad.material());
				return true;
			});
			super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
			context.popTransform();
		}
	}
}
