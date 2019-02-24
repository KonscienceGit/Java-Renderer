package geometries;

import java.nio.IntBuffer;
import java.util.Vector;

import com.jogamp.opengl.GL4ES3;

public class BasicMesh extends Geometrie {
	private boolean wireframe = false;
	public BasicMesh() {
		if(wireframe) {
			drawMethod = GL4ES3.GL_LINES;
		}else {
			drawMethod = GL4ES3.GL_TRIANGLES;	
		}
		
		valueType = GL4ES3.GL_UNSIGNED_INT;
		textured = false;
		triDimensional = false;
		rotate2D = false;
		texturePath = "";
		vertexShaderPath = "shaders/biDimensional.vert";
		fragmentShaderPath = "shaders/vertexColored.frag";
		byteStride = 5*Float.BYTES;
		byteOffset = 3*Float.BYTES;
		polygonDetail = 10;
	}
	
	private float center(float coord) {//Normalize and Center the vertex in normalized model space -1 / 1
		coord /= (polygonDetail-1)*0.5;
		coord--;
		return coord;		
	}
	
	@Override
	public float[] getVertexData() {
		Vector<Float> vertexVec = new Vector<Float>();
		for (int y = 0; y < polygonDetail; y++) {
			for (int x = 0; x < polygonDetail; x++) {
				vertexVec.add(center(x));//coordonnee x
				vertexVec.add(center(y));//coordonnee y
				vertexVec.add((float) (x%2));//couleur R
				vertexVec.add((float) (y%2));//couleur G
				vertexVec.add((float) ((x+y)%2));//couleur B
				
			}
		}
		float[] vertexArray = new float[vertexVec.size()];
		for (int i = 0; i < vertexVec.size(); i++) {
			vertexArray[i] = vertexVec.get(i); 
		}
		/*System.out.println("x\ty\tR\tG\tB");//Afficher les coordonnées de vertex et de couleur
		for (int i = 0; i < vertexVec.size(); i+= 5) {
			System.out.println(vertexArray[i]+"\t"+vertexArray[i+1]+"\t"+vertexArray[i+2]+"\t"+vertexArray[i+3]+"\t"+vertexArray[i+4]);
		}*/
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
		Vector<Integer> elementVec = new Vector<Integer>();
		for (int y = 0; y < n-1; y++) {
			for (int x = 0; x < n-1; x++) {
				elementVec.add((int) (x+y*n));//haut gauche, ligne 1
				elementVec.add((int) ((x+1)+y*n));//haut droite, ligne 1

				elementVec.add((int) ((x+1)+y*n));//haut droite, ligne 2
				elementVec.add((int) (x+(y+1)*n));//angle inferieur gauche, ligne 2

				elementVec.add((int) (x+(y+1)*n));//angle inferieur gauche, ligne 3
				elementVec.add((int) (x+y*n));//angle supérieur gauche, ligne 3

				if (x == n-2 ) { //si fin de rangée (x), finir en dessinant la derniere ligne
					elementVec.add((int) ((x+1)+y*n));//angle supérieur droit, ligne 4
					elementVec.add((int) ((x+1)+(y+1)*n));//angle inférieur droit, ligne 4
				}

				if (y == n-2 ) { //si fin de colonne (y), finir en dessinant la derniere ligne
					elementVec.add((int) ((x+1)+(y+1)*n));//angle inférieur droit, ligne 5
					elementVec.add((int) (x+(y+1)*n));//angle inférieur gauche, ligne 5
				}
			}
		}
		return elementVec;
	}

	private Vector<Integer> generateTrianglesElements(){
		int n = polygonDetail;
		Vector<Integer> elementVec = new Vector<Integer>();
		for (int y = 0; y < n-1; y++) {
			for (int x = 0; x < n-1; x++) {
				elementVec.add((int) (x+y*n));//angle supérieur gauche, triangle 1
				elementVec.add((int) ((x+1)+y*n));//angle supérieur droit, triangle 1
				elementVec.add((int) (x+(y+1)*n));//angle inférieur gauche, triangle 1

				elementVec.add((int) ((x+1)+y*n));//angle supérieur droit, triangle 2
				elementVec.add((int) ((x+1)+(y+1)*n));//angle inférieur droit, triangle 2
				elementVec.add((int) (x+(y+1)*n));//angle inférieur gauche, triangle 2
			}
		}
		return elementVec;
	}
}
