package allink.dischat.listener.impl;

import allink.dischat.Main;
import allink.dischat.listener.Listener;
import allink.dischat.messaging.Queue;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class MessageListener extends Listener {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        final JDA jda = event.getJDA();
        final Message message = event.getMessage();
        final User author = message.getAuthor();

        if (author.isBot()) {
            return;
        }

        if (author.equals(jda.getSelfUser())) {
            return;
        }

        final ChannelType channelType = event.getChannelType();

        if (channelType.equals(ChannelType.PRIVATE)) {
            final User holder = Main.getHolder();
            message.reply("Sending messages through private messages is currently disabled. If you have any questions or requests, please forward them to " + holder.getAsMention() + ", or to our moderation team!\nNote: Your message has not been forwarded to our administration team.").complete();
            return;
        }

        if (!channelType.equals(ChannelType.TEXT)) {
            return;
        }

        final Queue queue = Main.getCourier().getQueue();

        queue.queueMessage(message, author);
    }
}
