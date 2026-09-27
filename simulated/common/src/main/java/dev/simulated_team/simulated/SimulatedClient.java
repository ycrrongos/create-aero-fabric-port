package dev.simulated_team.simulated;

/** Minimal client facade until full client port is restored. */
public class SimulatedClient {
    public static final PhysicsStaffStub PHYSICS_STAFF_CLIENT_HANDLER = new PhysicsStaffStub();
    public static final PlungerStub PLUNGER_LAUNCHER_RENDER_HANDLER = new PlungerStub();

    public static void init() {}

    public static class PhysicsStaffStub {
        public boolean holdingStaff;
        public void onItemPunched() {}
        public void onItemUsed(Object action) {}
        public void handlePacket(Object packet) {}
    }

    public static class PlungerStub {
        public void dontAnimateItem(Object hand) {}
        public void handlePacket(Object packet) {}
    }
}
