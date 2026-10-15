package io.vidocq.tools.vaadin;

import io.vidocq.runtime.core.Vidocq;
import io.vidocq.runtime.spi.VidocqApp;
import io.vidocq.runtime.spi.VidocqMain;

/** Starts the Vidocq runtime; Foy serves Vaadin until the process is stopped. */
@VidocqMain
public class App implements VidocqApp {

    static void main(String[] args) {
        Vidocq.run(App.class, args);
    }

    @Override
    public int run(String... args) throws Exception {
        Vidocq.waitForExit();
        return 0;
    }
}
