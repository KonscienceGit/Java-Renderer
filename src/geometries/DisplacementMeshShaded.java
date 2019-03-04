package geometries;

import java.nio.IntBuffer;
import java.util.Vector;
import static com.jogamp.opengl.GL4.*;

public class DisplacementMeshShaded extends Geometrie {
	public DisplacementMeshShaded() {
		drawMethod = GL_TRIANGLES;	
		wireframe = false;	
		valueType = GL_UNSIGNED_INT;
		textured = true;
		triDimensional = false;
		rotate2D = false;
		rotate3D = true;
		texturePath = "images/height_map.jpg";
		lightSource = true;
		vertexShaderPath = "shaders/DisplacementFlatLighting/vertex.vert";
		geomShaderPath = "shaders/DisplacementFlatLighting/geometrie.geom";
		//geomShaderPath = "shaders/DisplacementFlatLighting/geometriePoints.geom";
		fragmentShaderPath = "shaders/DisplacementFlatLighting/fragment.frag";
		//fragmentShaderPath = "shaders/DisplacementFlatLighting/fragmentWhite.frag";
		//fragmentShaderPath = "shaders/DisplacementFlatLighting/fragmentNormalColored.frag";
		//fragmentShaderPath = "shaders/DisplacementFlatLighting/fragmentDepthBuffDebug.frag";

		byteStride = 4*Float.BYTES;
		byteOffset = 2*Float.BYTES;
		polygonDetail = 256;
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
		Vector<Float> vertexVec = new Vector<Float>();
		for (int y = 0; y < polygonDetail; y++) {
			for (int x = 0; x < polygonDetail; x++) {
				vertexVec.add(center(x));//coordonnee x
				vertexVec.add(center(y));//coordonnee y
				vertexVec.add(normTexture(x));//coordonnee text x
				vertexVec.add(normTexture(y));//coordonnee text y
			}
		}
		float[] vertexArray = new float[vertexVec.size()];
		for (int i = 0; i < vertexVec.size(); i++) {
			vertexArray[i] = vertexVec.get(i); 
		}
		/*System.out.println("x\t\ty\t\tu\t\tv");//Afficher les coordonnées de vertex et de textures
		for (int i = 0; i < vertexVec.size(); i+= 5) {
			System.out.println(vertexArray[i]+"\t\t"+vertexArray[i+1]+"\t\t"+vertexArray[i+2]+"\t\t"+vertexArray[i+3]);
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
		for (short y = 0; y < n-1; y++) {
			for (short x = 0; x < n-1; x++) {
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
		Vector<Integer> elementVec = new Vector<Integer>();
		for (short y = 0; y < n-1; y++) {
			for (short x = 0; x < n-1; x++) {
				elementVec.add(x+y*n);//angle supérieur gauche, triangle 1
				elementVec.add(x+(y+1)*n);//angle inférieur gauche, triangle 1
				elementVec.add((x+1)+y*n);//angle supérieur droit, triangle 1

				elementVec.add((x+1)+y*n);//angle supérieur droit, triangle 2
				elementVec.add(x+(y+1)*n);//angle inférieur gauche, triangle 2
				elementVec.add((x+1)+(y+1)*n);//angle inférieur droit, triangle 2
			}
		}
		return elementVec;
	}
}
