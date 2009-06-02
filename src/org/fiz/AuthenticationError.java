package org.fiz;

/**
 * AuthenticationError is thrown when a request arrives without a valid
 * authentication token. It may indicate that a CSRF attack is being
 * attempted, or just a session expiration.
 */
public class AuthenticationError extends UserError {
    /**
     * Construct a AuthenticationError.
     */
    public AuthenticationError() {
        super("invalid or missing authentication token; most likely " +
                "the page is stale and needs to be refreshed");
    }
}
