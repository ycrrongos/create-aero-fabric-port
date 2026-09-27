package com.zurrtum.create.client;

/** Minimal CreateClient stand-in for Create Fly. */
public class CreateClient {
    public static final ZapperStub ZAPPER_RENDER_HANDLER = new ZapperStub();
    public static final ValueSettingsStub VALUE_SETTINGS_HANDLER = new ValueSettingsStub();

    public static class ZapperStub {
        public void shoot(Object hand, Object pos) {}
    }

    public static class ValueSettingsStub {
        public void showHoverTip(Object tip) {}
    }
}
