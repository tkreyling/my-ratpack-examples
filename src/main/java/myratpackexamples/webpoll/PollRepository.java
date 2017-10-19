package myratpackexamples.webpoll;

import java.util.HashMap;
import java.util.Map;

public class PollRepository {
    private static Map<String, Poll> polls = new HashMap<>();

    public static void storePoll(Poll poll) {
        polls.put(poll.getId(), poll);
    }

    public static Poll retrievePoll(String pollId) {
        return polls.get(pollId);
    }
}
