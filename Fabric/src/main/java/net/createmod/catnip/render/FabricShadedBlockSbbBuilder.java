package net.createmod.catnip.render;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;

import com.mojang.blaze3d.vertex.MeshData;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.createmod.ponder.mixin.client.accessor.BufferBuilderAccessor;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class FabricShadedBlockSbbBuilder implements ShadedBlockSbbBuilder {
	private final WrapperModel wrapperModel = new WrapperModel();
	private final ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(512);
	private final IntList shadeSwapVertices = new IntArrayList();

	private BufferBuilder bufferBuilder;
	private boolean currentShade;

	@Override
	public BufferBuilder getBuffer(boolean shade) {
		this.swapShade(shade);
		return this.bufferBuilder;
	}

	@Override
	public void begin() {
		this.bufferBuilder = new BufferBuilder(this.byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		this.shadeSwapVertices.clear();
		this.currentShade = true;
	}

	@Override
	public void bufferBlock(BlockAndTintGetter blockView, BakedModel model, BlockState state, BlockPos pos, PoseStack stack, RandomSource random) {
		stack.pushPose();
		stack.translate(pos.getX(), pos.getY(), pos.getZ());
		this.wrapperModel.setWrapped(model);
		Minecraft.getInstance().getBlockRenderer().getModelRenderer()
			.tesselateBlock(blockView, this.wrapperModel, state, pos, stack, this.bufferBuilder, true, random, state.getSeed(pos), OverlayTexture.NO_OVERLAY);
		stack.popPose();
	}

	@Override
	public SuperByteBuffer end() {
		this.wrapperModel.setWrapped(null);

		MeshData data = this.bufferBuilder.build();
		TemplateMesh mesh;
		if (data != null) {
			mesh = new MutableTemplateMesh(data);
			data.close();
		} else {
			mesh = new TemplateMesh(0);
		}

		return new ShadeSeparatingSuperByteBuffer(mesh, this.shadeSwapVertices.toIntArray());
	}

	private void swapShade(boolean shade) {
		if (shade != this.currentShade) {
			this.shadeSwapVertices.add(((BufferBuilderAccessor) this.bufferBuilder).catnip$getVertices());
			this.currentShade = shade;
		}
	}

	private void prepareForGeometry(RenderMaterial material) {
		this.swapShade(!material.disableDiffuse());
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
