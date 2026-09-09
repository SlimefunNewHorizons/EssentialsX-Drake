package net.ess3.provider.providers;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.ess3.provider.AbstractChatEvent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

public abstract class PaperChatListenerProvider implements Listener {
    private final boolean formatParsing;
    private final LegacyComponentSerializer serializer;
    // AsyncChatEvent is fired off the main thread, and several chat events can be in flight at the
    // same time (e.g. when another plugin re-dispatches chat from its own async pool), so this map
    // is mutated concurrently and must be synchronised.
    private final Map<AsyncChatEvent, PaperChatEvent> eventMap = Collections.synchronizedMap(new IdentityHashMap<>());

    public PaperChatListenerProvider() {
        this(true);
    }

    public PaperChatListenerProvider(final boolean formatParsing) {
        this.formatParsing = formatParsing;
        this.serializer = LegacyComponentSerializer.builder()
                .flattener(ComponentFlattener.basic())
                .extractUrls(AbstractChatEvent.URL_PATTERN)
                .useUnusualXRepeatedCharacterHexFormat()
                .hexColors()
                .build();
    }

    public void onChatLowest(final AbstractChatEvent event) {

    }

    public void onChatNormal(final AbstractChatEvent event) {

    }

    public void onChatHighest(final AbstractChatEvent event) {

    }

    public void onChatMonitor(final AbstractChatEvent event) {

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public final void onLowest(final AsyncChatEvent event) {
        onChatLowest(wrap(event));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public final void onNormal(final AsyncChatEvent event) {
        onChatNormal(wrap(event));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onHighest(final AsyncChatEvent event) {
        final PaperChatEvent paperChatEvent = wrap(event);
        onChatHighest(paperChatEvent);

        if (event.isCancelled()) {
            return;
        }

        if (!formatParsing) {
            return;
        }

        final TextComponent format = serializer.deserialize(paperChatEvent.getFormat());
        final TextComponent eventMessage = serializer.deserialize(paperChatEvent.getMessage());

        event.renderer(ChatRenderer.viewerUnaware((player, displayName, message) ->
                format.replaceText(builder -> builder
                        .match("%(\\d)\\$s").replacement((index, match) -> {
                            if (index.group(1).equals("1")) {
                                return displayName;
                            }
                            return eventMessage;
                        })
                )));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public final void onMonitor(final AsyncChatEvent event) {
        onChatMonitor(wrap(event));

        eventMap.remove(event);
    }

    private PaperChatEvent wrap(final AsyncChatEvent event) {
        // computeIfAbsent so that two threads wrapping distinct events cannot interleave a resize,
        // and so a single event always yields the very same wrapper instance.
        return eventMap.computeIfAbsent(event, e -> new PaperChatEvent(e, serializer));
    }
}
