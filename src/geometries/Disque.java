package geometries;

import com.jogamp.opengl.GL4ES3;

import java.nio.IntBuffer;
import java.util.Vector;

public class Disque extends Geometry {
	private static final double pi2 = 2*Math.PI;
	private boolean wireframe = false;
	public Disque() {
		if(wireframe) {
			drawMethod = GL4ES3.GL_LINES;
		}else {
			drawMethod = GL4ES3.GL_TRIANGLES;	
		}
		valueType = GL4ES3.GL_UNSIGNED_INT;
		textured = false;
		normals = false;
		triDimensional = true;
		rotate3D = true;
		vertexShaderPath = "shaders/triDimensional.vert";
		fragmentShaderPath = "shaders/vertexColored.frag";
		byteStride = 6*Float.BYTES;
		byteOffset = 3*Float.BYTES;
		polygonDetail = 20;
	}

	private void putColor(Vector<Float> vertexVec){
		//colors for now
		vertexVec.add(0.0f);
		vertexVec.add(0.0f);
		vertexVec.add(1.0f);
	}

	@Override
	public float[] getVertexData() {
		Vector<Float> vertexVec = new Vector<>();
		//centre
		vertexVec.add(0.0f);//coordonnee x
		vertexVec.add(0.0f);//coordonnee y
		vertexVec.add(0.0f);//coordonnee z
		putColor(vertexVec);
		for (int i = 0; i < polygonDetail; i++) {
			double rad = i*pi2/polygonDetail;
			vertexVec.add((float) Math.cos(rad));//coordonnee x
			vertexVec.add((float) Math.sin(rad));//coordonnee y
			vertexVec.add(0.0f);//coordonnee z
			putColor(vertexVec);
		}
		float[] vertexArray = new float[vertexVec.size()];
		for (int i = 0; i < vertexVec.size(); i++) {
			vertexArray[i] = vertexVec.get(i);
		}
		return vertexArray;
	}

	@Override
	public IntBuffer getElementBuffer() {
		Vector<Integer> elementVec;
		if(wireframe) {//Index d'éléments pour lignes
			elementVec = generateLinesElements();
		}else {//index d'elements pour triangles
			elementVec = generateTrianglesElements();
		}
		elementData = new int[elementVec.size()];
		for (int i = 0; i < elementVec.size(); i++) {
			elementData[i] = elementVec.get(i);
		}
		elemIndexLength = elementData.length;
		elementBuffer = IntBuffer.wrap(elementData);
		return elementBuffer;
	}

	private Vector<Integer> generateLinesElements(){
		int n = polygonDetail;
		Vector<Integer> elementVec = new Vector<>();
		for (int i = 0; i < n-1; i++) {//Pour chaque rayon/triangle de lignes (sauf le dernier)
			elementVec.add(0);    //ligne 1
			elementVec.add(i+1);//ligne 1

			elementVec.add(i+1);//ligne 2
			elementVec.add(i+2);//ligne 2
		}
		//Dernier rayon, fermer le cercle
		elementVec.add(0);//ligne 1
		elementVec.add(n);//ligne 1

		elementVec.add(n);//ligne 2
		elementVec.add(1);//ligne 2
		return elementVec;
	}

	private Vector<Integer> generateTrianglesElements(){
		int n = polygonDetail;
		Vector<Integer> elementVec = new Vector<>();
		for (int i = 0; i < n-1; i++) {//tout les triangles sauf le dernier
			elementVec.add(0);
			elementVec.add(i+1);
			elementVec.add(i+2);
		}
		//Dernier triangle pour connecter le cercle
		elementVec.add(0);
		elementVec.add(n);
		elementVec.add(1);
		return elementVec;
	}
}