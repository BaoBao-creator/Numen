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
	private static final SimpleCommandExceptionType NOT_ENOUGH = new SimpleCommandExceptionType(Component.literal("You do not have enough currency."));
	private static final SimpleCommandExceptionType SELF_PAYMENT = new SimpleCommandExceptionType(Component.literal("You cannot pay yourself."));

	private CurrencyCommands() {}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerAdmin(dispatcher, Currency.COIN, "coin");
			registerAdmin(dispatcher, Currency.COIN, "c");
			registerAdmin(dispatcher, Currency.NEXUS, "nexus");
			registerAdmin(dispatcher, Currency.NEXUS, "n");
			registerBalance(dispatcher);
			registerPay(dispatcher);
		});
	}

	private static void registerAdmin(CommandDispatcher<CommandSourceStack> dispatcher, Currency currency, String name) {
		dispatcher.register(literal(name).requires(source -> source.hasPermission(2))
			.then(literal("add").then(argument("player", EntityArgument.player()).then(argument("amount", LongArgumentType.longArg(1)).executes(ctx -> add(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), currency, LongArgumentType.getLong(ctx, "amount")))))
			.then(literal("set").then(argument("player", EntityArgument.player()).then(argument("amount", LongArgumentType.longArg(0)).executes(ctx -> set(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), currency, LongArgumentType.getLong(ctx, "amount")))))
			.then(literal("remove").then(argument("player", EntityArgument.player()).then(argument("amount", LongArgumentType.longArg(1)).executes(ctx -> remove(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), currency, LongArgumentType.getLong(ctx, "amount")))))
			.then(literal("subtract").then(argument("player", EntityArgument.player()).then(argument("amount", LongArgumentType.longArg(1)).executes(ctx -> remove(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), currency, LongArgumentType.getLong(ctx, "amount")))))
			.then(literal("take").then(argument("player", EntityArgument.player()).then(argument("amount", LongArgumentType.longArg(1)).executes(ctx -> remove(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), currency, LongArgumentType.getLong(ctx, "amount"))))));
	}

	private static void registerBalance(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(literal("balance")
			.executes(ctx -> balance(ctx.getSource(), ctx.getSource().getPlayerOrException()))
			.then(argument("player", EntityArgument.player()).requires(source -> source.hasPermission(2))
				.executes(ctx -> balance(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))));
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

	private static int add(CommandSourceStack source, ServerPlayer player, Currency currency, long amount) {
		long balance = PlayerCurrencyState.get(source.getServer()).add(player, currency, amount);
		source.sendSuccess(() -> Component.literal("Added " + format(amount, currency) + " to " + player.getName().getString() + ". New balance: " + balance + "."), true);
		return 1;
	}

	private static int set(CommandSourceStack source, ServerPlayer player, Currency currency, long amount) {
		long balance = PlayerCurrencyState.get(source.getServer()).set(player, currency, amount);
		source.sendSuccess(() -> Component.literal("Set " + player.getName().getString() + "'s " + currency.displayName() + " balance to " + balance + "."), true);
		return 1;
	}

	private static int remove(CommandSourceStack source, ServerPlayer player, Currency currency, long amount) {
		long balance = PlayerCurrencyState.get(source.getServer()).remove(player, currency, amount);
		source.sendSuccess(() -> Component.literal("Removed " + format(amount, currency) + " from " + player.getName().getString() + ". New balance: " + balance + "."), true);
		return 1;
	}

	private static int balance(CommandSourceStack source, ServerPlayer player) {
		PlayerCurrencyState state = PlayerCurrencyState.get(source.getServer());
		source.sendSuccess(() -> Component.literal(player.getName().getString() + " has " + state.get(player, Currency.COIN) + " Coin and " + state.get(player, Currency.NEXUS) + " Nexus."), false);
		return 1;
	}

	private static int pay(ServerPlayer sender, ServerPlayer receiver, Currency currency, long amount) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		if (sender.getUUID().equals(receiver.getUUID())) throw SELF_PAYMENT.create();
		PlayerCurrencyState state = PlayerCurrencyState.get(sender.server);
		if (!state.take(sender, currency, amount)) throw NOT_ENOUGH.create();
		state.add(receiver, currency, amount);
		sender.sendSystemMessage(Component.literal("Paid " + format(amount, currency) + " to " + receiver.getName().getString() + "."));
		receiver.sendSystemMessage(Component.literal(sender.getName().getString() + " paid you " + format(amount, currency) + "."));
		return 1;
	}

	private static String format(long amount, Currency currency) {
		return amount + " " + currency.displayName();
	}
}
