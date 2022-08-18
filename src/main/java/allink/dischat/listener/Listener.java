package allink.dischat.listener;

import allink.dischat.Main;
import allink.dischat.listener.impl.JoinLeaveListener;
import allink.dischat.listener.impl.MessageListener;
import allink.dischat.listener.impl.ReadyListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class Listener extends ListenerAdapter {
    public Listener() {
        Main.registerListener(this);
    }

    public static class Manager {
        public void createListeners() {
            new ReadyListener();
            new MessageListener();
            new JoinLeaveListener();
        }
    }
}
