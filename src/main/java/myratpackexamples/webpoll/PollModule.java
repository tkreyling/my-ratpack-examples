package myratpackexamples.webpoll;

import com.google.inject.Binder;
import com.google.inject.Module;

public class PollModule implements Module {
    @Override
    public void configure(Binder binder) {
        binder.bind(CreatePollHandler.class);
    }
}
