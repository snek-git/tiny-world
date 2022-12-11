import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import org.joml.Matrix4f;

import java.nio.*;
import java.util.Vector;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class SimpleCylinder {

    public static final int SCREEN_WIDTH = 600;
    public static final int SCREEN_HEIGHT = 600;
    public static float CAMERA_RADIUS = 3.0f;

    // The window handle
    private long window;
    boolean pressed;


    private int VBO_pos;
    private int VBO_norm;
    private int VBO_tex;

    private Shader shader;
    private Shader waterShader;

    private float time = 0;

    public void run() {
        init();
        loop();

        shader.dispose();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Hello World!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }



    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*
            glfwGetFramebufferSize(window, pWidth, pHeight);
            glViewport(0, 0, pWidth.get(0), pHeight.get(0));
        } // the stack frame is popped automatically

        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            glViewport(0, 0, width, height);
        });

        shader = new Shader("src/shaders/vertex.shader", "src/shaders/fragment.shader");
        waterShader = new Shader("src/shaders/water_vertex.shader", "src/shaders/water_fragment.shader");

        Matrix4f view = new Matrix4f();
        final Vector3f cameraPos = new Vector3f(0.0f, 2.0f, 3.0f);
        final Vector3f cameraFront = new Vector3f(0.0f, -1.0f, 0.0f);
        final Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);

        Surface terrain = new Surface(loadTexture("grass.png"), loadTexture("grass_heightmap.png"));

        int VAO = glGenVertexArrays();
        glBindVertexArray(VAO);

        bindIndicesBuffer(terrain.getIndices());
        storeDataInAttributeList(0, 3, terrain.getVertices());
        storeDataInAttributeList(1, 2, terrain.getTextureCoords());
        storeDataInAttributeList(2, 3, terrain.getNormals());

        GL30.glBindVertexArray(0);

        Water water = new Water(loadTexture("water.png"));

        int VAO_water = glGenVertexArrays();
        glBindVertexArray(VAO_water);

        bindIndicesBuffer(water.getIndices());
        storeDataInAttributeList(0, 3, water.getVertices());
        storeDataInAttributeList(1, 2, water.getTextureCoords());
        storeDataInAttributeList(2, 3, water.getNormals());

        GL30.glBindVertexArray(0);

        Matrix4f model = new Matrix4f();
        Matrix4f projection = new Matrix4f();

        GLFW.glfwSetScrollCallback(window, new GLFWScrollCallback() {
            @Override public void invoke (long win, double dx, double dy) {
                CAMERA_RADIUS += dy * 0.5f;
            }
        });

        double yaw = Math.toRadians(-90), pitch = Math.toRadians(0);
        final double[] xPos = new double[1];
        final double[] yPos = new double[1];
        final Vector2f mousePos = new Vector2f();
        final Vector2f mouseDelta = new Vector2f();

        GLFW.glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                pressed = action == 1;
            }
        });

        glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                float xposf = (float) xpos;
                float yposf = (float) ypos;
                mouseDelta.set(xposf, yposf).sub(mousePos);
                mouseDelta.x = Math.max(-100.0f, Math.min(100.0f, mouseDelta.x));
                mouseDelta.y = Math.max(-100.0f, Math.min(100.0f, mouseDelta.y));
                mousePos.set(xposf, yposf);
            }
        });


        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.

        while (!glfwWindowShouldClose(window)) {
            time += glfwGetTime();
            // set the view matrix

//            lookAt.rotate(0.01f, new Vector3f(0, 1, 0));

            float mouseSensitivity = 0.001f;
            float cameraSpeed = 0.02f;

            if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS)
                cameraPos.add(new Vector3f(cameraFront).mul(cameraSpeed));
            if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS)
                cameraPos.sub(new Vector3f(cameraFront).mul(cameraSpeed));
            if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS)
                cameraPos.add(new Vector3f(cameraFront).cross(cameraUp).normalize().mul(cameraSpeed));
            if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS)
                cameraPos.sub(new Vector3f(cameraFront).cross(cameraUp).normalize().mul(cameraSpeed));

            if (pressed) {

                yaw += mouseDelta.x * mouseSensitivity;
                pitch += mouseDelta.y * mouseSensitivity;

                if (pitch > (float) Math.toRadians(89.0f))
                    pitch = (float) Math.toRadians(89.0f);
                if (pitch < (float) Math.toRadians(-89.0f))
                    pitch = (float) Math.toRadians(-89.0f);
            }

            // update camera
            cameraFront.set(
                    (float) ( Math.cos(pitch) * Math.cos(yaw)),
                    (float) ( Math.sin(pitch)),
                    (float) ( Math.cos(pitch) * Math.sin(yaw))
            );
            cameraFront.normalize();

            mouseDelta.zero();

            view.identity();
            view.lookAt(cameraPos, new Vector3f(cameraPos).add(cameraFront), cameraUp);
            projection.identity();
            projection.perspective((float) Math.toRadians(45.0f), SCREEN_WIDTH / SCREEN_HEIGHT, 0.1f, 100.0f);
            // Set the clear color
            glClearColor(.2f, .4f, .7f, 1f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL_FILL);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glCullFace(GL11.GL_BACK);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            model.identity();

            // render terrain
            shader.use();
            shader.setMatrix("model", model);
            shader.setMatrix("view", view);
            shader.setMatrix("projection", projection);

            shader.setVec3("lightColor", 1.0f, 1.0f, 1.0f);
            shader.setVec3("lightPos", (float) (Math.sin(glfwGetTime()) * 2.0f), 2.0f, (float) (Math.cos(glfwGetTime()) * 2.0f));
            shader.setVec3("viewPos", 2.0f, 2.0f, 2.0f);

            shader.setInt("texture1", 0);
            shader.setInt("texture2", 1);

            GL30.glBindVertexArray(VAO);
            GL20.glEnableVertexAttribArray(0);
            GL20.glEnableVertexAttribArray(1);
            GL20.glEnableVertexAttribArray(2);

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrain.getTexture());

            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrain.getHeightmap());

            glDrawElements(GL_TRIANGLE_STRIP, terrain.getIndices().length , GL_UNSIGNED_INT, 0);

            // render water
            waterShader.use();
            waterShader.setMatrix("model", model);
            waterShader.setMatrix("view", view);
            waterShader.setMatrix("projection", projection);

            waterShader.setVec3("lightColor", 1.0f, 1.0f, 1.0f);
            waterShader.setVec3("lightPos", (float) (Math.sin(glfwGetTime()) * 2.0f), 2.0f, (float) (Math.cos(glfwGetTime()) * 2.0f));
            waterShader.setVec3("viewPos", 2.0f, 2.0f, 2.0f);

            waterShader.setFloat("waterDisplacement", time);

            waterShader.setInt("texture1", 0);

            GL30.glBindVertexArray(VAO_water);
            GL20.glEnableVertexAttribArray(0);
            GL20.glEnableVertexAttribArray(1);
            GL20.glEnableVertexAttribArray(2);

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, water.getTexture());

            glDrawElements(GL_TRIANGLE_STRIP, water.getIndices().length , GL_UNSIGNED_INT, 0);

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    private void storeDataInAttributeList(int attributeNumber, int coordinateSize,float[] data) {
        int vboID = GL15.glGenBuffers();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        FloatBuffer buffer = storeDataInFloatBuffer(data);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(attributeNumber,coordinateSize,GL11.GL_FLOAT,false,0,0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private FloatBuffer storeDataInFloatBuffer(float[] data) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    private void bindIndicesBuffer(int[] indices) {
        int vboID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
        IntBuffer buffer = storeDataInIntBuffer(indices);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
    }

    private IntBuffer storeDataInIntBuffer(int[] data) {
        IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    public int loadTexture(String fileName) {
        stbi_set_flip_vertically_on_load(true);

        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture); // all upcoming GL_TEXTURE_2D operations now have effect on this texture object
        // set the texture wrapping parameters
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        // set texture filtering parameters
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        // load image, create texture and generate mipmaps
        try (MemoryStack stack = stackPush()) {
            IntBuffer w    = stack.mallocInt(1);
            IntBuffer h    = stack.mallocInt(1);
            IntBuffer nrChannels = stack.mallocInt(1);

            ByteBuffer data = stbi_load("src/assets/" + fileName, w, h, nrChannels, 0);
            if (data != null) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, w.get(0), h.get(0), 0, GL_RGB, GL_UNSIGNED_BYTE, data);
                glGenerateMipmap(GL_TEXTURE_2D);
            } else {
                System.out.println("Failed to load texture " + fileName);
            }
            stbi_image_free(data);
        }

        return texture;
    }


    public static void main(String[] args) {
        new SimpleCylinder().run();
    }

}
