package mwong.myprojects.fifteenpuzzle.server;

public class ReferenceServerProperties {
    private static final String DB_SERVICE_NAME = "ReferenceService";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 1099;

    /**
     * Retrieve the service name of the server.
     *
     * @return A string value of database service name.
     */
    public static String getDBServiceName() {
        return DB_SERVICE_NAME;
    }
    
    /**
     * Retrieve the default host name.
     *
     * @return A string value of the default host name.
     */
    public static String getDefaultHost() {
        return DEFAULT_HOST;
    }

    /**
     * Retrieve the default port number.
     *
     * @return An integer value of the default port number.
     */
    public static int getDefaultPort() {
        return DEFAULT_PORT;
    }
}
