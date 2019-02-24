package geometries;

import java.nio.IntBuffer;

import com.jogamp.opengl.GL4ES3;

public class Fleche2D extends Geometrie{
	
	public Fleche2D() {
		drawMethod = GL4ES3.GL_TRIANGLES;
		valueType = GL4ES3.GL_UNSIGNED_INT;
		textured = false;
		triDimensional = false;
		vertexShaderPath = "shaders/biDimensional.vert";
		fragmentShaderPath = "shaders/vertexColored.frag";
		byteStride = 5*Float.BYTES;
		byteOffset = 2*Float.BYTES;
		elemIndexLength = 9;
		
		//-------------------------------------
		//Arrow
	    vertexData = new float[] {
    		//Top
            +0, +1, 1, 1, 0,
            +1, +0, 1, 0, 0,
            -1, +0, 1, 0, 0,
            //superior right
            -0.5f, +0, 1, 1, 1,
            +0.5f, +0, 0, 0, 1,
            +0.5f, -1, 0, 1, 0,
            //inferior left
            -0.5f, +0, 0, 0, 0,
            +0.5f, -1, 1, 0.5f, 0.5f,
            -0.5f, -1, 0, 1, 1
	    };

	    elementData = new int[] {
    		0,2,1,
    		3,5,4,
    		6,8,7
		};	
	}
	public IntBuffer getElementBuffer() {
		elemIndexLength = elementData.length;
		elementBuffer = IntBuffer.wrap(elementData);
		return elementBuffer;
	}
}
