package myratpackexamples.webpoll;

import java.util.HashMap;
import java.util.Map;

public class PollRepository {
    private static Map<String, Poll> polls = new HashMap<>();

    public void storePoll(Poll poll) {
        polls.put(poll.getId(), poll);
    }

    public Poll retrievePoll(String pollId) {
        return polls.get(pollId);
    }
}
