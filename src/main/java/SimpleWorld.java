import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import org.joml.Matrix4f;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class SimpleWorld {

    public static final int SCREEN_WIDTH = 600;
    public static final int SCREEN_HEIGHT = 600;
    public static float CAMERA_RADIUS = 3.0f;

    // The window handle
    private long window;
    boolean pressed;

    private int VBO;
    private Shader shader;

    public void run() {
        init();
        loop();

        glDeleteBuffers(VBO);
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

/*        glEnable(GL_CULL_FACE);
	    glCullFace(GL_BACK);
	    glFrontFace(GL_CW);*/

        glEnable(GL_DEPTH_TEST);



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

        final Matrix4f camera = new Matrix4f();
        final Vector3f cameraPos = new Vector3f(0.0f, 2.0f, 3.0f);
        final Vector3f cameraFront = new Vector3f(0.0f, 0.5f, 0.0f);
        final Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);

        // set up vertex data (and buffer(s)) and configure vertex attributes
        // ------------------------------------------------------------------
        float vertices[] = {
                -0.5f,  0.5f, 0.0f, 0.0f, 0.0f,0.0f,
                0.5f,  -0.5f, 0.0f, 0.0f, 0.0f,0.0f,
                -0.5f,  -0.5f, 0.0f, 0.0f, 0.0f,0.0f,

                -0.5f,  0.5f,0.0f, 0.0f, 0.0f,0.0f,
                0.5f,  0.5f,0.0f, 0.0f, 0.0f,0.0f,
                0.5f,  -0.5f,0.0f, 0.0f, 0.0f,0.0f
        };


        // rendering the top of the cylinder

//        float[] vertices = new float[600];
        float[] indices = new float[600];


        Matrix4f model = new Matrix4f();
        Matrix4f projection = new Matrix4f();

        int VAO = glGenVertexArrays();
        glBindVertexArray(VAO);

        VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        int INB = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, INB);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

//        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        //position
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 4 * 6, 0);
        glEnableVertexAttribArray(0);

        //normal
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 4 * 6, 3 * 4);
        glEnableVertexAttribArray(1);

        // glBindBuffer(GL_ARRAY_BUFFER, 0);
        shader.use();

        GLFW.glfwSetScrollCallback(window, new GLFWScrollCallback() {
            @Override public void invoke (long win, double dx, double dy) {
                CAMERA_RADIUS += dy * 0.5f;
            }
        });

        double angleX = Math.toRadians(45), angleY = Math.toRadians(60);
        final double[] xPos = new double[1];
        final double[] yPos = new double[1];
        final Vector2f lastMousePos = new Vector2f();

        GLFW.glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                pressed = action == 1;
                glfwGetCursorPos(window, xPos, yPos);
                lastMousePos.x = (float) xPos[0];
                lastMousePos.y = (float) yPos[0];
            }
        });

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.

        while (!glfwWindowShouldClose(window)) {
            // set the view matrix
            projection.identity();
            projection.perspective((float) Math.toRadians(45.0f), SCREEN_WIDTH / SCREEN_HEIGHT, 0.1f, 100.0f);

            float cameraSpeed = 0.05f;

            if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS)
                cameraPos.add(cameraFront.mul(cameraSpeed));
            if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS)
                cameraPos.sub(cameraFront.mul(cameraSpeed));
            if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS)
                cameraPos.add(cameraFront.cross(cameraUp).mul(cameraSpeed));
            if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS)
                cameraPos.sub(cameraFront.cross(cameraUp).mul(cameraSpeed));

            if (pressed) {
                glfwGetCursorPos(window, xPos, yPos);
                double x = lastMousePos.x - xPos[0];
                double y = lastMousePos.y - yPos[0];
                double changedX = x / SCREEN_WIDTH * 2 * Math.PI;
                double changedY = y / SCREEN_HEIGHT * 2 * Math.PI;
                lastMousePos.x = (float) xPos[0];
                lastMousePos.y = (float) yPos[0];

                angleX += changedX;
                angleY += changedY;
            }

            // update camera
            cameraFront.set(
                    (float) (CAMERA_RADIUS * Math.sin(angleY) * Math.cos(angleX)),
                    (float) (CAMERA_RADIUS * Math.cos(angleY)),
                    (float) (CAMERA_RADIUS * Math.sin(angleY) * Math.sin(angleX))
            );

            camera.identity();
            camera.lookAt(cameraPos, cameraFront, cameraUp);

            // Set the clear color
            glClearColor(.2f, .4f, .7f, .4f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            model.identity();

            shader.use();

            shader.setMatrix("model", model);
            shader.setMatrix("view", camera);
            shader.setMatrix("projection", projection);


            shader.setVec3("objectColor", 1.0f, 0.5f, 0.31f);
            shader.setVec3("lightColor", 1.0f, 1.0f, 1.0f);
            shader.setVec3("lightPos", (float) (Math.sin(glfwGetTime()) * 2.0f), 2.0f, (float) (Math.cos(glfwGetTime()) * 2.0f));
            shader.setVec3("viewPos", 2.0f, 2.0f, 2.0f);


            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new SimpleWorld().run();
    }






}
