package io.cubyz.world.cubyzgenerators;

import java.util.Random;

import io.cubyz.api.CubyzRegistries;
import io.cubyz.api.Resource;
import io.cubyz.blocks.Block;
import io.cubyz.blocks.Block.BlockClass;
import io.cubyz.math.CubyzMath;
import io.cubyz.world.cubyzgenerators.biomes.Biome;

public class CrystalCavernGenerator implements FancyGenerator {
	
	private static Block glowCrystal, crystalOre;
	
	public static void init(Block crystal, Block ore) {
		glowCrystal = crystal;
		crystalOre = ore;
	}
	
	
	@Override
	public Resource getRegistryID() {
		return new Resource("cubyz", "lifeland_crystal_cavern");
	}
	
	@Override
	public int getPriority() {
		return 65537; // Directly after normal caves.
	}
	
	private static final int range = 32;
	private Block water = CubyzRegistries.BLOCK_REGISTRY.getByID("cubyz:water");
	private Block ice = CubyzRegistries.BLOCK_REGISTRY.getByID("cubyz:ice");
	private Block stone = CubyzRegistries.BLOCK_REGISTRY.getByID("cubyz:stone");

	@Override
	public void generate(long seed, int cx, int cz, Block[][][] chunk, boolean[][] vegetationIgnoreMap, float[][] heatMap, int[][] heightMap, Biome[][] biomeMap, int worldSize) {
		Random rand = new Random(seed);
		int rand1 = rand.nextInt() | 1;
		int rand2 = rand.nextInt() | 1;
		int chunksSize = worldSize >> 4;
		// Generate caves from all nearby chunks:
		for(int x = cx - range; x <= cx + range; ++x) {
			for(int z = cz - range; z <= cz + range; ++z) {
				int randX = CubyzMath.worldModulo(x, chunksSize)*rand1;
				int randZ = CubyzMath.worldModulo(z, chunksSize)*rand2;
				rand.setSeed((randX << 32) ^ randZ ^ seed);
				considerCoordinates(x, z, cx, cz, chunk, vegetationIgnoreMap, heightMap, rand);
			}
		}
	}
	
	private void generateCave(long random, int cx, int cz, Block[][][] chunk, double worldX, double worldY, double worldZ, float size, float direction, float slope, int curStep, double caveHeightModifier, boolean[][] vegetationIgnoreMap, int[][] heightMap, int[][] crystalSpawns, int[] index) {
		double cwx = (double) (cx*16 + 8);
		double cwz = (double) (cz*16 + 8);
		float directionModifier = 0.0F;
		float slopeModifier = 0.0F;
		Random localRand = new Random(random);
		// Choose a random cave length if not specified:
		int local = range*16 - 16;
		int caveLength = local - localRand.nextInt(local / 4);

		for(boolean highSlope = localRand.nextInt(6) == 0; curStep < caveLength; ++curStep) {
			double xzScale = 6.0 + Math.sin(curStep*Math.PI/caveLength)*size;
			double yScale = xzScale*caveHeightModifier;
			// Move cave center point one unit into a direction given by slope and direction:
			float xzUnit = (float)Math.cos(slope);
			float yUnit = (float)Math.sin(slope);
			double delX = Math.cos(direction) * xzUnit;
			double delZ = Math.sin(direction) * xzUnit;
			worldX += delX;
			worldY += yUnit;
			worldZ += Math.sin(direction)*xzUnit;

			if(highSlope) {
				slope *= 0.92F;
			} else {
				slope *= 0.7F;
			}

			slope += slopeModifier * 0.01F;
			direction += directionModifier * 0.01F;
			slopeModifier *= 0.9F;
			directionModifier *= 0.75F;
			slopeModifier += (localRand.nextFloat() - localRand.nextFloat())*localRand.nextFloat()*2;
			directionModifier += (localRand.nextFloat() - localRand.nextFloat())*localRand.nextFloat()*4;
			double deltaX = worldX - cwx;
			double deltaZ = worldZ - cwz;
			double stepsLeft = (double)(caveLength - curStep);
			double maxLength = (double)(size + 8);
			// Abort if the cave is getting to far away from this chunk:
			if(deltaX*deltaX + deltaZ*deltaZ - stepsLeft*stepsLeft > maxLength*maxLength) {
				return;
			}
			
			// Only care about it if it is inside the current chunk:
			if(worldX >= cwx - 8 - xzScale && worldZ >= cwz - 8 - xzScale && worldX <= cwx + 8 + xzScale && worldZ <= cwz + 8 + xzScale) {
				// Determine min and max of the current cave segment in all directions.
				int xMin = (int)(worldX - xzScale) - cx*16 - 1;
				int xMax = (int)(worldX + xzScale) - cx*16 + 1;
				int yMin = (int)(worldY - yScale) - 1;
				int yMax = (int)(worldY + yScale) + 1;
				int zMin = (int)(worldZ - xzScale) - cz*16 - 1;
				int zMax = (int)(worldZ + xzScale) - cz*16 + 1;
				if (xMin < 0)
					xMin = 0;
				if (xMax > 16)
					xMax = 16;
				if (yMin < 1)
					yMin = 1; // Don't make caves expand to the bedrock layer.
				if (yMax > 248)
					yMax = 248;
				if (zMin < 0)
					zMin = 0;
				if (zMax > 16)
					zMax = 16;
				
				// Go through all blocks within range of the cave center and remove them if they
				// are within range of the center.
				for(int curX = xMin; curX < xMax; ++curX) {
					double distToCenterX = ((double) (curX + cx*16) + 0.5 - worldX) / xzScale;
					
					for(int curZ = zMin; curZ < zMax; ++curZ) {
						double distToCenterZ = ((double) (curZ + cz*16) + 0.5 - worldZ) / xzScale;
						int curYIndex = yMax;
						if(distToCenterX * distToCenterX + distToCenterZ * distToCenterZ < 1.0) {
							for(int curY = yMax - 1; curY >= yMin; --curY) {
								double distToCenterY = ((double) curY + 0.5 - worldY) / (yScale);
								if(distToCenterX*distToCenterX + distToCenterY*distToCenterY + distToCenterZ*distToCenterZ < 1.0 && !water.equals(chunk[curX][curZ][curYIndex]) && !ice.equals(chunk[curX][curZ][curYIndex])) {
									chunk[curX][curZ][curYIndex] = null;
									if(heightMap[curX][curZ] == curYIndex)
										vegetationIgnoreMap[curX][curZ] = true;
								}
								--curYIndex;
							}
						}
					}
				}
			}
			long seed = localRand.nextLong();
			// Only let crystals spawn when they are close enough to the chunk.
			if(worldX >= cwx - 40 && worldZ >= cwz - 40 && worldX <= cwx + 40 && worldZ <= cwz + 40) {
				// Consider a good amount of crystal spawns in the region.
				Random rand = new Random(seed);
				int amount = (int)(1+20*xzScale*yScale/size/size);
				for(int i = 0; i < amount; i++) {
					// Choose a random point on the surface of the surrounding spheroid to generate a crystal there:
					double theta = 2*Math.PI*rand.nextDouble();
			        double phi = Math.acos(1 - 2*rand.nextDouble());
			        double x = Math.sin(phi)*Math.cos(theta);
			        double y = Math.sin(phi)*Math.sin(theta);
			        double z = Math.cos(phi);
			        // Check if the crystal touches the wall:
			        if(Math.abs(delX*x+yUnit*y+delZ*z) < 0.05) {
				        crystalSpawns[index[0]++] = new int[] {(int)(worldX + x*xzScale), (int)(worldY + y*yScale), (int)(worldZ + z*xzScale)};
			        }
				}
			}
		}
	}
	
	private double distSqr(double x, double y, double z) {
		return x*x+y*y+z*z;
	}
	
	private void considerCrystal(int wx, int wz, int[] xyz, Block[][][] chunk, long seed, boolean useNeedles) {
		if(xyz[0] >= wx-32 && xyz[0] <= wx+48 && xyz[2] >= wz-32 && xyz[2] <= wz+48) {
			int x = xyz[0]-wx;
			int y = xyz[1];
			int z = xyz[2]-wz;
			Random rand = new Random(seed);
			// Make some crystal spikes in random directions:
			int spikes = 4;
			if(useNeedles) spikes++;
			spikes += rand.nextInt(spikes); // Use somewhat between spikes and 2*spikes spikes.
			for(int i = 0; i < spikes; i++) {
				int length = rand.nextInt(24)+8;
				// Choose a random direction:
				double theta = 2*Math.PI*rand.nextDouble();
		        double phi = Math.acos(1 - 2*rand.nextDouble());
		        double delX = Math.sin(phi)*Math.cos(theta);
		        double delY = Math.sin(phi)*Math.sin(theta);
		        double delZ = Math.cos(phi);
		        for(double j = 0; j < length;) {
		        	double x2 = x + delX*j;
		        	double y2 = y + delY*j;
		        	double z2 = z + delZ*j;
		        	double size;
		        	if(useNeedles)
		        		size = 0.7;
		        	else
		        		size = 12*(length-j)/length/spikes;
		        	int xMin = (int)(x2-size);
		        	int xMax = (int)(x2+size);
		        	int yMin = (int)(y2-size);
		        	int yMax = (int)(y2+size);
		        	int zMin = (int)(z2-size);
		        	int zMax = (int)(z2+size);
		        	for(int x3 = xMin; x3 <= xMax; x3++) {
			        	for(int y3 = yMin; y3 <= yMax; y3++) {
				        	for(int z3 = zMin; z3 <= zMax; z3++) {
				        		double dist = distSqr(x3-x2, y3-y2, z3-z2);
				        		if(dist <= size*size) {
						        	if(x3 >= 0 && x3 < 16 && y3 >= 0 && y3 < 256 && z3 >= 0 && z3 < 16) {
						        		if(chunk[(int)x3][(int)z3][(int)y3] == null || chunk[(int)x3][(int)z3][(int)y3].isDegradable() || chunk[(int)x3][(int)z3][(int)y3].getBlockClass() == BlockClass.FLUID) {
						        			chunk[(int)x3][(int)z3][(int)y3] = glowCrystal;
						        		} else if(chunk[(int)x3][(int)z3][(int)y3] == stone) {
						        			chunk[(int)x3][(int)z3][(int)y3] = crystalOre; // When the crystal goes through stone, generate the corresponding ore at that position.
						        		}
						        	}
				        		}
				        	}
			        	}
		        	}
		        	if(size > 2) size = 2;
		        	j += size/2; // Make sure there are no crystal bits floating in the air.
		        	if(size < 0.5) break; // Also preventing floating crystal bits.
		        }
			}
		}
	}

	private void considerCoordinates(int x, int z, int cx, int cz, Block[][][] chunk, boolean[][] vegetationIgnoreMap, int[][] heightMap, Random rand) {
		if(rand.nextInt(2048) != 0) return; // This should be pretty rare(mostly because it is so huge).
		// Choose some in world coordinates to start generating:
		double worldX = (double)((x << 4) + rand.nextInt(16));
		double worldZ = (double)((z << 4) + rand.nextInt(16));
		float direction = rand.nextFloat()*(float)Math.PI*2.0F;
		float slope = (rand.nextFloat() - 0.5F)/4.0F;
		float size = rand.nextFloat()*20 + 20;
		double worldY = size + rand.nextFloat()*10;
		int[][] crystalSpawns = new int[1024][3]; // TODO: Properly evaluate the maximum number of crystal spawns per crystal cavern.
		int[] index = {0};
		long rand1 = rand.nextLong();
		long rand2 = rand.nextLong();
		long rand3 = rand.nextLong();
		boolean useNeedles = rand.nextBoolean(); // Different crystal type.
		generateCave(rand.nextLong(), cx, cz, chunk, worldX, worldY, worldZ, size, direction, slope, 0, 0.75, vegetationIgnoreMap, heightMap, crystalSpawns, index);

		// Generate the crystals:
		for(int i = 0; i < index[0]; i++) {
			considerCrystal(cx << 4, cz << 4, crystalSpawns[i], chunk, crystalSpawns[i][0]*rand1 + crystalSpawns[i][1]*rand2 + crystalSpawns[i][2]*rand3, useNeedles);
		}
	}
}
