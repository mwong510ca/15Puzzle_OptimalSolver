package mwong.myprojects.fifteenpuzzle.console;

public enum ReferenceConnectionType {
    RemoteServer,
    Standalone,
    Disabled;

    @Override
    public String toString() {
        String str = "";
        switch (this) {
            case RemoteServer:
                str = "Remote connection SUCCEED. Reference collection in sync.";
                break;
            case Standalone:
                str = "Remote connection FAILED. Standalone advanced version in use.";
                break;
            case Disabled:
                str = "Unable to connect reference collection, resume standard version.";
                break;
            default:
                break;
        }
        return str;
    }
}

