package net.createmod.ponder.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.createmod.ponder.FabricPonderClient;
import net.createmod.ponder.foundation.PonderTooltipHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {
	@WrapMethod(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V")
	private void setBorderColorOverride(Font font, ItemStack itemStack, int i, int j, Operation<Void> original) {
		FabricPonderClient.tooltipBorderColorOverride = PonderTooltipHandler.handleTooltipColor(itemStack).orElse(null);
		original.call(font, itemStack, i, j);
		FabricPonderClient.tooltipBorderColorOverride = null;
	}
}
