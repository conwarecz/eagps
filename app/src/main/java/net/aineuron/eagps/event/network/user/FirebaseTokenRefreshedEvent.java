package net.aineuron.eagps.event.network.user;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 23.10.2017.
 */

public class FirebaseTokenRefreshedEvent {
    public final String token;

    public FirebaseTokenRefreshedEvent(String token) {
        this.token = token;
    }
}
