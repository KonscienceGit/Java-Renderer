package geometries;

import java.nio.IntBuffer;

import com.jogamp.opengl.GL4ES3;

public class Carre2DTexturedCheckered extends Geometry {
	public Carre2DTexturedCheckered() {
		drawMethod = GL4ES3.GL_TRIANGLES;
		valueType = GL4ES3.GL_UNSIGNED_INT;
		textured = true;
		triDimensional = false;
		rotate2D = false;
		repeatTexture = true;
		texturePath = "images/checkered.jpg";
		vertexShaderPath = "shaders/biDimensionalTextured.vert";
		fragmentShaderPath = "shaders/textureColored.frag";
		byteStride = 4*Float.BYTES;
		byteOffset = 2*Float.BYTES;
		elemIndexLength = 6;
		
		//-------------------------------------
		//Square
	    vertexData = new float[] {
            -1.0f, -1.0f, 0.0f, 0.0f, 
            -1.0f, +1.0f, 0.0f, 25.0f, 
            +1.0f, +1.0f, 25.0f, 25.0f,
            +1.0f, -1.0f, 25.0f, 0.0f
		    };
	    elementData = new int[] {
    		0,2,1,
    		0,3,2
		};
	}
	public IntBuffer getElementBuffer() {
		elemIndexLength = elementData.length;
		elementBuffer = IntBuffer.wrap(elementData);
		return elementBuffer;
	}
}
