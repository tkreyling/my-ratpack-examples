package myratpackexamples.webpoll;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Value;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.jackson.Jackson;

@Value
@AllArgsConstructor(onConstructor=@__(@Inject))
public class RetrievePollHandler implements Handler {
    PollRepository pollRepository;

    @Override
    public void handle(Context context) throws Exception {
        String pollId = context.getPathTokens().get("poll");

        pollRepository.retrievePoll(pollId)
                .map(Jackson::json)
                .then(context::render);
    }
}
