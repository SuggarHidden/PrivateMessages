package me.suggarhidden.privatemessages;

import com.google.inject.Inject;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import me.suggarhidden.privatemessages.commands.Message;
import me.suggarhidden.privatemessages.commands.Reply;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@Plugin(
        id = "privatemessages",
        name = "PrivateMessages",
        version = "0.0.1"
)
public class PrivateMessages {
    private static ProxyServer proxy;
    public static YamlDocument config;
    private static HashMap<UUID, UUID> recentMessages = new HashMap<>();

    @Inject
    public PrivateMessages(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        PrivateMessages.proxy = proxy;
        try {
            config = YamlDocument.create(new File(dataDirectory.toFile(), "config.yml"),
                    getClass().getResourceAsStream("/config.yml"),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version")).setOptionSorting(
                            UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build()
            );
            config.update();
            config.save();
        } catch (IOException e) {
            logger.error("Could not crea/load plugin config!");
            logger.error(e.toString());
            Optional<PluginContainer> container = proxy.getPluginManager().getPlugin("privatemessages");
            container.ifPresent(pluginContainer -> pluginContainer.getExecutorService().shutdown());
        }

        logger.info("PrivateMessages enabled successfully.");
    }

    public static HashMap<UUID, UUID> getRecentMessages() {
        return recentMessages;
    }

    public static YamlDocument getConfig() {
        return config;
    }

    public static ProxyServer getProxy() {
        return proxy;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        CommandManager commandManager = proxy.getCommandManager();
        CommandMeta messageMeta = commandManager.metaBuilder("message").aliases("msg").plugin(this).build();
        CommandMeta replyMeta = commandManager.metaBuilder("reply").aliases("r").plugin(this).build();
        commandManager.register(messageMeta, new Message());
        commandManager.register(replyMeta, new Reply());
    }
}
