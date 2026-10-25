module io.vidocq.tools.vaadin {
    requires io.vidocq.runtime.extensions.jakartaee.web.foy;
    requires io.vidocq.foy.api;
    requires io.vidocq.vauban.api;
    requires jakarta.servlet;
    // The Vaadin jars have no module-info: their module names come from their file names.
    requires flow.server;
    requires flow.html.components;
    requires vaadin.button.flow;
    requires vaadin.text.field.flow;
    requires vaadin.ordered.layout.flow;
    // Vaadin creates the routes by reflection.
    opens io.vidocq.tools.vaadin;
}
