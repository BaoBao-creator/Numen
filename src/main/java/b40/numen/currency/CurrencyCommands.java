package b40.numen.currency;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class CurrencyCommands {
	private static final SimpleCommandExceptionType NOT_ENOUGH = new SimpleCommandExceptionType(Component.literal("You do not have enough money.").withStyle(ChatFormatting.RED));
	private static final SimpleCommandExceptionType SELF_PAYMENT = new SimpleCommandExceptionType(Component.literal("You cannot pay yourself.").withStyle(ChatFormatting.RED));

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
			.then(literal("add").then(argument("player", EntityArgument.player()).then(argument("amount", IntegerArgumentType.integer(1)).executes(ctx -> add(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), currency, IntegerArgumentType.getInteger(ctx, "amount")))))
			.then(literal("set").then(argument("player", EntityArgument.player()).then(argument("amount", IntegerArgumentType.integer(1)).executes(ctx -> set(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), currency, IntegerArgumentType.getInteger(ctx, "amount")))))
			.then(literal("take").then(argument("player", EntityArgument.player()).then(argument("amount", IntegerArgumentType.integer(1)).executes(ctx -> take(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), currency, IntegerArgumentType.getInteger(ctx, "amount"))))));
	}

	private static void registerPay(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(literal("pay")
			.then(argument("player", EntityArgument.player())
				.then(argument("amount", IntegerArgumentType.integer(1))
					.then(literal("coin").executes(ctx -> pay(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player"), Currency.COIN, IntegerArgumentType.getInteger(ctx, "amount"))))
					.then(literal("c").executes(ctx -> pay(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player"), Currency.COIN, IntegerArgumentType.getInteger(ctx, "amount"))))
					.then(literal("nexus").executes(ctx -> pay(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player"), Currency.NEXUS, IntegerArgumentType.getInteger(ctx, "amount"))))
					.then(literal("n").executes(ctx -> pay(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player"), Currency.NEXUS, IntegerArgumentType.getInteger(ctx, "amount")))))));
	}

	private static int add(CommandSourceStack source, ServerPlayer player, Currency currency, int amount) {
		int balance = CurrencyComponents.get(player).add(currency, amount);
		source.sendSuccess(() -> Component.literal("Added ").append(format(amount, currency)).append(" to " + player.getName().getString() + ". New balance: " + balance + "."), true);
		return 1;
	}

	private static int set(CommandSourceStack source, ServerPlayer player, Currency currency, int amount) {
		int balance = CurrencyComponents.get(player).set(currency, amount);
		source.sendSuccess(() -> Component.literal("Set " + player.getName().getString() + "'s " + currency.displayName() + " balance to " + balance + ".").withStyle(currency.formatting()), true);
		return 1;
	}

	private static int take(CommandSourceStack source, ServerPlayer player, Currency currency, int amount) {
		int balance = CurrencyComponents.get(player).take(currency, amount);
		source.sendSuccess(() -> Component.literal("Took ").append(format(amount, currency)).append(" from " + player.getName().getString() + ". New balance: " + balance + "."), true);
		return 1;
	}

	private static int pay(ServerPlayer sender, ServerPlayer receiver, Currency currency, int amount) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		if (sender.getUUID().equals(receiver.getUUID())) throw SELF_PAYMENT.create();
		if (!CurrencyComponents.get(sender).transferTo(receiver, currency, amount)) throw NOT_ENOUGH.create();
		sender.sendSystemMessage(Component.literal("You sent ").append(format(amount, currency)).append(" to " + receiver.getName().getString() + "."));
		receiver.sendSystemMessage(Component.literal("You received ").append(format(amount, currency)).append(" from " + sender.getName().getString() + "."));
		return 1;
	}

	private static Component format(int amount, Currency currency) {
		return Component.literal(amount + " " + currency.displayName()).withStyle(currency.formatting());
	}
}
