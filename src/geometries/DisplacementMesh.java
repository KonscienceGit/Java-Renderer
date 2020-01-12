package geometries;

import java.nio.IntBuffer;
import java.util.Vector;

import com.jogamp.opengl.GL4ES3;

public class DisplacementMesh extends Geometry {
	private boolean wireframe = true;
	public DisplacementMesh() {
		if(wireframe) {
			drawMethod = GL4ES3.GL_LINES;
		}else {
			drawMethod = GL4ES3.GL_TRIANGLES;	
		}
		
		valueType = GL4ES3.GL_UNSIGNED_INT;
		textured = true;
		normals = true;
		triDimensional = false;
		rotate2D = false;
		rotate3D = true;
		texturePath = "images/height_map.jpg";
		vertexShaderPath = "shaders/displacement.vert";
		fragmentShaderPath = "shaders/vertexColored.frag";
		byteStride = 7*Float.BYTES;
		byteOffset = 2*Float.BYTES;
		byteOffset2 = 4*Float.BYTES;
		polygonDetail = 200;
	}
	
	private float center(float coord) {//Normalize and Center the vertex in normalized model space -1 / 1
		coord /= (polygonDetail-1)*0.5;
		coord--;
		return coord;
	}
	
	private float normTexture(float coord) {//Normalize the coordinate in normalized texture coord 0 / 1
		coord /= (polygonDetail-1);
		return coord;
	}
	
	@Override
	public float[] getVertexData() {
		Vector<Float> vertexVec = new Vector<>();
		for (int y = 0; y < polygonDetail; y++) {
			for (int x = 0; x < polygonDetail; x++) {
				vertexVec.add(center(x));//coordonnee x
				vertexVec.add(center(y));//coordonnee y
				vertexVec.add(normTexture(x));//coordonnee text x
				vertexVec.add(normTexture(y));//coordonnee text y
				//normals
				vertexVec.add(0.0f);
				vertexVec.add(0.0f);
				vertexVec.add(1.0f);
			}
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
		if(wireframe) {//index as lines
			elementVec = generateLinesElements();
		}else {//index as triangles
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
		for (int y = 0; y < n-1; y++) {
			for (int x = 0; x < n-1; x++) {
				elementVec.add(x+y*n);//haut gauche, ligne 1
				elementVec.add((x+1)+y*n);//haut droite, ligne 1

				elementVec.add((x+1)+y*n);//haut droite, ligne 2
				elementVec.add(x+(y+1)*n);//angle inferieur gauche, ligne 2

				elementVec.add(x+(y+1)*n);//angle inferieur gauche, ligne 3
				elementVec.add(x+y*n);//angle supérieur gauche, ligne 3

				if (x == n-2 ) { //si fin de rangée (x), finir en dessinant la derniere ligne
					elementVec.add((x+1)+y*n);//angle supérieur droit, ligne 4
					elementVec.add((x+1)+(y+1)*n);//angle inférieur droit, ligne 4
				}

				if (y == n-2 ) { //si fin de colonne (y), finir en dessinant la derniere ligne
					elementVec.add((x+1)+(y+1)*n);//angle inférieur droit, ligne 5
					elementVec.add(x+(y+1)*n);//angle inférieur gauche, ligne 5
				}
			}
		}
		return elementVec;
	}

	private Vector<Integer> generateTrianglesElements(){
		int n = polygonDetail;
		Vector<Integer> elementVec = new Vector<>();
		for (int y = 0; y < n-1; y++) {
			for (int x = 0; x < n-1; x++) {
				elementVec.add(x+y*n);//angle supérieur gauche, triangle 1
				elementVec.add((x+1)+y*n);//angle supérieur droit, triangle 1
				elementVec.add(x+(y+1)*n);//angle inférieur gauche, triangle 1

				elementVec.add((x+1)+y*n);//angle supérieur droit, triangle 2
				elementVec.add((x+1)+(y+1)*n);//angle inférieur droit, triangle 2
				elementVec.add(x+(y+1)*n);//angle inférieur gauche, triangle 2
			}
		}
		return elementVec;
	}
}
