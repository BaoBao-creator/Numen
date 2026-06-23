package b40.numen.client;

import b40.numen.currency.CurrencySyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class NumenClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(CurrencySyncPayload.TYPE, (payload, context) -> context.client().execute(() -> CurrencyClientState.set(payload.coin(), payload.nexus())));
	}
}
