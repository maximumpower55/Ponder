package net.createmod.ponder.mixin.client;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.createmod.ponder.FabricPonderClient;
import net.createmod.ponder.foundation.PonderTooltipHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
	@WrapOperation(
		method = "renderTooltip",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V"
		)
	)
	private void setBorderColorOverride(
		GuiGraphics graphics, Font font, List<Component> lines, Optional<TooltipComponent> component, int x, int y,
		Operation<Void> original, @Local ItemStack stack
	) {
		FabricPonderClient.tooltipBorderColorOverride = PonderTooltipHandler.handleTooltipColor(stack).orElse(null);
		original.call(graphics, font, lines, component, x, y);
		FabricPonderClient.tooltipBorderColorOverride = null;
	}
}
