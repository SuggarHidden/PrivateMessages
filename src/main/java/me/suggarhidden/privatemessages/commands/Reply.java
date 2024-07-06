package me.suggarhidden.privatemessages.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import me.suggarhidden.privatemessages.PrivateMessages;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.Optional;
import java.util.UUID;

public class Reply implements SimpleCommand {
    @Override
    public void execute(SimpleCommand.Invocation invocation) {
        CommandSource source = invocation.source();
        if (!(source instanceof Player)) {
            source.sendMessage(MiniMessage.miniMessage().deserialize("Only players can use this command."));
            return;
        }
        System.out.println(invocation.arguments());
        Player sender = (Player) source;
        String[] args = invocation.arguments();

        UUID targetID = PrivateMessages.getRecentMessages().get(sender.getUniqueId());
        if (targetID == null) {
            source.sendMessage(MiniMessage.miniMessage()
                    .deserialize(PrivateMessages.getConfig().getString("messages.message_first")));
            return;
        }

        if (args.length < 1) {
            source.sendMessage(MiniMessage.miniMessage()
                    .deserialize(PrivateMessages.getConfig().getString("messages.reply_usage")));
            return;
        }

        Optional<Player> optionalTarget = PrivateMessages.getProxy().getPlayer(targetID);
        if (optionalTarget.isEmpty()) {
            source.sendMessage(MiniMessage.miniMessage()
                    .deserialize(PrivateMessages.getConfig().getString("messages.player_disconnect")));
            PrivateMessages.getRecentMessages().remove(sender.getUniqueId());
            return;
        }

        Player target = optionalTarget.get();
        String msg = String.join(" ", args);

        sender.sendMessage(MiniMessage.miniMessage().deserialize(PrivateMessages.getConfig().getString("format.sender"),
                Placeholder.parsed("sender", sender.getUsername()),
                Placeholder.parsed("receiver", target.getUsername()), Placeholder.unparsed("message", msg)));

        target.sendMessage(MiniMessage.miniMessage()
                .deserialize(PrivateMessages.getConfig().getString("format.receiver"),
                        Placeholder.parsed("sender", sender.getUsername()),
                        Placeholder.parsed("receiver", target.getUsername()), Placeholder.unparsed("message", msg)));
    }

    @Override
    public boolean hasPermission(SimpleCommand.Invocation invocation) {
        return invocation.source().hasPermission(PrivateMessages.getConfig().getString("permissions.reply"));
    }
}
