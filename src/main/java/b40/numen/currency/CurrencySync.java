package b40.numen.currency;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class CurrencySync {
	private CurrencySync() {}

	public static void register() {
		PayloadTypeRegistry.playS2C().register(CurrencySyncPayload.TYPE, CurrencySyncPayload.CODEC);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> send(handler.player));
	}

	public static void send(ServerPlayer player) {
		PlayerCurrencyState state = PlayerCurrencyState.get(player.server);
		ServerPlayNetworking.send(player, new CurrencySyncPayload(state.get(player, Currency.COIN), state.get(player, Currency.NEXUS)));
	}
}
