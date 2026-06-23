package b40.numen.client.mixin;

import b40.numen.client.CurrencyClientState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
	@Inject(method = "render", at = @At("TAIL"))
	private void numen$renderCurrency(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		InventoryScreen screen = (InventoryScreen) (Object) this;
		int x = screen.getGuiLeft() + 97;
		int y = screen.getGuiTop() + 61;
		graphics.drawString(screen.getFont(), Component.literal("Coin: " + CurrencyClientState.coin()), x, y, 0xFFD700, false);
		graphics.drawString(screen.getFont(), Component.literal("Nexus: " + CurrencyClientState.nexus()), x, y + 10, 0x7D5CFF, false);
	}
}
