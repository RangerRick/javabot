package javabot.operations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.antwerkz.maven.SPI;
import javabot.Action;
import javabot.IrcEvent;
import javabot.Javabot;
import javabot.Message;
import javabot.TellMessage;
import javabot.dao.FactoidDao;
import javabot.model.Factoid;
import javabot.operations.throttle.Throttler;
import org.schwering.irc.lib.IRCUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@SPI(StandardOperation.class)
public class GetFactoidOperation extends StandardOperation {
    private static final Logger log = LoggerFactory.getLogger(GetFactoidOperation.class);
    private static final Throttler<TellInfo> throttler = new Throttler<TellInfo>(100, Javabot.THROTTLE_TIME);
    @Autowired
    private FactoidDao factoidDao;

    public GetFactoidOperation() {
    }

    public GetFactoidOperation(final Javabot javabot) {
        setBot(javabot);
    }

    @Override
    public List<Message> handleMessage(final IrcEvent event) {
        List<Message> responses = tell(event);
        if (responses.isEmpty()) {
            responses.add(getFactoid(null, event.getMessage(), event.getSender(), event.getChannel(), event,
                new HashSet<String>()));
        }
        return responses;
    }

    private Message getFactoid(final TellSubject subject, final String toFind, final IRCUser sender,
        final String chadnnel, final IrcEvent event, final Set<String> backtrack) {
        String message = toFind;
        if (message.endsWith(".") || message.endsWith("?") || message.endsWith("!")) {
            message = message.substring(0, message.length() - 1);
        }
        final String firstWord = message.split(" ")[0];
        final String params = message.substring(firstWord.length()).trim();
        Factoid factoid = factoidDao.getFactoid(message.toLowerCase());
        if (factoid == null) {
            factoid = factoidDao.getParameterizedFactoid(firstWord);
        }
        return factoid != null
            ? getResponse(subject, sender, event, backtrack, params, factoid)
            : new Message(event.getChannel(), event, sender + ", what does that even *mean*?");
    }

    private Message getResponse(final TellSubject subject, final IRCUser sender,
        final IrcEvent event, final Set<String> backtrack, final String replacedValue, final Factoid factoid) {
        final String message = factoid.evaluate(subject, sender.getNick(), replacedValue);
        if (message.startsWith("<see>")) {
            if (backtrack.contains(message)) {
                return new Message(event.getChannel(), event, "Loop detected for factoid '" + message + "'.");
            } else {
                backtrack.add(message);
                return getFactoid(subject, message.substring(5).trim(), sender, event.getChannel(), event, backtrack);
            }
        } else if (message.startsWith("<reply>")) {
            return new Message(event.getChannel(), event, message.substring("<reply>".length()));
        } else if (message.startsWith("<action>")) {
            return new Action(event.getChannel(), event, message.substring("<action>".length()));
        } else {
            return new Message(event.getChannel(), event, message);
        }
    }

    private List<Message> tell(final IrcEvent event) {
        final String message = event.getMessage();
        final String channel = event.getChannel();
        final IRCUser sender = event.getSender();
        final List<Message> responses = new ArrayList<Message>();
        if (isTellCommand(message)) {
            final TellSubject tellSubject = parseTellSubject(event, message);
            if (tellSubject == null) {
                responses.add(new Message(channel, event,
                    String.format("The syntax is: tell nick about factoid - you missed out the 'about', %s", sender)));
            } else {
                IRCUser user = tellSubject.getTarget();
                if ("me".equalsIgnoreCase(user.getNick())) {
                    user = sender;
                }
                final String thing = tellSubject.getSubject();
                if (user.getNick().equalsIgnoreCase(getBot().getNick())) {
                    responses.add(new Message(channel, event, "I don't want to talk to myself"));
                } else {
                    final TellInfo info = new TellInfo(user, thing);
                    if (throttler.isThrottled(info)) {
                        responses.add(new Message(channel, event, sender + ", Slow down, Speedy Gonzalez!"));
                    } else if (!getBot().userIsOnChannel(user, channel)) {
                        responses.add(new Message(channel, event, "The user " + user + " is not on " + channel));
                    } else if (sender.getNick().equals(channel) && !getBot().isOnSameChannelAs(user)) {
                        responses
                            .add(new Message(sender, event, "I will not send a message to someone who is not on any"
                                + " of my channels."));
                    } else if (thing.endsWith("++") || thing.endsWith("--")) {
                        responses.add(new Message(channel, event, "I'm afraid I can't let you do that, Dave."));
                    } else {
                        final List<Message> list = getBot().getListener().getResponses(channel, sender, thing);
                        for (final Message msg : list) {
                            responses
                                .add(new TellMessage(user, msg.getDestination(), msg.getEvent(), msg.getMessage()));
                        }
                        throttler.addThrottleItem(info);
                    }
                }
            }
        }
        return responses;
    }

    private TellSubject parseTellSubject(final IrcEvent event, final String message) {
        if (message.startsWith("tell ")) {
            return parseLonghand(event, message);
        }
        return parseShorthand(event, message);
    }

    private TellSubject parseLonghand(final IrcEvent event, final String message) {
        final String body = message.substring("tell ".length());
        final String nick = body.substring(0, body.indexOf(" "));
        final int about = body.indexOf("about ");
        if (about < 0) {
            return null;
        }
        final String thing = body.substring(about + "about ".length());
        return new TellSubject(getBot().getUser(nick), thing);
    }

    private TellSubject parseShorthand(final IrcEvent event, final String message) {
        String target = message;//.substring(0, space);
        for (final String start : getBot().getStartStrings()) {
            if (target.startsWith(start)) {
                target = target.substring(start.length()).trim();
            }
        }
        final int space = target.indexOf(' ');
        final String user = target.substring(0, space);
        final String value = target.substring(space + 1).trim();
        return space < 0 ? null : new TellSubject(getBot().getUser(user.trim()), value);
    }

    private boolean isTellCommand(final String message) {
        return message.startsWith("tell ") || message.startsWith("~");
    }
}