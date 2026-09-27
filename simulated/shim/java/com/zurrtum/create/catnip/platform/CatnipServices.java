package com.zurrtum.create.catnip.platform;

/** Minimal CatnipServices stand-in for Create Fly. */
public final class CatnipServices {
    private CatnipServices() {}

    public static final Platform PLATFORM = new Platform();
    public static final Network NETWORK = new Network();

    public static final class Platform {
        public boolean isDevelopmentEnvironment() {
            return false;
        }
    }

    public static final class Network {
        public void sendToAllClients(Object packet) {}
        public void sendToServer(Object packet) {}
        public void sendToClient(Object player, Object packet) {}
    }
}
