package com.earth2me.essentials.chat.processing;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.chat.EssentialsChat;
import net.ess3.provider.AbstractChatEvent;
import net.ess3.provider.providers.PaperChatListenerProvider;
import org.bukkit.plugin.PluginManager;

public class PaperChatHandler extends AbstractChatHandler {
    public PaperChatHandler(Essentials ess, EssentialsChat essChat) {
        super(ess, essChat);
    }

    @Override
    protected Object cacheKey(final AbstractChatEvent event) {
        // PaperChatListenerProvider hands back the very same wrapper for every priority of a given
        // AsyncChatEvent, so the wrapper identifies the chat event and keeps concurrent chats from the
        // same player in separate cache entries.
        return event;
    }

    public void registerListeners() {
        final PluginManager pm = essChat.getServer().getPluginManager();
        pm.registerEvents(new ChatListener(), essChat);
    }

    public final class ChatListener extends PaperChatListenerProvider {
        @Override
        public void onChatLowest(AbstractChatEvent event) {
            handleChatFormat(event);
        }

        @Override
        public void onChatNormal(AbstractChatEvent event) {
            handleChatRecipients(event);
        }

        @Override
        public void onChatHighest(AbstractChatEvent event) {
            handleChatPostFormat(event);
            handleChatSubmit(event);
        }
    }
}
