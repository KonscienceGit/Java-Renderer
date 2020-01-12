import renderer.Renderer;

/**
 * Main entry point of this test program.
 *
 * @author Nicolas POINSIGNON
 */
public class Main {

    public static void main(String[] args) {
        setSystemProperties();
        Renderer renderer = new Renderer();
        renderer.start();
        //renderer.dispose();
    }

    private static void setSystemProperties(){
        String osName = System.getProperty("os.name").toLowerCase();

        if(osName.contains("win")) {
            //Same as passing the JVM argument -Dsun.java2d.d3d=false
            //Is to prevent direct3D to be used (can throw Exceptions on Windows OS otherwise)
            System.getProperties().setProperty("sun.java2d.d3d", "false");
        }
    }
}
