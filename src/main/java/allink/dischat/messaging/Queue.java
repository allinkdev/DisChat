package allink.dischat.messaging;

import allink.dischat.Main;
import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import net.jodah.expiringmap.ExpiringMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Queue {
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final List<QueuedMessage> toBroadcast = new ArrayList<>();
    private final Map<User, Long> lastMessage = ExpiringMap.builder()
            .expiration(5, TimeUnit.SECONDS)
            .build();

    private final Courier courier;
    private final Enveloper enveloper;

    public Queue(Courier courier) {
        executorService.scheduleAtFixedRate(this::sendQueuedMessages, 1, 1, TimeUnit.SECONDS);
        this.courier = courier;
        this.enveloper = Main.getEnveloper();
    }

    private void sendQueuedMessages() {
        final Set<TextChannel> channels = enveloper.getCachedTextChannels();
        final Map<TextChannel, User> map = enveloper.getChannelToUser();
        final List<QueuedMessage> sent = new ArrayList<>();

        for (QueuedMessage queuedMessage : toBroadcast) {
            final String username = queuedMessage.getUsername();
            final WebhookMessage message = new WebhookMessageBuilder()
                    .setContent(queuedMessage.getContent())
                    .setUsername(queuedMessage.getUsername())
                    .setAvatarUrl(queuedMessage.isSystem() ? Main.getSystemMessageAvatarUrl() : "https://singlecolorimage.com/get/" + username.substring(0, 6) + "/128x128")
                    .setAllowedMentions(AllowedMentions.none())
                    .build();

            for (TextChannel cachedTextChannel : channels) {
                final User author = queuedMessage.getAuthor();

                if (author != null) {
                    if (map.get(cachedTextChannel).equals(author)) {
                        continue;
                    }
                }

                final Webhook webhook = courier.getOrCreateWebhook(cachedTextChannel);
                final WebhookClient webhookClient = JDAWebhookClient.from(webhook);

                webhookClient.send(message);
                webhookClient.close();
            }

            sent.add(queuedMessage);
        }

        toBroadcast.removeAll(sent);
    }

    public void queueSystemMessage(String message) {
        toBroadcast.add(QueuedMessage.builder()
                .username("System")
                .content(message)
                .system(true)
                .build());
    }

    public void queueMessage(Message message, User author) {
        final long now = System.currentTimeMillis();

        if (lastMessage.containsKey(author)) {
            final long lastMessageAt = lastMessage.get(author);
            final float diff = (now - lastMessageAt);

            if (diff < 1000) {
                message.reply("You may send another message in " + ((1000f - diff) / 1000f) + "s.").complete();
                return;
            }
        }

        lastMessage.put(author, now);

        queueMessage(QueuedMessage.builder()
                .username(courier.getOrCreateAlias(author))
                .content(message.getContentStripped())
                .system(false)
                .author(author)
                .build());
    }

    public void queueMessage(QueuedMessage message) {
        toBroadcast.add(message);
    }

    @Builder
    @Data
    public static class QueuedMessage {
        private final String content;
        private final String username;
        private final boolean system;
        private final User author;
    }
}
