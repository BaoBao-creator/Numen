package b40.numen.client;

public final class CurrencyClientState {
	private static long coin;
	private static long nexus;

	private CurrencyClientState() {}
	public static long coin() { return coin; }
	public static long nexus() { return nexus; }
	public static void set(long coin, long nexus) { CurrencyClientState.coin = coin; CurrencyClientState.nexus = nexus; }
}
