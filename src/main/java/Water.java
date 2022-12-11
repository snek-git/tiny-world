public class Water {

    private static final float SIZE = 4;
    private static final int PARTITIONS = 248;

    private float[] vertices;
    private int[] indices;
    private float[] normals;
    private float[] textureCoords;

    private int texture;

    public Water (int texture) {
        this.texture = texture;
        generateTerrain();
    }

    public float[] getVertices() {
        return vertices;
    }

    public float[] getTextureCoords() {
        return textureCoords;
    }

    public float[] getNormals() {
        return normals;
    }

    public int[] getIndices() {
        return indices;
    }

    private void generateTerrain(){
        int vertices_size = (PARTITIONS + 1) * (PARTITIONS + 1);
        int indices_size = PARTITIONS * (PARTITIONS + 1) * 2 + 2 * (PARTITIONS - 1);
        vertices = new float[vertices_size * 3];
        normals = new float[vertices_size * 3];
        textureCoords = new float[vertices_size * 2];
        indices = new int[indices_size];
        int vertexPointer = 0;
        for (int i = 0; i <= PARTITIONS; i++) { // j = s
            for (int j = 0; j  <= PARTITIONS; j++) { // i = n
                float x, y, z;
                x = (float) j / ((float) PARTITIONS - 1) * SIZE;
                y = 0;
                z = (float) i / ((float) PARTITIONS - 1) * SIZE;

                float n_x, n_y, n_z;
                n_x = 0;
                n_y = 1;
                n_z = 0;

                float u, v;
                u = (float) j / ((float) PARTITIONS - 1);
                v = (float) i / ((float) PARTITIONS - 1);

                vertices[vertexPointer * 3] = x;
                vertices[vertexPointer * 3 + 1] = y;
                vertices[vertexPointer * 3 + 2] = z;
                normals[vertexPointer * 3] = n_x;
                normals[vertexPointer * 3 + 1] = n_y;
                normals[vertexPointer * 3 + 2] = n_z;
                textureCoords[vertexPointer * 2] = u;
                textureCoords[vertexPointer * 2 + 1] = v;

                vertexPointer++;
            }}
        // init the indices
        int pointer = 0;
        for (int i = 0; i < PARTITIONS; i++) {
            for (int j = 0; j <= PARTITIONS; j++) {
                // quad
                indices[pointer]         = i * (PARTITIONS + 1) + j;
                indices[pointer + 1]     = (i + 1) * (PARTITIONS + 1) + j;
                pointer += 2;
            }
            if (i + 1 < PARTITIONS) {
                // add indices for degenerate triangles
                indices[pointer] = (i + 1) * (PARTITIONS + 1) + (PARTITIONS - 1) + 1;
                indices[pointer + 1] = (i + 1) * (PARTITIONS + 1);
                pointer += 2;
            }
        }
    }

    public int getTexture () {
        return texture;
    }
}