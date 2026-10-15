package io.vidocq.tools.vaadin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/** A plain Vaadin view: a text field, a button and a greeting. */
@Route("")
public class MainView extends VerticalLayout {

    public MainView() {
        TextField name = new TextField("Your name");
        Paragraph greeting = new Paragraph();
        Button hello = new Button("Say hello",
                e -> greeting.setText("Hello " + name.getValue() + ", from Vaadin on Vidocq"));
        add(new H1("Vidocq + Vaadin"), name, hello, greeting);
    }
}
