package allink.dischat.tasks;

import allink.dischat.Main;
import allink.dischat.messaging.Courier;
import allink.dischat.messaging.Enveloper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CheckChannelsTask implements Runnable {
    @Override
    public void run() {
        final Enveloper enveloper = Main.getEnveloper();

        int removeAmount = enveloper.removeUnused();
        int creationAmount = enveloper.createNewIfRequired();

        final Courier courier = Main.getCourier();

        courier.refreshWebhooks();

        if (removeAmount > 0) {
            log.info("Removed {} unused channels.", removeAmount);
        }

        if (creationAmount > 0) {
            log.info("Created {} new channels.", creationAmount);
        }
    }
}
