package b40.numen.currency;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class CurrencyCommands {
	private static final SimpleCommandExceptionType NOT_ENOUGH = new SimpleCommandExceptionType(Component.literal("Bạn không có đủ tiền."));
	private CurrencyCommands() {}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerAdmin(dispatcher, Currency.COIN, "coin");
			registerAdmin(dispatcher, Currency.COIN, "c");
			registerAdmin(dispatcher, Currency.NEXUS, "nexus");
			registerAdmin(dispatcher, Currency.NEXUS, "n");
			registerPay(dispatcher);
		});
	}

	private static void registerAdmin(CommandDispatcher<CommandSourceStack> dispatcher, Currency currency, String name) {
		dispatcher.register(literal(name).requires(source -> source.hasPermission(2))
			.then(literal("add").then(argument("player", EntityArgument.player()).then(argument("amount", LongArgumentType.longArg(0)).executes(ctx -> add(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), currency, LongArgumentType.getLong(ctx, "amount")))))
			.then(literal("set").then(argument("player", EntityArgument.player()).then(argument("amount", LongArgumentType.longArg(0)).executes(ctx -> set(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), currency, LongArgumentType.getLong(ctx, "amount")))))
			.then(literal("giam").then(argument("player", EntityArgument.player()).then(argument("amount", LongArgumentType.longArg(0)).executes(ctx -> reduce(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), currency, LongArgumentType.getLong(ctx, "amount")))))
			.then(literal("giảm").then(argument("player", EntityArgument.player()).then(argument("amount", LongArgumentType.longArg(0)).executes(ctx -> reduce(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), currency, LongArgumentType.getLong(ctx, "amount"))))));
	}

	private static void registerPay(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(literal("pay")
			.then(argument("player", EntityArgument.player())
				.then(argument("amount", LongArgumentType.longArg(1))
					.then(literal("coin").executes(ctx -> pay(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player"), Currency.COIN, LongArgumentType.getLong(ctx, "amount"))))
					.then(literal("c").executes(ctx -> pay(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player"), Currency.COIN, LongArgumentType.getLong(ctx, "amount"))))
					.then(literal("nexus").executes(ctx -> pay(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player"), Currency.NEXUS, LongArgumentType.getLong(ctx, "amount"))))
					.then(literal("n").executes(ctx -> pay(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player"), Currency.NEXUS, LongArgumentType.getLong(ctx, "amount")))))));
	}

	private static int add(CommandSourceStack source, ServerPlayer player, Currency currency, long amount) { PlayerCurrencyState.get(source.getServer()).add(player, currency, amount); source.sendSuccess(() -> Component.literal("Đã thêm " + amount + " " + currency.id() + " cho " + player.getName().getString() + "."), true); return 1; }
	private static int set(CommandSourceStack source, ServerPlayer player, Currency currency, long amount) { PlayerCurrencyState.get(source.getServer()).set(player, currency, amount); source.sendSuccess(() -> Component.literal("Đã đặt " + currency.id() + " của " + player.getName().getString() + " thành " + amount + "."), true); return 1; }
	private static int reduce(CommandSourceStack source, ServerPlayer player, Currency currency, long amount) { PlayerCurrencyState state = PlayerCurrencyState.get(source.getServer()); state.set(player, currency, Math.max(0, state.get(player, currency) - amount)); source.sendSuccess(() -> Component.literal("Đã giảm " + amount + " " + currency.id() + " của " + player.getName().getString() + "."), true); return 1; }
	private static int pay(ServerPlayer sender, ServerPlayer receiver, Currency currency, long amount) throws com.mojang.brigadier.exceptions.CommandSyntaxException { if (sender.getUUID().equals(receiver.getUUID())) { sender.sendSystemMessage(Component.literal("Bạn không thể tự chuyển tiền cho chính mình.")); return 0; } PlayerCurrencyState state = PlayerCurrencyState.get(sender.server); if (!state.take(sender, currency, amount)) throw NOT_ENOUGH.create(); state.add(receiver, currency, amount); sender.sendSystemMessage(Component.literal("Đã chuyển " + amount + " " + currency.id() + " cho " + receiver.getName().getString() + ".")); receiver.sendSystemMessage(Component.literal(sender.getName().getString() + " đã chuyển cho bạn " + amount + " " + currency.id() + ".")); return 1; }
}
