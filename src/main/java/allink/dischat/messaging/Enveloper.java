package allink.dischat.messaging;

import allink.dischat.Main;
import allink.dischat.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.*;

@Slf4j
public class Enveloper {
    private final Map<TextChannel, User> channelToUser = new HashMap<>();
    private final Guild guild = Main.getGuild();

    public Set<TextChannel> getCachedTextChannels() {
        final Set<TextChannel> cachedTextChannels = new HashSet<>();

        for (Map.Entry<TextChannel, User> entry : channelToUser.entrySet()) {
            final TextChannel textChannel = entry.getKey();
            cachedTextChannels.add(textChannel);
        }

        return Set.copyOf(cachedTextChannels);
    }

    public Map<TextChannel, User> getChannelToUser() {
        return Map.copyOf(channelToUser);
    }

    public int removeUnused() {
        final Set<TextChannel> removed = new HashSet<>();

        for (Map.Entry<TextChannel, User> entry : channelToUser.entrySet()) {
            final TextChannel channel = entry.getKey();
            final User user = entry.getValue();

            try {
                final Member retrievedMember = guild.retrieveMemberById(user.getId()).complete();

                if (retrievedMember != null) {
                    continue;
                }
            } catch (Exception ignored) {
            }

            channel.delete().complete();

            removed.add(channel);
        }

        for (TextChannel removedChannel : removed) {
            channelToUser.remove(removedChannel);
        }

        return removed.size();
    }

    public int createNewIfRequired() {
        int created = 0;

        final Set<Member> members = new HashSet<>(guild.getMembers());
        members.add(guild.retrieveOwner().complete());

        for (Member member : members) {
            final JDA jda = Main.getJda();
            final SelfUser selfUser = jda.getSelfUser();
            final User asUser = member.getUser();

            if (asUser.equals(selfUser)) {
                continue;
            }

            if (asUser.isBot()) {
                continue;
            }

            if (channelToUser.containsValue(asUser)) {
                continue;
            }

            final String channelName = StringUtil.generateRandomString(8);
            final TextChannel newChannel = guild.createTextChannel(channelName)
                    .addMemberPermissionOverride(asUser.getIdLong(),
                            List.of(Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY, Permission.VIEW_CHANNEL),
                            List.of(Permission.MESSAGE_MENTION_EVERYONE, Permission.MESSAGE_ATTACH_FILES))
                    .addRolePermissionOverride(Main.getEveryoneRoleId(),
                            Collections.emptyList(),
                            List.of(Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY, Permission.VIEW_CHANNEL))
                    .complete();

            channelToUser.put(newChannel, asUser);

            created++;
        }

        return created;
    }

    public int buildCache() {
        int deleted = 0;
        final List<GuildChannel> channels = guild.getChannels();

        for (GuildChannel channel : channels) {
            if (!channel.getType().equals(ChannelType.TEXT)) {
                continue;
            }

            final TextChannel textChannel = (TextChannel) channel;
            final List<PermissionOverride> memberPermissionOverrides = textChannel.getMemberPermissionOverrides();

            if (memberPermissionOverrides.size() == 0) {
                deleted++;
                textChannel.delete().complete();
                continue;
            }

            final PermissionOverride memberPermissionOverride = memberPermissionOverrides.get(0);
            final String id = memberPermissionOverride.getId();
            final Member member = guild.retrieveMemberById(id).complete();

            if (member == null) {
                deleted++;
                textChannel.delete().complete();
                continue;
            }

            final User user = member.getUser();

            channelToUser.put(textChannel, user);
        }

        return deleted;
    }
}
