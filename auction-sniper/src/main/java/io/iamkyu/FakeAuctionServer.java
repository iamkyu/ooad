package io.iamkyu;

import org.hamcrest.Matcher;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static io.iamkyu.Main.AUCTION_RESOURCE;
import static io.iamkyu.Main.ITEM_ID_AS_LOGIN;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * FakeAuctionServer의 책임
 * 1. XMPP 브로커에 접속해 스나이퍼와의 채팅에 참여하라는 요청을 수락할 수 있다
 * 2. 스나이퍼로부터 채팅 메시지를 받거나 특정 제한 시간 내 아무런 메시지를 받지 못하면 실패한다
 * 3. 사우스비 온라인에서 명시한 대로 테스트에서 스나이퍼로 메시지를 되돌려 보낼 수 있다
 *
 * @author Kj Nam
 */
public class FakeAuctionServer {
    private final SingleMessageListener messageListener = new SingleMessageListener();

    public static final String XMPP_HOSTNAME = "localhost";
    public static final String AUCTION_PASSWORD = "auction";

    private String itemId;
    private XMPPConnection connection;
    private Chat currentChat;

    public FakeAuctionServer(String itemId) {
        this.itemId = itemId;
        this.connection = new XMPPConnection(XMPP_HOSTNAME);
    }

    public void startSellingItem() throws XMPPException {
        connection.connect();
        connection.login(format(ITEM_ID_AS_LOGIN, itemId), AUCTION_PASSWORD, AUCTION_RESOURCE);
        connection.getChatManager().addChatListener(
                (chat, createdLocally) -> {
                    currentChat = chat;
                    chat.addMessageListener(messageListener);
                }
        );
    }

    public void reportPrice(int price, int increment, String bidder) throws XMPPException {
        currentChat.sendMessage(String.format(
                "SQLVersion: 1.1; Event: PRICE; CurrentPrice: %d; Increment: %d; Bidder: %s",
                price, increment, bidder
        ));
    }

    public String getItemId() {
        return itemId;
    }

    public void hasReceivedJoinRequestFrom(String sniperId) throws InterruptedException {
        receivesAMessageMatching(sniperId, equalTo(Main.JOIN_COMMAND_FORMAT));
    }

    private void receivesAMessageMatching(String sniperId, Matcher<String> messageMatcher) throws InterruptedException {
        messageListener.receivesAMessage(messageMatcher);
        assertThat(currentChat.getParticipant(), equalTo(sniperId));
    }

    public void hasReceivedJoinRequestFromSniper() throws InterruptedException {
        messageListener.receivesAMessage(is(anything()));
    }

    public void hasReceivedBid(int bid, String sniperId) throws InterruptedException {
        receivesAMessageMatching(sniperId, equalTo(String.format(Main.BID_COMMAND_FORMAT, bid)));
    }

    public void announceClosed() throws XMPPException {
        currentChat.sendMessage("SQLVersion: 1.1; Event: CLOSE;");
    }

    public void stop() {
        connection.disconnect();
    }

    class SingleMessageListener implements MessageListener {
        private final ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<>(1);

        @Override
        public void processMessage(Chat chat, Message message) {
            messages.add(message);
        }

        public void receivesAMessage(Matcher<? super String> messageMatcher) throws InterruptedException {
            Message message = messages.poll(5, TimeUnit.SECONDS);
            assertThat("Message", message, is(notNullValue()));
            assertThat(message.getBody(), messageMatcher);
        }
    }
}
