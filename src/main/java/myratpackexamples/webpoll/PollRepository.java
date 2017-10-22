package myratpackexamples.webpoll;

import ratpack.exec.Promise;

import java.util.HashMap;
import java.util.Map;

import static ratpack.exec.Promise.value;

public class PollRepository {
    private static Map<String, Poll> polls = new HashMap<>();

    public Promise<Void> storePoll(Poll poll) {
        polls.put(poll.getId(), poll);
        return value(null);
    }

    public Promise<Poll> retrievePoll(String pollId) {
        return value(polls.get(pollId));
    }
}
