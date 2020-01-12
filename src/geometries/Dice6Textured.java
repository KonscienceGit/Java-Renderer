package geometries;

import java.nio.IntBuffer;

import com.jogamp.opengl.GL4ES3;

public class Dice6Textured extends Geometry {
	public Dice6Textured() {
		drawMethod = GL4ES3.GL_TRIANGLES;
		valueType = GL4ES3.GL_UNSIGNED_INT;
		textured = true;
		triDimensional = true;
		rotate3D = true;
		texturePath = "images/diceTexture.jpg";
		
		vertexShaderPath = "shaders/triDimensionalTextured.vert";
		fragmentShaderPath = "shaders/textureColored.frag";
		byteStride = 5*Float.BYTES;
		byteOffset = 3*Float.BYTES;
		elemIndexLength= 36;
		
		//-------------------------------------
		//Square
	    vertexData = new float[] {
    		//Face 1 (BAS)
            -1, -1, -1, 0.00f, 0.00f, 
            +1, -1, -1, 0.00f, 0.3232f, 
            +1, -1, +1, 0.49f, 0.3232f,
            -1, -1, +1, 0.49f, 0.00f,
    		//Face 2 (SUD)
            -1, -1, -1, 0.49f, 0.00f, 
            +1, -1, -1, 0.49f, 0.3232f, 
            +1, +1, -1, 1.00f, 0.3232f,
            -1, +1, -1, 1.00f, 0.00f,
    		//Face 3 (EST)
            +1, -1, -1, 0.00f, 0.3232f, 
            +1, +1, -1, 0.00f, 0.66f, 
            +1, +1, +1, 0.49f, 0.66f,
            +1, -1, +1, 0.49f, 0.3232f,
    		//Face 4 (NORD)
            -1, -1, +1, 0.49f, 0.3232f, 
            +1, -1, +1, 0.49f, 0.66f, 
            +1, +1, +1, 1.00f, 0.66f,
            -1, +1, +1, 1.00f, 0.3232f,
    		//Face 5 (WEST)
            -1, -1, -1, 0.00f, 0.66f, 
            -1, +1, -1, 0.00f, 1.00f, 
            -1, +1, +1, 0.49f, 1.00f,
            -1, -1, +1, 0.49f, 0.66f,
    		//Face 6 (HAUT)
            -1, +1, -1, 0.49f, 0.66f, 
            +1, +1, -1, 0.49f, 1.00f, 
            +1, +1, +1, 1.00f, 1.00f,
            -1, +1, +1, 1.00f, 0.66f
		    };
	    elementData = new int[] {
    		//Face 1
    		0,2,1,
    		0,3,2,
    		//Face 2
    		4,5,6,
    		4,6,7,
    		//Face 3
    		8,10,9,
    		8,11,10,
    		//Face 4
    		12,14,13,
    		12,15,14,
    		//Face 5
    		16,17,18,
    		16,18,19,
    		//Face 6
    		20,21,22,
    		20,22,23
		};
	}
	public IntBuffer getElementBuffer() {
		elemIndexLength = elementData.length;
		elementBuffer = IntBuffer.wrap(elementData);
		return elementBuffer;
	}
}
