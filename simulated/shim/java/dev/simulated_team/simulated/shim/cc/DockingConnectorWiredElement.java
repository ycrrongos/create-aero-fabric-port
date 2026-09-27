package dev.simulated_team.simulated.shim.cc;

/** Optional ComputerCraft stand-in. */
public class DockingConnectorWiredElement {
    public DockingConnectorWiredElement(Object be) {}

    public static DockingConnectorWiredElement create(Object be) {
        return new DockingConnectorWiredElement(be);
    }

    public void invalidate() {}
    public void setRemoved() {}
    public void remove() {}

    public void connect(DockingConnectorWiredElement other) {}

    public void disconnect(DockingConnectorWiredElement other) {}
}
