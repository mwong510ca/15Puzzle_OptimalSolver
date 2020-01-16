package mwong.myprojects.fifteenpuzzle.server;

/**
 * ReferenceServerProperties contains the service name, host and port information
 * for the remote server.
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class ReferenceServerProperties {
    private static final String REMOTE_SERVICE_NAME = "ReferenceService";
    private static final String REMOTE_HOST = "localhost";
    private static final int REMOTE_PORT = 1099;

    /**
     * Retrieve the service name of the server.
     *
     * @return A string value of database service name.
     */
    public static String getRemoteServiceName() {
        return REMOTE_SERVICE_NAME;
    }

    /**
     * Retrieve the default host name.
     *
     * @return A string value of the default host name.
     */
    public static String getRemoteHost() {
        return REMOTE_HOST;
    }

    /**
     * Retrieve the default port number.
     *
     * @return An integer value of the default port number.
     */
    public static int getRemotePort() {
        return REMOTE_PORT;
    }
}
