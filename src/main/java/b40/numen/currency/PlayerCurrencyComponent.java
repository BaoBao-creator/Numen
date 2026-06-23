package b40.numen.currency;

import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Server-authoritative wallet attached directly to each PlayerEntity by CCA.
 * CCA persists this component with the player, copies it on respawn, and sends
 * updates to the tracking client when {@link CurrencyComponents#sync(Player)} is called.
 */
public class PlayerCurrencyComponent implements AutoSyncedComponent {
	private final Player player;
	private int coin;
	private int nexus;

	public PlayerCurrencyComponent(Player player) {
		this.player = player;
	}

	public int get(Currency currency) {
		return currency == Currency.COIN ? coin : nexus;
	}

	public int set(Currency currency, int amount) {
		int sanitized = Math.max(0, amount);
		setRaw(currency, sanitized);
		sync();
		return sanitized;
	}

	public int add(Currency currency, int amount) {
		if (amount <= 0) return get(currency);
		int updated = saturatedAdd(get(currency), amount);
		setRaw(currency, updated);
		sync();
		return updated;
	}

	public int take(Currency currency, int amount) {
		if (amount <= 0) return get(currency);
		int updated = Math.max(0, get(currency) - amount);
		setRaw(currency, updated);
		sync();
		return updated;
	}

	public boolean transferTo(ServerPlayer target, Currency currency, int amount) {
		if (amount <= 0 || get(currency) < amount) return false;
		setRaw(currency, get(currency) - amount);
		CurrencyComponents.get(target).add(currency, amount);
		sync();
		return true;
	}

	private void setRaw(Currency currency, int amount) {
		if (currency == Currency.COIN) coin = amount;
		else nexus = amount;
	}

	private void sync() {
		CurrencyComponents.sync(player);
	}

	private static int saturatedAdd(int current, int amount) {
		if (Integer.MAX_VALUE - current < amount) return Integer.MAX_VALUE;
		return current + amount;
	}

	@Override
	public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
		coin = Math.max(0, tag.getInt("coin"));
		nexus = Math.max(0, tag.getInt("nexus"));
	}

	@Override
	public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
		tag.putInt("coin", coin);
		tag.putInt("nexus", nexus);
	}
}
