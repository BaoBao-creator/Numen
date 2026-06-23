package b40.numen.currency;

import java.util.Locale;

import net.minecraft.ChatFormatting;

public enum Currency {
	COIN("coin", "c", "coins", ChatFormatting.GOLD),
	NEXUS("nexus", "n", "nexus", ChatFormatting.AQUA);

	private final String id;
	private final String shortId;
	private final String displayName;
	private final ChatFormatting formatting;

	Currency(String id, String shortId, String displayName, ChatFormatting formatting) {
		this.id = id;
		this.shortId = shortId;
		this.displayName = displayName;
		this.formatting = formatting;
	}

	public String id() { return id; }
	public String shortId() { return shortId; }
	public String displayName() { return displayName; }
	public ChatFormatting formatting() { return formatting; }

	public static Currency parse(String value) {
		String normalized = value.toLowerCase(Locale.ROOT);
		for (Currency currency : values()) {
			if (currency.id.equals(normalized) || currency.shortId.equals(normalized)) return currency;
		}
		throw new IllegalArgumentException("Unknown currency: " + value);
	}
}
