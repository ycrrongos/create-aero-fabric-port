package dev.simulated_team.simulated.fabric.cc;

/** No-op CC wired element for Fabric spike (ComputerCraft not on classpath). */
public class DockingConnectorWiredElement {
    public static DockingConnectorWiredElement create(final Object be) {
        return new DockingConnectorWiredElement();
    }

    public void connect(final DockingConnectorWiredElement other) {}
    public void disconnect(final DockingConnectorWiredElement other) {}
    public void remove() {}
}
