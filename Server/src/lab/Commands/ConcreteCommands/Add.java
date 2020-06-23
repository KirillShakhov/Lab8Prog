package lab.Commands.ConcreteCommands;


import lab.Commands.Command;
import lab.Commands.CommandReceiver;
import lab.Commands.SerializedCommands.Message;

import java.io.IOException;

/**
 * Конкретная команда добавления в коллекцию.
 */

public class Add extends Command {
    private static final long serialVersionUID = 33L;

    @Override
    public String execute(Object argObject) {
        CommandReceiver commandReceiver = new CommandReceiver();
        return commandReceiver.add(((Message) argObject).getMusicBand());
    }
}
