package net.createmod.catnip.render;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.createmod.ponder.mixin.client.accessor.BufferBuilderAccessor;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class FabricShadedBlockSbbBuilder implements ShadedBlockSbbBuilder {
	private final WrapperModel wrapperModel = new WrapperModel();
	private final BufferBuilder bufferBuilder = new BufferBuilder(512);
	private final IntList shadeSwapVertices = new IntArrayList();
	private boolean currentShade;

	@Override
	public BufferBuilder unwrap(boolean shade) {
		this.swapShade(shade);
		return this.bufferBuilder;
	}

	public void begin() {
		this.bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		this.shadeSwapVertices.clear();
		this.currentShade = true;
	}

	public void renderBlock(BlockAndTintGetter blockView, BakedModel model, BlockState state, BlockPos pos, PoseStack stack, RandomSource random) {
		stack.pushPose();
		stack.translate(pos.getX(), pos.getY(), pos.getZ());
		this.wrapperModel.setWrapped(model);
		Minecraft.getInstance().getBlockRenderer().getModelRenderer()
			.tesselateBlock(blockView, this.wrapperModel, state, pos, stack, this, true, random, state.getSeed(pos), OverlayTexture.NO_OVERLAY);
		stack.popPose();
	}

	public SuperByteBuffer end() {
		this.wrapperModel.setWrapped(null);
		BufferBuilder.RenderedBuffer data = this.bufferBuilder.end();
		MutableTemplateMesh mesh = new MutableTemplateMesh(data);
		return new ShadeSeparatingSuperByteBuffer(mesh.toImmutable(), this.shadeSwapVertices.toIntArray());
	}

	private void swapShade(boolean shade) {
		if (shade != currentShade) {
			this.shadeSwapVertices.add(((BufferBuilderAccessor) this.bufferBuilder).catnip$getVertices());
			this.currentShade = shade;
		}
	}

	private void prepareForGeometry(RenderMaterial material) {
		this.swapShade(!material.disableDiffuse());
	}

	@Override
	public VertexConsumer vertex(double x, double y, double z) {
		this.bufferBuilder.vertex(x, y, z);
		return this;
	}

	@Override
	public VertexConsumer color(int red, int green, int blue, int alpha) {
		this.bufferBuilder.color(red, green, blue, alpha);
		return this;
	}

	@Override
	public VertexConsumer uv(float u, float v) {
		this.bufferBuilder.uv(u, v);
		return this;
	}

	@Override
	public VertexConsumer overlayCoords(int u, int v) {
		this.bufferBuilder.overlayCoords(u, v);
		return this;
	}

	@Override
	public VertexConsumer uv2(int u, int v) {
		this.bufferBuilder.uv2(u, v);
		return this;
	}

	@Override
	public VertexConsumer normal(float x, float y, float z) {
		this.bufferBuilder.normal(x, y, z);
		return this;
	}

	@Override
	public void endVertex() {
		this.bufferBuilder.endVertex();
	}

	@Override
	public void defaultColor(int red, int green, int blue, int alpha) {
		this.bufferBuilder.defaultColor(red, green, blue, alpha);
	}

	@Override
	public void unsetDefaultColor() {
		this.bufferBuilder.unsetDefaultColor();
	}

	@Override
	public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
		this.bufferBuilder.vertex(x, y, z, red, green, blue, alpha, u, v, overlay, light, normalX, normalY, normalZ);
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, int light, int overlay) {
		this.bufferBuilder.putBulkData(pose, quad, red, green, blue, light, overlay);
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightnesses, float red, float green, float blue, int[] lights, int overlay, boolean readExistingColor) {
		this.bufferBuilder.putBulkData(pose, quad, brightnesses, red, green, blue, lights, overlay, readExistingColor);
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
				FabricShadedBlockSbbBuilder.this.prepareForGeometry(quad.material());
				return true;
			});
			super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
			context.popTransform();
		}
	}
}
