package net.createmod.ponder.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import net.createmod.catnip.data.Couple;
import net.createmod.catnip.theme.Color;
import net.createmod.ponder.FabricPonderClient;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;

@Mixin(TooltipRenderUtil.class)
public class TooltipRenderUtilMixin {
	@ModifyArgs(
		method = "renderTooltipBackground",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/TooltipRenderUtil;renderFrameGradient(Lnet/minecraft/client/gui/GuiGraphics;IIIIIII)V"
		)
	)
	private static void overrideBorderColor(Args args) {
		Couple<Color> override = FabricPonderClient.tooltipBorderColorOverride;
		if (override == null)
			return;

		args.set(6, override.getFirst().getRGB());
		args.set(7, override.getSecond().getRGB());
	}
}
