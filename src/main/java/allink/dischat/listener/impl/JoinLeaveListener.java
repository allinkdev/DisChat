package allink.dischat.listener.impl;

import allink.dischat.Main;
import allink.dischat.listener.Listener;
import allink.dischat.messaging.Courier;
import allink.dischat.messaging.Enveloper;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import org.jetbrains.annotations.NotNull;

public class JoinLeaveListener extends Listener {
    private void resetMemberCache() {
        Main.getGuild().pruneMemberCache();
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        resetMemberCache();

        final Enveloper enveloper = Main.getEnveloper();
        final Courier courier = Main.getCourier();

        enveloper.createNewIfRequired();
        courier.refreshWebhooks();
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        resetMemberCache();

        final Enveloper enveloper = Main.getEnveloper();
        final Courier courier = Main.getCourier();

        enveloper.removeUnused();
        courier.refreshWebhooks();
    }
}
