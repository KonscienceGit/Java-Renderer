package geometries;

import java.nio.IntBuffer;

import com.jogamp.opengl.GL4ES3;

public class Carre2DTextured extends Geometrie {
	public Carre2DTextured() {
		drawMethod = GL4ES3.GL_TRIANGLES;
		valueType = GL4ES3.GL_UNSIGNED_INT;
		textured = true;
		triDimensional = false;
		texturePath = "images/milky.jpg";
		vertexShaderPath = "shaders/biDimensionalTextured.vert";
		fragmentShaderPath = "shaders/textureColored.frag";
		byteStride = 4*Float.BYTES;
		byteOffset = 2*Float.BYTES;
		elemIndexLength = 6;
		
		//-------------------------------------
		//Square
	    vertexData = new float[] {
            -1, -1, 0, 0, 
            -1, +1, 0, 1, 
            +1, +1, 1, 1,
            +1, -1, 1, 0
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
