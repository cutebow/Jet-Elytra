package me.cutebow.jetely.model;

public final class ObjMesh {
    public final float[] positions;
    public final float[] uvs;
    public final int[] indices;

    public ObjMesh(float[] positions, float[] uvs, int[] indices) {
        this.positions = positions;
        this.uvs = uvs;
        this.indices = indices;
    }

    public boolean isEmpty() {
        return positions.length == 0 || indices.length == 0;
    }
}
