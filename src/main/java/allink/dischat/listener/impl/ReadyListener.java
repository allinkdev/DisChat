package allink.dischat.listener.impl;

import allink.dischat.listener.Listener;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.events.ReadyEvent;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class ReadyListener extends Listener {
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        final JDA jda = event.getJDA();
        final SelfUser selfUser = jda.getSelfUser();

        log.info("Ready as {}!", selfUser.getAsTag());

        jda.getPresence().setPresence(Activity.listening("your messages!"), false);
    }
}
