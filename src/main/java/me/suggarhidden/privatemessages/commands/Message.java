package me.suggarhidden.privatemessages.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import me.suggarhidden.privatemessages.PrivateMessages;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Message implements SimpleCommand {
    @Override
    public void execute(SimpleCommand.Invocation invocation) {
        CommandSource source = invocation.source();

        if (!(source instanceof Player)) {
            source.sendMessage(MiniMessage.miniMessage().deserialize("Only players can use this command."));
            return;
        }

        Player sender = (Player) source;
        String[] args = invocation.arguments();

        if (args.length < 2) {
            source.sendMessage(MiniMessage.miniMessage().deserialize(
                    PrivateMessages.getConfig().getString("messages.message_usage")));
            return;
        }

        if (args[0].equalsIgnoreCase(sender.getUsername())) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                    PrivateMessages.getConfig().getString("messages.message_self")));
            return;
        }

        Optional<Player> targetOpt = PrivateMessages.getProxy().getPlayer(args[0]);
        if (targetOpt.isEmpty()) {
            source.sendMessage(MiniMessage.miniMessage().deserialize(
                    PrivateMessages.getConfig().getString("messages.player_not_found")));
            return;
        }

        Player target = targetOpt.get();
        String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        sender.sendMessage(MiniMessage.miniMessage().deserialize(
                PrivateMessages.getConfig().getString("format.sender"),
                Placeholder.parsed("sender", sender.getUsername()),
                Placeholder.parsed("receiver", target.getUsername()),
                Placeholder.unparsed("message", msg)));

        target.sendMessage(MiniMessage.miniMessage().deserialize(
                PrivateMessages.getConfig().getString("format.receiver"),
                Placeholder.parsed("sender", sender.getUsername()),
                Placeholder.parsed("receiver", target.getUsername()),
                Placeholder.unparsed("message", msg)));

        PrivateMessages.getRecentMessages().put(sender.getUniqueId(), target.getUniqueId());
    }

    @Override
    public boolean hasPermission(SimpleCommand.Invocation invocation) {
        return invocation.source().hasPermission(PrivateMessages.getConfig().getString("permissions.message"));
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(SimpleCommand.Invocation invocation) {
        List<String> players = new ArrayList<>();
        String[] args = invocation.arguments();

        if (args.length < 2) {
            PrivateMessages.getProxy().getAllPlayers().forEach(p -> players.add(p.getUsername()));
        }

        return CompletableFuture.completedFuture(players);
    }
}
