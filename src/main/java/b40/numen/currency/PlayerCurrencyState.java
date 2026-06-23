package b40.numen.currency;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class PlayerCurrencyState extends SavedData {
	private static final String DATA_NAME = "numen_currencies";
	private static final SavedDataType<PlayerCurrencyState> TYPE = new SavedDataType<>(DATA_NAME, PlayerCurrencyState::new, PlayerCurrencyState::load, null);
	private final Map<UUID, Account> accounts = new HashMap<>();

	public static PlayerCurrencyState get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	public long get(ServerPlayer player, Currency currency) {
		return account(player.getUUID()).get(currency);
	}

	public void set(ServerPlayer player, Currency currency, long amount) {
		account(player.getUUID()).set(currency, Math.max(0, amount));
		setDirty();
		CurrencySync.send(player);
	}

	public boolean take(ServerPlayer player, Currency currency, long amount) {
		Account account = account(player.getUUID());
		if (account.get(currency) < amount) return false;
		account.set(currency, account.get(currency) - amount);
		setDirty();
		CurrencySync.send(player);
		return true;
	}

	public void add(ServerPlayer player, Currency currency, long amount) {
		set(player, currency, get(player, currency) + amount);
	}

	private Account account(UUID uuid) {
		return accounts.computeIfAbsent(uuid, ignored -> new Account());
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		CompoundTag players = new CompoundTag();
		accounts.forEach((uuid, account) -> {
			CompoundTag playerTag = new CompoundTag();
			playerTag.putLong("coin", account.coin);
			playerTag.putLong("nexus", account.nexus);
			players.put(uuid.toString(), playerTag);
		});
		tag.put("players", players);
		return tag;
	}

	private static PlayerCurrencyState load(CompoundTag tag, HolderLookup.Provider registries) {
		PlayerCurrencyState state = new PlayerCurrencyState();
		CompoundTag players = tag.getCompound("players").orElse(new CompoundTag());
		for (String key : players.keySet()) {
			try {
				CompoundTag playerTag = players.getCompound(key).orElse(new CompoundTag());
				Account account = new Account();
				account.coin = Math.max(0, playerTag.getLong("coin").orElse(0L));
				account.nexus = Math.max(0, playerTag.getLong("nexus").orElse(0L));
				state.accounts.put(UUID.fromString(key), account);
			} catch (IllegalArgumentException ignored) {
			}
		}
		return state;
	}

	private static class Account {
		long coin;
		long nexus;
		long get(Currency currency) { return currency == Currency.COIN ? coin : nexus; }
		void set(Currency currency, long amount) { if (currency == Currency.COIN) coin = amount; else nexus = amount; }
	}
}
