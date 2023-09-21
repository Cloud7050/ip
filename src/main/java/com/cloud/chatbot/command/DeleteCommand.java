package com.cloud.chatbot.command;

import com.cloud.chatbot.Cloud;
import com.cloud.chatbot.annotation.Nullable;
import com.cloud.chatbot.item.Item;
import com.cloud.chatbot.token.CommandManager;
import com.cloud.chatbot.ui.CloudApp;



/**
 * Command for deleting an Item.
 */
public class DeleteCommand extends Command {
    public DeleteCommand(CommandManager _commandManager) {
        super(_commandManager);
    }

    @Override
    public void run() {
        @Nullable Integer number = this.verifyNumber();
        if (number == null) {
            return;
        }

        Item item = Cloud.ITEM_MANAGER.remove(number);
        CloudApp.say("Yeeted:");
        CloudApp.say(item.toString(number));
    }
}
