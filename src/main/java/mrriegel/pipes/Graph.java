package mrriegel.pipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

import mrriegel.pipes.block.BlockPipeBase;
import mrriegel.pipes.block.BlockPipeBase.Connect;
import mrriegel.pipes.tile.TilePipeBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

public class Graph {

	TilePipeBase tile;

	public Graph(TilePipeBase tile) {
		super();
		this.tile = tile;
	}

	public List<BlockPos> getShortestPath(BlockPos pp) {
		List<BlockPos> lis = Lists.newArrayList();
		BiMap<BlockPos, Vertex> map = getBiMap();
		if (pp.equals(tile.getPos()))
			return Lists.newArrayList(pp);
		computePaths(map.get(tile.getPos()));
		List<Vertex> path = new ArrayList<Vertex>();
		for (Vertex vertex = map.get(pp); vertex != null; vertex = vertex.previous)
			path.add(vertex);
		Collections.reverse(path);
		for (Vertex v : path)
			lis.add(map.inverse().get(v));
		return lis;
	}

	static class Vertex implements Comparable<Vertex> {
		public final String name;
		public Edge[] adjacencies;
		public int minDistance = Integer.MAX_VALUE;
		public Vertex previous;

		public Vertex(String argName) {
			name = argName;
		}

		public String toString() {
			return name;
		}

		public int compareTo(Vertex other) {
			return Integer.compare(minDistance, other.minDistance);
		}

	}

	static class Edge {
		public final Vertex target;

		public Edge(Vertex argTarget) {
			target = argTarget;
		}
	}

	public static void computePaths(Vertex source) {
		source.minDistance = 0;
		PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
		vertexQueue.add(source);

		while (!vertexQueue.isEmpty()) {
			Vertex u = vertexQueue.poll();
			for (Edge e : u.adjacencies) {
				Vertex v = e.target;
				int distanceThroughU = u.minDistance + 1;
				if (distanceThroughU < v.minDistance) {
					vertexQueue.remove(v);

					v.minDistance = distanceThroughU;
					v.previous = u;
					vertexQueue.add(v);
				}
			}
		}
	}

	BiMap<BlockPos, Vertex> getBiMap() {
		BiMap<BlockPos, Vertex> map = HashBiMap.create(tile.getPipes().size());
		for (BlockPos p : tile.getPipes())
			map.put(p, new Vertex(p.toString()));
		for (BlockPos p1 : map.keySet()) {
			List<Edge> edges = Lists.newArrayList();
			for (BlockPos p2 : getConnectedPipes(p1)) {
				edges.add(new Edge(map.get(p2)));
			}
			Edge[] ar = new Edge[edges.size()];
			for (int i = 0; i < ar.length; i++)
				ar[i] = edges.get(i);
			map.get(p1).adjacencies = ar;
		}
		return map;

	}

	protected List<BlockPos> getConnectedPipes(BlockPos p) {
		List<BlockPos> lis = Lists.newArrayList();
		if (tile.getWorld().getBlockState(p).getBlock() instanceof BlockPipeBase)
			for (EnumFacing f : EnumFacing.VALUES) {
				if (((BlockPipeBase) tile.getWorld().getBlockState(p).getBlock()).getConnect(tile.getWorld(), p, f) == Connect.PIPE)
					lis.add(p.offset(f));
			}
		return lis;
	}
}
