package allink.dischat;

import allink.dischat.configuration.Configuration;
import allink.dischat.listener.Listener;
import allink.dischat.messaging.Courier;
import allink.dischat.messaging.Enveloper;
import allink.dischat.tasks.CheckChannelsTask;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Main {
    private static final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    @Getter
    private static JDA jda;
    @Getter
    private static Configuration.Manager configurationManager;
    private static Listener.Manager listenerManager;
    @Getter
    private static User holder;
    @Getter
    private static Guild guild;
    @Getter
    private static Courier courier;
    @Getter
    private static Enveloper enveloper;
    @Getter
    private static long everyoneRoleId;
    @Getter
    private static String systemMessageAvatarUrl;

    public static void main(String[] args) {
        try {
            configurationManager = new Configuration.Manager();
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialize configuration manager!", e);
        }

        final Configuration configuration = configurationManager.getConfiguration();

        final JDABuilder jdaBuilder = JDABuilder.create(configuration.getToken(), GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_WEBHOOKS, GatewayIntent.DIRECT_MESSAGES);

        jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
        jdaBuilder.enableCache(CacheFlag.MEMBER_OVERRIDES);

        try {
            jda = jdaBuilder.build();
        } catch (LoginException e) {
            throw new RuntimeException("Unable to login to Discord!", e);
        }

        listenerManager = new Listener.Manager();
        listenerManager.createListeners();

        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while awaiting for ready status!", e);
        }

        systemMessageAvatarUrl = jda.getSelfUser().getAvatarUrl();

        holder = jda.getUserById(configuration.getOwner());
        guild = jda.getGuildById(configuration.getGuildId());

        if (guild == null) {
            throw new RuntimeException("Guild does not exist!");
        }

        everyoneRoleId = guild.getPublicRole().getIdLong();

        enveloper = new Enveloper();
        courier = new Courier();

        enveloper.buildCache();
        enveloper.removeUnused();
        enveloper.createNewIfRequired();

        courier.refreshWebhooks();

        scheduledExecutor.scheduleAtFixedRate(CheckChannelsTask::new, 5, 5, TimeUnit.MINUTES);
    }

    public static void registerListener(Listener listener) {
        jda.addEventListener(listener);
    }
}
