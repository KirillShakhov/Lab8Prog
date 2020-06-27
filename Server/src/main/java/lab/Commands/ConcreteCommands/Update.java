package lab.Commands.ConcreteCommands;


import lab.Commands.Command;
import lab.Commands.CommandReceiver;
import lab.Commands.SerializedCommands.Message;

/**
 * Конкретная команда обновления объекта.
 */
public class Update extends Command {
    private static final long serialVersionUID = 33L;

    @Override
    public String execute(Object argObject) {
        Message mes = (Message) argObject;
        if (mes.getArgs() != null) {
            CommandReceiver commandReceiver = new CommandReceiver();
            return commandReceiver.update(mes);
        }
        return null;
    }
}
