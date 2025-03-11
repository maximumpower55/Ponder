package net.createmod.catnip.platform.services;

import java.util.Locale;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.createmod.catnip.render.ShadedBlockSbbBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public interface ModClientHooksHelper {

	Locale getCurrentLocale();

	void enableStencilBuffer(RenderTarget renderTarget);

	ShadedBlockSbbBuilder createSbbBuilder();

	@Nullable BakedModel filterModelForRenderType(BlockState state, BakedModel model, RenderType layer);

	void renderVirtualBlockStateModel(BakedModel model, PoseStack ms, BlockState state, Function<RenderType, VertexConsumer> bufferMap);

	void renderFullFluidState(PoseStack ms, MultiBufferSource.BufferSource buffer, FluidState fluid);

	void vertexConsumerPutBulkDataWithAlpha(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad, float red,
											float green, float blue, float alpha, int packedLight, int packedOverlay);

	void renderGuiGameElementModel(BlockRenderDispatcher blockRenderer, MultiBufferSource.BufferSource buffer,
								   PoseStack ms, BlockState state, BakedModel blockModel, int color, @Nullable BlockEntity BEWithModelData);

	<T extends ParticleOptions> Particle createParticleFromData(T data, ClientLevel level, double x, double y, double z,
																double mx, double my, double mz);

	Minecraft getMinecraftFromScreen(Screen screen);

	default boolean isKeyPressed(KeyMapping mapping) {
		return mapping.isDown();
	}

	default BlockRenderDispatcher getBlockRenderDispatcher() {
		return Minecraft.getInstance().getBlockRenderer();
	}
}
