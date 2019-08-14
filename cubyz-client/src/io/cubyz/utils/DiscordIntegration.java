package io.cubyz.utils;

import java.util.Random;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordEventHandlers.OnReady;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import club.minnced.discord.rpc.DiscordUser;
import io.cubyz.client.Cubyz;
import io.cubyz.ui.ToastManager;
import io.cubyz.ui.ToastManager.Toast;

public class DiscordIntegration {

	static DiscordRichPresence presence;
	static Thread worker;
	
	public static String generatePartyID() {
		return Integer.toHexString(new Random().nextInt());
	}
	
	public static void startRPC() {
		DiscordRPC lib = DiscordRPC.INSTANCE;
		String appID = "527033701343952896";
		DiscordEventHandlers handlers = new DiscordEventHandlers();
		handlers.errored = handlers.disconnected = new DiscordEventHandlers.OnStatus() {

			@Override
			public void accept(int errorCode, String message) {
				System.err.println(errorCode + ": " + message);
				ToastManager.queuedToasts.push(new Toast("Discord Integration", "An error occured: " + message));
			}
			
		};
		handlers.ready = new OnReady() {
			@Override
			public void accept(DiscordUser user) {
				ToastManager.queuedToasts.push(new Toast("Discord Integration", "Hello " + user.username + " !"));
				System.out.println("Linked!");
			}
			
		};
		handlers.joinGame = (secret) -> {
			String serverIP = secret.split(":")[0];
			int serverPort = Integer.parseInt(secret.split(":")[1]);
			System.out.println("Attempting to join server " + serverIP + " at port " + serverPort);
			Cubyz.requestJoin(serverIP, serverPort);
		};
		
		handlers.joinRequest = (user) -> {
			ToastManager.queuedToasts.push(new Toast("Discord Integration", "Join request from " + user.username));
			if (Cubyz.serverOnline < Cubyz.serverCapacity) {
				lib.Discord_Respond(user.userId, DiscordRPC.DISCORD_REPLY_YES);
			} else {
				lib.Discord_Respond(user.userId, DiscordRPC.DISCORD_REPLY_NO);
			}
		};
		String javaExec = System.getProperty("java.home") + "/bin/java.exe";
		String classpath = System.getProperty("java.class.path");
		lib.Discord_Initialize(appID, handlers, false, null);
		
		String path = javaExec + " -cp " + classpath + " io.cubyz.client.GameLauncher";
		Cubyz.log.fine("Registered launch path as " + path);
		lib.Discord_Register(appID, path);
		lib.Discord_RunCallbacks();
		
		presence = new DiscordRichPresence();
		presence.largeImageKey = "cubz_logo";
		//presence.largeImageText = Cubyz.serverIP;
		
		//presence.joinSecret = Cubyz.serverIP + ":" + Cubyz.serverPort;
		//presence.partySize = Cubyz.serverOnline;
		//presence.partyMax = Cubyz.serverCapacity;
		
		//presence.partyId = generatePartyID();
		
		
		worker = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                lib.Discord_RunCallbacks();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                	break;
                }
            }
        });
		worker.setName("RPC-Callback-Handler");
		worker.start();
		Cubyz.log.info("Discord RPC integration opened!");
		ToastManager.queuedToasts.add(new Toast("Discord Integration", "Linking.."));
		setStatus("On Main Menu.");
	}
	
	public static boolean isEnabled() {
		return worker != null;
	}
	
	public static void updateState() {
		if (Cubyz.isIntegratedServer) {
			presence.state = "Singleplayer";
		}
		else {
			if (Cubyz.isOnlineServerOpened) {
				presence.state = "Join me ;)";
				presence.partyMax = 50; // temporary
			} else {
				presence.state = "Multiplayer";
				presence.partyMax = 50; // temporary
			}
		}
		DiscordRPC.INSTANCE.Discord_UpdatePresence(presence);
	}
	
	public static void setStatus(String status) {
		if (isEnabled()) {
			presence.details = status;
			updateState();
		}
	}
	
	public static void closeRPC() {
		DiscordRPC lib = DiscordRPC.INSTANCE;
		if (worker != null)
			worker.interrupt();
		worker = null;
		lib.Discord_Shutdown();
	}
	
}
