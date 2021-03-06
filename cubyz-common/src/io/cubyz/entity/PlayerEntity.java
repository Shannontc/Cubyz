
package io.cubyz.entity;

import org.joml.Vector3f;

import io.cubyz.ClientOnly;
import io.cubyz.api.Resource;
import io.cubyz.blocks.Block.BlockClass;
import io.cubyz.blocks.BlockInstance;
import io.cubyz.items.Inventory;
import io.cubyz.items.tools.Tool;
import io.cubyz.math.CubyzMath;
import io.cubyz.ndt.NDTContainer;
import io.cubyz.world.Surface;
import io.cubyz.world.World;

public class PlayerEntity extends EntityType {

	public PlayerEntity() {
		super(new Resource("cubyz:player"));
	}

	public static class PlayerImpl extends Player {

		private boolean flying = false;
		private Inventory inv = new Inventory(37); // 4*8 normal inventory + 4 crafting slots + 1 crafting result slot.
		private BlockInstance toBreak = null;
		private long timeStarted = 0;
		private int maxTime = -1;
		private int breakingSlot = -1; // Slot used to break the block. Slot change results in restart of block breaking.

		public PlayerImpl(Surface surface) {
			super(surface);
		}
		
		@Override
		public boolean isFlying() {
			return flying;
		}
		
		@Override
		public void setFlying(boolean fly) {
			flying = fly;
		}
		
		public long getRemainingBreakTime() {
			return (maxTime+timeStarted) - System.currentTimeMillis();
		}
		
		@Override
		public void move(Vector3f inc, Vector3f rot, int worldSize) {
			float deltaX = 0;
			float deltaZ = 0;
			if (inc.z != 0) {
				deltaX += (float) Math.sin(rot.y) * -1.0F * inc.z;
				deltaZ += (float) Math.cos(rot.y) * inc.z;
			}
			if (inc.x != 0) {
				deltaX += (float) Math.sin(rot.y - Math.PI/2) * -1.0F * inc.x;
				deltaZ += (float) Math.cos(rot.y - Math.PI/2) * inc.x;
			}
			if (inc.y != 0) {
				vy = inc.y;
			}
			if(!flying) { // Allows the player to move through blocks when flying.
				if(deltaX != 0)
					deltaX = _getX(deltaX);
				if(deltaZ != 0)
					deltaZ = _getZ(deltaZ);
			}
			position.add(deltaX, 0, deltaZ);
			float newX = CubyzMath.worldModulo(position.x, worldSize);
			float newZ = CubyzMath.worldModulo(position.z, worldSize);
			boolean crossedBorder = newX != position.x || newZ != position.z;
			position.x = newX;
			position.z = newZ;
			if(crossedBorder) {
				ClientOnly.onBorderCrossing.accept(this);
			}
		}
		
		@Override
		public void update() {
			if (health < 0)
				health = 0;
			if (health > maxHealth)
				health = maxHealth;
			if (!flying) {
				vy -= surface.getStellarTorus().getGravity();
				vx = 0;
				vz = 0;
				updatePosition();
			} else {
				position.y += vy;
				vy = 0;
			}
		}

		@Override
		public Inventory getInventory() {
			return inv;
		}

		@Override
		public void feedback(String feedback) {}
		
		@Override
		public void loadFrom(NDTContainer ndt) {
			super.loadFrom(ndt);
			if (ndt.hasKey("inventory")) {
				inv.loadFrom(ndt.getContainer("inventory"), surface.getCurrentRegistries());
			}
		}
		
		@Override
		public NDTContainer saveTo(NDTContainer ndt) {
			ndt = super.saveTo(ndt);
			ndt.setContainer("inventory", inv.saveTo(new NDTContainer()));
			return ndt;
		}
		
		private void calculateBreakTime(BlockInstance bi, int slot) {
			if(bi == null || bi.getBlock().getBlockClass() == BlockClass.UNBREAKABLE) {
				return;
			}
			timeStarted = System.currentTimeMillis();
			maxTime = (int)(Math.round(bi.getBlock().getHardness()*200));
			if(inv.getItem(slot) instanceof Tool) {
				Tool tool = (Tool)inv.getItem(slot);
				if(tool.canBreak(bi.getBlock())) {
					maxTime = (int)(maxTime/tool.getSpeed());
				}
			}
		}

		@Override
		public void breaking(BlockInstance bi, int slot, Surface w) {
			if(bi != toBreak || breakingSlot != slot) {
				resetBlockBreaking(); // Make sure block breaking animation is reset.
				toBreak = bi;
				breakingSlot = slot;
				calculateBreakTime(bi, slot);
			}
			if(bi == null || bi.getBlock().getBlockClass() == BlockClass.UNBREAKABLE)
				return;
			long deltaTime = System.currentTimeMillis() - timeStarted;
			bi.setBreakingAnimation((float) deltaTime / (float) maxTime);
			if (deltaTime > maxTime) {
				if(Tool.class.isInstance(inv.getItem(slot))) {
					if(((Tool)inv.getItem(slot)).used()) {
						inv.getStack(slot).clear();
					}
				}
				w.removeBlock(bi.getX(), bi.getY(), bi.getZ());
				if(inv.addItem(bi.getBlock().getBlockDrop(), 1) != 0) {
					//DropItemOnTheGround(); //TODO: Add this function.
				}
			}
		}

		@Override
		public void resetBlockBreaking() {
			if(toBreak != null) {
				toBreak.setBreakingAnimation(0);
				toBreak = null;
			}
		}

		@Override
		public World getWorld() {
			return surface.getStellarTorus().getWorld();
		}
	}

	@Override
	public Entity newEntity(Surface surface) {
		return new PlayerImpl(surface);
	}
	
}
