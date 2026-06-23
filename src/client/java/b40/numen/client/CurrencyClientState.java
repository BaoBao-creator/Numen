package b40.numen.client;

import b40.numen.currency.Currency;
import b40.numen.currency.CurrencyComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class CurrencyClientState {
	private CurrencyClientState() {}

	public static int coin() {
		return get(Currency.COIN);
	}

	public static int nexus() {
		return get(Currency.NEXUS);
	}

	private static int get(Currency currency) {
		Player player = Minecraft.getInstance().player;
		return player == null ? 0 : CurrencyComponents.get(player).get(currency);
	}
}
