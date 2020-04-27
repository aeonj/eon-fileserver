package eon.hg.fileserver.util.file;

public class FDfsHandler {
    public static String getStorageStatusCaption(byte status) {
        switch(status) {
            case 0:
                return "INIT";
            case 1:
                return "WAIT_SYNC";
            case 2:
                return "SYNCING";
            case 3:
                return "IP_CHANGED";
            case 4:
                return "DELETED";
            case 5:
                return "OFFLINE";
            case 6:
                return "ONLINE";
            case 7:
                return "ACTIVE";
            case 99:
                return "NONE";
            default:
                return "UNKOWN";
        }
    }

}
