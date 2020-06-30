package clientfx.Commands.ConcreteCommands;


import clientfx.Commands.Command;
import clientfx.Commands.CommandReceiver;
import clientfx.Commands.SerializedCommands.Message;

/**
 * Конкретная команда добавления в коллекцию.
 */
public class Auth extends Command {
    private static final long serialVersionUID = 33L;
    transient private CommandReceiver commandReceiver;

    public Auth (CommandReceiver commandReceiver) {
        this.commandReceiver = commandReceiver;
    }

    public Auth() {}

    @Override
    protected Message execute(String[] args) {
        if (args.length > 1) {
            System.out.println("Введен не нужный аргумент. Команда приведена к базовой команде add.");
        }
        return commandReceiver.auth();
    }

    @Override
    protected void writeInfo() {
        System.out.println("Команда add – добавить новый элемент в коллекцию.");
    }
}
