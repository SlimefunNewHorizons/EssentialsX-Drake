package com.earth2me.essentials.chat.processing;

import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import net.essentialsx.api.v2.ChatType;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

public class ChatProcessingCache {

    // Keyed by identity on a per-chat token supplied by the handler (the chat event itself where the
    // platform gives us a stable wrapper, the player otherwise). Keying by player alone breaks when a
    // player has two chat events in flight at once: the first one to reach HIGHEST clears the entry
    // and the second then reads null at NORMAL/HIGHEST.
    private final Map<Object, ProcessedChat> chats = Collections.synchronizedMap(new IdentityHashMap<>());

    public ProcessedChat getProcessedChat(final Object key) {
        return chats.get(key);
    }

    public void setProcessedChat(final Object key, final ProcessedChat chat) {
        chats.put(key, chat);
    }

    public void clearProcessedChat(final Object key) {
        chats.remove(key);
    }

    public abstract static class Chat {
        private final User user;
        private final ChatType type;
        private final String originalMessage;
        protected long radius;

        protected Chat(User user, ChatType type, String originalMessage) {
            this.user = user;
            this.type = type;
            this.originalMessage = originalMessage;
        }

        public User getUser() {
            return user;
        }

        public ChatType getType() {
            return type;
        }

        public String getOriginalMessage() {
            return originalMessage;
        }

        public long getRadius() {
            return radius;
        }

        public final String getLongType() {
            return type == ChatType.UNKNOWN ? "chat" : "chat-" + type.key();
        }
    }

    public static class ProcessedChat extends Chat {
        private final Trade charge;

        public ProcessedChat(final User user, final ChatType type, final String originalMessage) {
            super(user, type, originalMessage);
            this.charge = new Trade(getLongType(), user.getEssentials());
        }

        public void setRadius(final long radius) {
            this.radius = radius;
        }

        public Trade getCharge() {
            return charge;
        }
    }
}
