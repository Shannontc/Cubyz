package io.cubyz.ui;

import io.cubyz.client.Cubyz;
import io.cubyz.items.Inventory;
import io.cubyz.ui.components.InventorySlot;
import io.jungle.Window;

public class GameOverlay extends MenuGUI {

	int crosshair;
	int selection;
	int[] healthBar;
	
	long lastPlayerHurtMs; // stored here and not in Player for easier multiplayer integration
	float lastPlayerHealth;

	private InventorySlot inv [] = new InventorySlot[8];
	
	@Override
	public void init(long nvg) {
		crosshair = NGraphics.loadImage("assets/cubyz/textures/crosshair.png");
		selection = NGraphics.loadImage("assets/cubyz/guis/inventory/selected_slot.png");
		healthBar = new int[8];
		healthBar[0] = NGraphics.loadImage("assets/cubyz/textures/health_bar_beg_empty.png");
		healthBar[1] = NGraphics.loadImage("assets/cubyz/textures/health_bar_beg_full.png");
		healthBar[2] = NGraphics.loadImage("assets/cubyz/textures/health_bar_end_empty.png");
		healthBar[3] = NGraphics.loadImage("assets/cubyz/textures/health_bar_end_full.png");
		healthBar[4] = NGraphics.loadImage("assets/cubyz/textures/health_bar_mid_empty.png");
		healthBar[5] = NGraphics.loadImage("assets/cubyz/textures/health_bar_mid_half.png");
		healthBar[6] = NGraphics.loadImage("assets/cubyz/textures/health_bar_mid_full.png");
		healthBar[7] = NGraphics.loadImage("assets/cubyz/textures/health_bar_icon.png");
		Inventory inventory = Cubyz.world.getLocalPlayer().getInventory();
		for(int i = 0; i < 8; i++) {
			inv[i] = new InventorySlot(inventory.getStack(i), i*64-256, 64);
		}
	}

	@Override
	public void render(long nvg, Window win) {
		NGraphics.drawImage(crosshair, win.getWidth() / 2 - 16, win.getHeight() / 2 - 16, 32, 32);
		NGraphics.setColor(0, 0, 0);
		if(!(Cubyz.gameUI.getMenuGUI() instanceof GeneralInventory)) {
			NGraphics.drawImage(selection, win.getWidth()/2 - 254 + Cubyz.inventorySelection*64, win.getHeight() - 62, 60, 60);
			for(int i = 0; i < 8; i++) {
				inv[i].reference = Cubyz.world.getLocalPlayer().getInventory().getStack(i); // without it, if moved in inventory, stack won't refresh
				inv[i].render(nvg, win);
			}
		}
		// Draw the health bar:#
		float maxHealth = Cubyz.world.getLocalPlayer().maxHealth;
		float health = Cubyz.world.getLocalPlayer().health;
		if (lastPlayerHealth != health) {
			if (lastPlayerHealth > health) {
				lastPlayerHurtMs = System.currentTimeMillis();
			}
			lastPlayerHealth = health;
		}
		if (System.currentTimeMillis() < lastPlayerHurtMs+510) {
			NGraphics.setColor(255, 50, 50, (int) (255-(System.currentTimeMillis()-lastPlayerHurtMs))/2);
			NGraphics.fillRect(0, 0, win.getWidth(), win.getHeight());
		}
		String s = health + "/" + maxHealth + " HP";
		float width = NGraphics.getTextWidth(s);
		NGraphics.drawImage(healthBar[7], (int)(win.getWidth() - maxHealth*12 - 40 - width), 6, 24, 24);
		NGraphics.drawText(win.getWidth()-maxHealth*12 - 10 - width, 9, s);
		for(int i = 0; i < maxHealth; i += 2) {
			boolean half = i+1 == health;
			boolean empty = i >= health;
			
			int idx = 0;
			if (i == 0) { // beggining
				idx = empty ? 0 : 1;
			} else if (i == maxHealth-2) { // end
				idx = i+1 >= health ? 2 : 3;
			} else {
				idx = empty ? 4 : (half ? 5 : 6); // if empty = 4, half = 5, full = 6
			}
			NGraphics.drawImage(healthBar[idx], (int)(i*12 + win.getWidth() - maxHealth*12 - 4), 6, 24, 24);
		}
	}

	@Override
	public boolean doesPauseGame() {
		return false;
	}

}
