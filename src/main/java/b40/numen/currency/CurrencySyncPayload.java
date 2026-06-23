package b40.numen.currency;

import b40.numen.Numen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CurrencySyncPayload(long coin, long nexus) implements CustomPacketPayload {
	public static final Type<CurrencySyncPayload> TYPE = new Type<>(Numen.id("currency_sync"));
	public static final StreamCodec<FriendlyByteBuf, CurrencySyncPayload> CODEC = CustomPacketPayload.codec(CurrencySyncPayload::write, CurrencySyncPayload::new);

	private CurrencySyncPayload(FriendlyByteBuf buf) {
		this(buf.readVarLong(), buf.readVarLong());
	}

	private void write(FriendlyByteBuf buf) {
		buf.writeVarLong(coin);
		buf.writeVarLong(nexus);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
