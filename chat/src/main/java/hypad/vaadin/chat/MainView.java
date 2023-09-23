package hypad.vaadin.chat;

import com.github.rjeschke.txtmark.Processor;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.shared.Registration;

@Route("")
@Push
public class MainView extends VerticalLayout implements AppShellConfigurator {
    private  Grid<Storage.ChatMessage>grid;
    private  VerticalLayout chat;
    private final Storage storage;
    private Registration registration;
    private UI ui;
    private String user = "";
    private VerticalLayout login;

    public MainView(Storage storage){
        this.storage = storage;
        buildLogin();
        buildChat();
    }

    private void buildLogin() {
    login = new VerticalLayout(){{
        TextField textField = new TextField();
        textField.setPlaceholder("Introduce yourself");
        add(
            textField,
                new Button("Login"){{
                    addClickListener(click ->{
                        login.setVisible(false);
                        chat.setVisible(true);
                        user=textField.getValue();
                        storage.addRecordJoined(user);
                    });
                    addClickShortcut(Key.ENTER);
                }}
        );
    }};
    add(login);
    }

    private void buildChat() {
        chat = new VerticalLayout();
        add(chat);
        chat.setVisible(false);
        grid = new Grid<>();

        grid.setItems(storage.getMessages());
        grid.addColumn(new ComponentRenderer<>(message-> new Html(renderRow(message))))
                .setAutoWidth(true);


        TextField textField = new TextField();
        chat.add(
                new H3("Chat"),
                grid,
                new HorizontalLayout(){{
                   add( textField,
                            new Button("➡"){{
                                addClickListener(click ->{
                                    storage.addRecord(user,textField.getValue());
                                    textField.clear();
                                });
                                addClickShortcut(Key.ENTER);
                            }}
                   );
                }}

        );
    }

    public void onMessage(Storage.ChatEvent event){
        if(getUI().isPresent()){
            ui = getUI().get();
            ui.getSession().lock();
            ui.beforeClientResponse(grid,ctx->grid.scrollToEnd());
            ui.access(() -> grid.getDataProvider().refreshAll());
            ui.getSession().unlock();
        }
    }

    private static String renderRow(Storage.ChatMessage message) {
        if(message.getName().isEmpty()){
            return Processor.process(String.format("_User **%s** %s has joined chat_", message.getName(), message.getMessage()));

        }
        else {
            return Processor.process(String.format("**%s**: %s", message.getName(), message.getMessage()));
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent){
        registration = storage.attachListener(this::onMessage);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent){
        registration.remove();
    }


}
