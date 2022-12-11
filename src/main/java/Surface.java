public class Surface {

    private float[] vertices;
    private int[] indices;
    private float[] normals;
    private float[] textureCoords;

    private int size;
    private int count;

    public Surface(int hVertices, int vVertices, float size){
        generateTerrain();

        count = indices.length;
    }

    public int[] getIndices() {
        return indices;
    }

    public float[] getVertices() {
        return vertices;
    }

    public int getSize() {
        return size;
    }

    public float[] getNormals() {
        return normals;
    }

    public float[] getTextureCoords() {
        return textureCoords;
    }
    //    private float[] getTriangleStripVertices(int hVertices, int vVertices, float size) {
//
//
//        float xOffset = size/(float) hVertices;
//        float zOffset = size/ (float) vVertices;
//
//        float[] vertices = new float[8 * hVertices * vVertices];
//
//        for (int i = 0; i < hVertices; i++) {
//            for (int j = 0; j < vVertices; j++) {
//                int index = (i * hVertices + j) * 8;
//
//                //position
//                vertices[index] = xOffset * j;
//                vertices[index + 1] = 0;
//                vertices[index + 2] = zOffset * i;
//
//                vertices[index + 3] = 0;
//                vertices[index + 4] = 1;
//
//                //normal
//                vertices[index + 5] = 0;
//                vertices[index + 6] = 1;
//                vertices[index + 7] = 0;
//            }
//        }
//
//
//        return vertices;
//    }
//
//    private int[] getStripIndices(int hVertices, int vVertices){
//
//        //0, i, 1, i+1, ..., i-1, 2i-1, 2i-1, i, i, 2i, i+1, 2i+1, ...
//
//        int degenNumber = 2 * (vVertices-2);
//
//        int[] indices = new int[2 * vVertices * (hVertices - 1) + degenNumber];
//
//        int index = 0;
//
//        for (int i = 0; i < hVertices; i++) {
//            if(i>=1)
//                indices[index++] = i * vVertices;
//
//            for (int j = 0; j < vVertices; j++) {
//                indices[index++] = j * vVertices + i;
//                indices[index++] = (j+1) * vVertices + i;
//            }
//
//            if(i < hVertices - 2)
//                indices[index++] = (i+1) * vVertices + vVertices -1;
//        }
//
//        return indices;
//    }

//    private float[] generateVertices(int verticalVerticesCount, int horizontalVerticesCount) {
//        final int floatsPerVertex = 5;
//        final float[] vertices = new float[verticalVerticesCount * horizontalVerticesCount * floatsPerVertex];
//        int offset = 0;
//
//        for (int y = 0; y < horizontalVerticesCount; y++) {
//            for (int x = 0; x < verticalVerticesCount; x++) {
//                final float xRatio = x / (float) (verticalVerticesCount - 1);
//                final float zRatio = 1f - (y / (float) (horizontalVerticesCount - 1));
//
//                final float xPosition = xRatio * size - size * 0.5f;
//                final float zPosition = zRatio * size - size * 0.5f;
//
//                // position
//                vertices[offset++] = xPosition;
//                vertices[offset++] = 0;
//                vertices[offset++] = zPosition;
//
//                // texture
//                vertices[offset++] = xRatio;
//                vertices[offset++] = zRatio;
//            }
//        }
//        return vertices;
//    }
//
//
//    private int[] generateIndices(int verticalVerticesCount, int horizontalVerticesCount) {
//        final int numStripsRequired = horizontalVerticesCount - 1;
//        final int numDegensRequired = 2 * (numStripsRequired - 1);
//        final int verticesPerStrip = 2 * verticalVerticesCount;
//
//        final int[] indices = new int[(verticesPerStrip * numStripsRequired) + numDegensRequired];
//        int offset = 0;
//
//        for (int y = 0; y < horizontalVerticesCount - 1; y++) {
//            if (y > 0) {
//                // degenerate begin: repeat first vertex
//                indices[offset++] = y * verticalVerticesCount;
//            }
//
//            for (int x = 0; x < verticalVerticesCount; x++) {
//                // one part of the strip
//                indices[offset++] = y * verticalVerticesCount + x;
//                indices[offset++] = (y + 1) * verticalVerticesCount + x;
//            }
//
//            if (y < horizontalVerticesCount - 2) {
//                // degenerate end: repeat last vertex
//                indices[offset++] = (y + 1) * verticalVerticesCount + verticalVerticesCount - 1;
//            }
//        }
//        return indices;
//    }
    private static final float SIZE = 4;
    private static final int DIVISIONS = 128;

    private void generateTerrain(){
        int vertices_size = (DIVISIONS + 1) * (DIVISIONS + 1);
        int indices_size = DIVISIONS * (DIVISIONS + 1) * 2 + 2 * (DIVISIONS - 1);
        vertices = new float[vertices_size * 3];
        normals = new float[vertices_size * 3];
        textureCoords = new float[vertices_size * 2];
        indices = new int[indices_size];
        int vertexPointer = 0;
        for (int i = 0; i <= DIVISIONS; i++) { // j = s
            for (int j = 0; j  <= DIVISIONS; j++) { // i = n
                float x, y, z;
                x = (float) j / ((float) DIVISIONS - 1) * SIZE;
                y = 0;
                z = (float) i / ((float) DIVISIONS - 1) * SIZE;

                float n_x, n_y, n_z;
                n_x = 0;
                n_y = 1;
                n_z = 0;

                float u, v;
                u = (float) j / ((float) DIVISIONS - 1);
                v = (float) i / ((float) DIVISIONS - 1);

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
        for (int i = 0; i < DIVISIONS; i++) {
            for (int j = 0; j <= DIVISIONS; j++) {
                // quad
                indices[pointer]         = i * (DIVISIONS + 1) + j;
                indices[pointer + 1]     = (i + 1) * (DIVISIONS + 1) + j;
                pointer += 2;
            }
            if (i + 1 < DIVISIONS) {
                // add indices for degenerate triangles
                indices[pointer] = (i + 1) * (DIVISIONS + 1) + (DIVISIONS - 1) + 1;
                indices[pointer + 1] = (i + 1) * (DIVISIONS + 1);
                pointer += 2;
            }
        }
    }
}