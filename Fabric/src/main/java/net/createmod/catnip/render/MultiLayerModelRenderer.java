package net.createmod.catnip.render;

import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

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

	private final WrapperModel wrapperModel = new WrapperModel();
	private Function<RenderType, VertexConsumer> bufferMap;
	private RenderType defaultLayer;

	private VertexConsumer bufferDelegate;

	public static void render(BakedModel model, PoseStack ms, BlockState state, Function<RenderType, VertexConsumer> bufferMap) {
		MultiLayerModelRenderer instance = INSTANCE.get();
		instance.wrapperModel.setWrapped(model);
		instance.bufferMap = bufferMap;
		instance.defaultLayer = ItemBlockRenderTypes.getChunkRenderType(state);

		Minecraft.getInstance().getBlockRenderer().getModelRenderer()
			.tesselateBlock(EmptyVirtualBlockGetter.FULL_BRIGHT, instance.wrapperModel, state, BlockPos.ZERO, ms, instance, false, RandomSource.create(), 42L, OverlayTexture.NO_OVERLAY);
	}

	private void prepareForGeometry(RenderMaterial material) {
		BlendMode blendMode = material.blendMode();
		this.bufferDelegate = this.bufferMap.apply(blendMode == BlendMode.DEFAULT ? this.defaultLayer : blendMode.blockRenderLayer);
	}

	@Override
	public VertexConsumer vertex(double x, double y, double z) {
		this.bufferDelegate.vertex(x, y, z);
		return this;
	}

	@Override
	public VertexConsumer color(int red, int green, int blue, int alpha) {
		this.bufferDelegate.color(red, green, blue, alpha);
		return this;
	}

	@Override
	public VertexConsumer uv(float u, float v) {
		this.bufferDelegate.uv(u, v);
		return this;
	}

	@Override
	public VertexConsumer overlayCoords(int u, int v) {
		this.bufferDelegate.overlayCoords(u, v);
		return this;
	}

	@Override
	public VertexConsumer uv2(int u, int v) {
		this.bufferDelegate.uv2(u, v);
		return this;
	}

	@Override
	public VertexConsumer normal(float x, float y, float z) {
		this.bufferDelegate.normal(x, y, z);
		return this;
	}

	@Override
	public void endVertex() {
		this.bufferDelegate.endVertex();
	}

	@Override
	public void defaultColor(int red, int green, int blue, int alpha) {
		this.bufferDelegate.defaultColor(red, green, blue, alpha);
	}

	@Override
	public void unsetDefaultColor() {
		this.bufferDelegate.unsetDefaultColor();
	}

	@Override
	public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
		this.bufferDelegate.vertex(x, y, z, red, green, blue, alpha, u, v, overlay, light, normalX, normalY, normalZ);
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, int light, int overlay) {
		this.bufferDelegate.putBulkData(pose, quad, red, green, blue, light, overlay);
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightnesses, float red, float green, float blue, int[] lights, int overlay, boolean readExistingColor) {
		this.bufferDelegate.putBulkData(pose, quad, brightnesses, red, green, blue, lights, overlay, readExistingColor);
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
