package net.createmod.ponder;

import org.jetbrains.annotations.Nullable;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.placement.PlacementClient;
import net.createmod.catnip.theme.Color;
import net.createmod.ponder.enums.PonderConfig;
import net.createmod.ponder.enums.PonderKeybinds;
import net.createmod.ponder.foundation.PonderTooltipHandler;
import net.createmod.ponder.utility.FabricClientResourceReloadListener;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public class FabricPonderClient implements ClientModInitializer {

	public static final FabricClientResourceReloadListener FABRIC$RESOURCE_RELOAD_LISTENER = new FabricClientResourceReloadListener();

	@SuppressWarnings("StaticNonFinalField")
	@Nullable
	public static Couple<Color> tooltipBorderColorOverride;

	@Override
	public void onInitializeClient() {
		PonderClient.init();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			PonderClient.onTick();
			PonderTooltipHandler.tick();
		});

		InvalidateRenderStateCallback.EVENT.register(() -> {
			PonderClient.invalidateRenderers();
			AnimationTickHolder.reset();
		});
		WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> PonderClient.onRenderWorld(context.matrixStack()));

		HudRenderCallback.EVENT.register((graphics, deltaTracker) -> PlacementClient.onRenderCrosshairOverlay(graphics, AnimationTickHolder.getPartialTicksUI()));

		ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> PonderTooltipHandler.addToTooltip(lines, stack));
		PonderKeybinds.register(KeyBindingHelper::registerKeyBinding);

		ClientLifecycleEvents.CLIENT_STARTED.register($ -> PonderClient.modLoadCompleted());

		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(FABRIC$RESOURCE_RELOAD_LISTENER);

		prepareConfigUI();
	}

	private void prepareConfigUI() {
		BaseConfigScreen.setDefaultActionFor(Ponder.MOD_ID, base -> base
				.withButtonLabels("Client Settings", null, null)
				.withSpecs(PonderConfig.client().specification, null, null)
		);
	}
}
