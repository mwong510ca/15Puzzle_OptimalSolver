package mwong.myprojects.fifteenpuzzle.server;

import mwong.myprojects.fifteenpuzzle.PropertiesCache;

/**
 * ReferenceServerProperties contains the service name, host and port information
 * for the remote server.
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class ReferenceServerProperties {
    private static final String DEFAULT_SERVICE_NAME = "ReferenceService";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 1099;
    private static String remoteServiceName = DEFAULT_SERVICE_NAME;
    private static String remoteHost = DEFAULT_HOST;
    private static int remotePort = DEFAULT_PORT;

    static {
        if (PropertiesCache.getInstance().containsKey("remoteServiceName")) {
            remoteServiceName = PropertiesCache.getInstance().getProperty("remoteServiceName");
        }

        if (PropertiesCache.getInstance().containsKey("remoteHost")) {
            remoteHost = PropertiesCache.getInstance().getProperty("remoteHost");
        }

        if (PropertiesCache.getInstance().containsKey("remotePort")) {
            try {
                int port = Integer.parseInt(PropertiesCache.getInstance().getProperty(
                        "remotePort"));
                if (port >= 1024 || port <= 65535) {
                    remotePort = port;
                }
            } catch (NumberFormatException ex) {
                // do nothing;
            }
        }
    }

    /**
     * Retrieve the service name of the server.
     *
     * @return A string value of database service name.
     */
    public static String getRemoteServiceName() {
        return remoteServiceName;
    }

    /**
     * Retrieve the default host name.
     *
     * @return A string value of the default host name.
     */
    public static String getRemoteHost() {
        return remoteHost;
    }

    /**
     * Retrieve the default port number.
     *
     * @return An integer value of the default port number.
     */
    public static int getRemotePort() {
        return remotePort;
    }
}
