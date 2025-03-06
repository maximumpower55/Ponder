package net.createmod.catnip.platform;

import java.util.Locale;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import io.github.fabricators_of_create.porting_lib.mixin.accessors.client.accessor.ParticleEngineAccessor;
import io.github.fabricators_of_create.porting_lib.models.virtual.FixedColorTintingBakedModel;
import net.createmod.catnip.platform.services.ModClientHooksHelper;
import net.createmod.catnip.render.BasicFluidRenderer;
import net.createmod.catnip.render.FabricShadedBlockSbbBuilder;
import net.createmod.catnip.render.LayerFilteringBakedModel;
import net.createmod.catnip.render.MultiLayerModelRenderer;
import net.createmod.catnip.render.ShadedBlockSbbBuilder;
import net.createmod.ponder.utility.VertexUtils;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class FabricClientHooksHelper implements ModClientHooksHelper {
	@Override
	public Locale getCurrentLocale() {
		return Minecraft.getInstance().getLanguageManager().getSelectedJavaLocale();
	}

	@Override
	public void enableStencilBuffer(RenderTarget renderTarget) {
		renderTarget.enableStencil();
	}

	@Override
	public ShadedBlockSbbBuilder createSbbBuilder() {
		return new FabricShadedBlockSbbBuilder();
	}

	@Override
	@Nullable
	public BakedModel filterModelForRenderType(BlockState state, BakedModel model, RenderType layer) {
		if (model.isVanillaAdapter()) {
			if (ItemBlockRenderTypes.getChunkRenderType(state) != layer) {
				model = null;
			}
		} else {
			model = LayerFilteringBakedModel.wrap(model, layer);
		}

		return model;
	}

	@Override
	public void renderVirtualBlockStateModel(BakedModel model, PoseStack ms, BlockState state, Function<RenderType, VertexConsumer> bufferMap) {
		MultiLayerModelRenderer.render(model, ms, state, bufferMap);
	}

	@Override
	public void renderFullFluidState(PoseStack ms, MultiBufferSource.BufferSource buffer, FluidState fluid) {
		BasicFluidRenderer.renderFluidBox(fluid.getType(), 1000, 0, 0, 0, 1, 1, 1, buffer, ms, LightTexture.FULL_BRIGHT, false, true);
	}

	@Override
	public void vertexConsumerPutBulkDataWithAlpha(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
		VertexUtils.putBulkData(consumer, pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
	}

	@Override
	public void renderGuiGameElementModel(BlockRenderDispatcher blockRenderer, MultiBufferSource.BufferSource bufferSource,
										  PoseStack ms, BlockState state, BakedModel model, int color, BlockEntity BEwithModelData) {
		int blockColor = Minecraft.getInstance()
				.getBlockColors()
				.getColor(state, null, null, 0);
		model = FixedColorTintingBakedModel.wrap(model, blockColor == -1 ? color : blockColor);

		MultiLayerModelRenderer.render(model, ms, state, bufferSource::getBuffer);
	}

	@Override
	public <T extends ParticleOptions> Particle createParticleFromData(T data, ClientLevel level, double x, double y, double z, double mx, double my, double mz) {
		int key = BuiltInRegistries.PARTICLE_TYPE.getId(data.getType());
		ParticleProvider<T> particleProvider = (ParticleProvider<T>) ((ParticleEngineAccessor) Minecraft.getInstance().particleEngine).port_lib$getProviders().get(key);
		return particleProvider == null ? null : particleProvider.createParticle(data, level, x, y, z, mx, my, mz);
	}

	@Override
	public Minecraft getMinecraftFromScreen(Screen screen) {
		return Screens.getClient(screen);
	}

	@Override
	public boolean isKeyPressed(KeyMapping mapping) {
		int keyCode = KeyBindingHelper.getBoundKeyOf(mapping).getValue();
		long window = Minecraft.getInstance().getWindow().getWindow();
		return InputConstants.isKeyDown(window, keyCode);
	}
}
