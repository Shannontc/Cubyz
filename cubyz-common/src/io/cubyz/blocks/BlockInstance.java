package io.cubyz.blocks;

import org.joml.Vector3i;

import io.cubyz.Settings;
import io.cubyz.entity.Player;
import io.cubyz.world.Surface;

public class BlockInstance {

	private Block block;
	private Object[] spatial;
	private final Vector3i position;
	private Surface surface;
	public boolean neighborUp, neighborDown, neighborEast, neighborWest, neighborNorth, neighborSouth;
	private byte blockData;
	public final int[] light;
	
	public BlockInstance(Block block, byte data, Vector3i position, Player player, int worldSize) {
		this.block = block;
		this.position = position;
		blockData = data;
		if(Settings.easyLighting)
			light = new int[8];
		else
			light = null;
		if(block.mode != null) {
			spatial = block.mode.generateSpatials(this, blockData, player, worldSize);
		}
	}
	
	public Surface getStellarTorus() {
		return surface;
	}
	
	public void setStellarTorus(Surface world) {
		this.surface = world;
	}
	
	public int getID() {
		return block.ID;
	}
	
	public Vector3i getPosition() {
		return position;
	}
	
	public int getX() {
		return position.x;
	}
	
	public int getY() {
		return position.y;
	}
	
	public int getZ() {
		return position.z;
	}
	
	public Block getBlock() {
		return block;
	}
	
	public void setBlock(Block b) {
		block = b;
	}
	
	public byte getData() {
		return blockData;
	}
	
	public void setData(byte data, Player player, int worldSize) {
		blockData = data;
		spatial = block.mode.generateSpatials(this, blockData, player, worldSize);
	}
	
	public Object[] getSpatials() {
		return spatial;
	}

	float breakAnim = 0f;
	public void setBreakingAnimation(float f) { // 0 <= f < 1
		breakAnim = f;
	}
	
	public float getBreakingAnim() {
		return breakAnim;
	}
	
}
