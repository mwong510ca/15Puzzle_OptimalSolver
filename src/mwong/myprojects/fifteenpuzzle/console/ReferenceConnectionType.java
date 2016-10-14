package mwong.myprojects.fifteenpuzzle.console;

/**
 * ReferenceConnectionType that can be used.
 * <li>{@link #RIGHT}</li>
 * <li>{@link #DOWN}</li>
 * <li>{@link #LEFT}</li>
 * <li>{@link #UP}</li>
 * <li>{@link #NONE}</li>
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public enum ReferenceConnectionType {
    REMOTESERVER,
    STANDALONE,
    DISABLED;

    @Override
    public String toString() {
        String str = "";
        switch (this) {
            case REMOTESERVER:
                str = "Remote connection SUCCEED. Reference collection in sync.";
                break;
            case STANDALONE:
                str = "Remote connection FAILED. Standalone advanced version in use.";
                break;
            case DISABLED:
                str = "Unable to connect reference collection, resume standard version.";
                break;
            default:
                break;
        }
        return str;
    }
}

