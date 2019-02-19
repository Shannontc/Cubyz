package io.spacycubyd.command;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for all game commands.
 * @author zenith391
 */
public abstract class CommandBase {

	protected String name;
	protected List<Permission> perms = new ArrayList<>();
	
	public abstract void commandExecute(ICommandSource source, String[] args);
	
	/**
	 * Returns the name of the Command
	 * @return command name
	 */
	public String getCommandName() {
		return name;
	}
	
	public Permission[] getRequiredPermissions() {
		return perms.toArray(new Permission[perms.size()]); // convert list to an array
	}
	
}