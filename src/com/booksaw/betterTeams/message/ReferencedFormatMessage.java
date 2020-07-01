package com.booksaw.betterTeams.message;

import org.bukkit.command.CommandSender;

/**
 * Used when sending a message to the user which has a fixed format and is
 * loaded from messages.yml
 * 
 * @author booksaw
 *
 */
public class ReferencedFormatMessage implements Message {

	String reference;
	Object[] replacement;

	/**
	 * @param reference the messages.yml reference for the command
	 */
	public ReferencedFormatMessage(String reference, String... replacement) {
		this.reference = reference;
		this.replacement = (Object[]) replacement;
	}

	@Override
	public void sendMessage(CommandSender sender) {
		MessageManager.sendMessageF(sender, reference, replacement);
	}

}