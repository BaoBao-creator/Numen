package b40.numen.currency;

import java.util.Locale;

public enum Currency {
	COIN("coin", "c", "Coin"),
	NEXUS("nexus", "n", "Nexus");

	private final String id;
	private final String shortId;
	private final String displayName;

	Currency(String id, String shortId, String displayName) {
		this.id = id;
		this.shortId = shortId;
		this.displayName = displayName;
	}

	public String id() { return id; }
	public String shortId() { return shortId; }
	public String displayName() { return displayName; }

	public static Currency parse(String value) {
		String normalized = value.toLowerCase(Locale.ROOT);
		for (Currency currency : values()) {
			if (currency.id.equals(normalized) || currency.shortId.equals(normalized)) return currency;
		}
		throw new IllegalArgumentException("Unknown currency: " + value);
	}
}
