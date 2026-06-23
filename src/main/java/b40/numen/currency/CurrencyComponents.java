package b40.numen.currency;

import b40.numen.Numen;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;

import net.minecraft.world.entity.player.Player;

public final class CurrencyComponents {
	public static final ComponentKey<PlayerCurrencyComponent> WALLET = ComponentRegistry.getOrCreate(Numen.id("wallet"), PlayerCurrencyComponent.class);

	private CurrencyComponents() {}

	public static PlayerCurrencyComponent get(Player player) {
		return WALLET.get(player);
	}

	public static void sync(Player player) {
		if (!player.level().isClientSide) {
			WALLET.sync(player);
		}
	}
}
