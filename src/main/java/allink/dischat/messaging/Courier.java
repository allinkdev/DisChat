package allink.dischat.messaging;

import allink.dischat.Main;
import allink.dischat.util.StringUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.jodah.expiringmap.ExpiringMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Courier {
    private final Map<TextChannel, Webhook> channelToWebhook = new HashMap<>();
    private final Map<User, String> userToAlias = ExpiringMap.builder()
            .expiration(1, TimeUnit.HOURS)
            .build();
    private final Guild guild = Main.getGuild();
    @Getter
    private final Queue queue;

    public Courier() {
        this.queue = new Queue(this);
    }

    public void refreshWebhooks() {
        final List<GuildChannel> channels = guild.getChannels();

        for (GuildChannel channel : channels) {
            if (!channel.getType().equals(ChannelType.TEXT)) {
                continue;
            }

            final TextChannel textChannel = (TextChannel) channel;
            channelToWebhook.put(textChannel, getOrCreateWebhook(textChannel));
        }

        for (Map.Entry<TextChannel, Webhook> entry : channelToWebhook.entrySet()) {
            final TextChannel textChannel = entry.getKey();
            final String channelId = textChannel.getId();

            if (guild.getChannelById(TextChannel.class, channelId) != null) {
                continue;
            }

            log.info("Removed unused channel {} from webhook cache!", channelId);
        }
    }

    public Webhook getOrCreateWebhook(TextChannel textChannel) {
        if (channelToWebhook.containsKey(textChannel)) {
            return channelToWebhook.get(textChannel);
        }

        return getOrCreateWebhookUncached(textChannel);
    }

    private Webhook getOrCreateWebhookUncached(TextChannel textChannel) {
        final List<Webhook> webhooks = textChannel.retrieveWebhooks().complete();

        if (webhooks.size() > 0) {
            return webhooks.get(0);
        }

        return textChannel.createWebhook("DisChat Relay").complete();
    }

    public String getOrCreateAlias(User user) {
        if (userToAlias.containsKey(user)) {
            return userToAlias.get(user);
        }

        final String username = StringUtil.generateRandomString(8);

        userToAlias.put(user, username);
        return username;
    }
}
