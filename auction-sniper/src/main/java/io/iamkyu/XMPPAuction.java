package io.iamkyu;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;

import static io.iamkyu.Main.BID_COMMAND_FORMAT;
import static io.iamkyu.Main.JOIN_COMMAND_FORMAT;

/**
 * @author Kj Nam
 */
public class XMPPAuction implements Auction {

    private Chat chat;

    public XMPPAuction(Chat chat) {
        this.chat = chat;
    }

    @Override
    public void bid(int amount) {
        sendMessage(String.format(BID_COMMAND_FORMAT, amount));
    }

    @Override
    public void join() {
        sendMessage(JOIN_COMMAND_FORMAT);
    }

    private void sendMessage(String message) {
        try {
            chat.sendMessage(message);
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }
}
