package b40.numen;

import b40.numen.currency.CurrencyCommands;
import b40.numen.currency.CurrencyComponents;
import b40.numen.currency.PlayerCurrencyComponent;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Numen implements ModInitializer, EntityComponentInitializer {
	public static final String MOD_ID = "numen";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CurrencyCommands.register();
		LOGGER.info("Numen currency systems loaded.");
	}

	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.registerForPlayers(CurrencyComponents.WALLET, PlayerCurrencyComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
