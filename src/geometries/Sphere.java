
package geometries;

import java.nio.IntBuffer;
import java.util.Vector;

import com.jogamp.opengl.GL4ES3;

public class Sphere extends Geometrie {
	private static final double pi2 = 2*Math.PI;
	private static final double pi = Math.PI;
	private boolean wireframe = false;
	public Sphere() {
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
		lightSource = true;
		if (lightSource) {
			//diffuses light
			//vertexShaderPath = "shaders/lightingSmooth/triDimensionalLight.vert";
			//fragmentShaderPath = "shaders/lightingSmooth/vertexColoredLight.frag";
			vertexShaderPath = "shaders/lightingNormalCalcul/triDimensionalLight.vert";
			geomShaderPath = "shaders/lightingNormalCalcul/computeNormals.geom";
			fragmentShaderPath = "shaders/lightingNormalCalcul/vertexColoredLight.frag";
						
		}else {
			//ambiant light
			vertexShaderPath = "shaders/triDimensional.vert";
			fragmentShaderPath = "shaders/vertexColored.frag";
			//fragmentShaderPath = "shaders/depthBuffer.frag";
		}
		
		byteStride = 6*Float.BYTES;
		byteOffset = 3*Float.BYTES;
		polygonDetail = 20; // minimum 4   maximum ~250
	}

	private void putColor(Vector<Float> vertexVec){
		//colors for now
		vertexVec.add(0.0f);
		vertexVec.add(0.0f);
		vertexVec.add(1.0f);
	}

	@Override
	public float[] getVertexData() {
		int n = polygonDetail;
		Vector<Float> vertexVec = new Vector<Float>();
		//sommet
		vertexVec.add(0.0f);//coordonnee x
		vertexVec.add(0.0f);//coordonnee y
		vertexVec.add(1.0f);//coordonnee z
		putColor(vertexVec);

		//disques
		for (int i = 1; i < n-1; i++){
			float z = (float) Math.cos(i*pi/(n-1));
			float rayon = (float) Math.sin(i*pi/(n-1));
			for (int j = 0; j < n; j++) {
				double rad = j*pi2/n;
				vertexVec.add((float) Math.cos(rad)*rayon);//coordonnee x
				vertexVec.add((float) Math.sin(rad)*rayon);//coordonnee y
				vertexVec.add( z );//coordonnee z
				putColor(vertexVec);
			}
		}

		//base
		vertexVec.add(0.0f);//coordonnee x
		vertexVec.add(0.0f);//coordonnee y
		vertexVec.add(-1.0f);//coordonnee z
		putColor(vertexVec);

		float[] vertexArray = new float[vertexVec.size()];
		for (int i = 0; i < vertexVec.size(); i++) {
			vertexArray[i] = vertexVec.get(i); 
		}
		/*System.out.println("x\t\ty\t\tz\t\tR\t\tG\t\tB");//Afficher les coordonnées de vertex et de textures
		for (int i = 0; i < vertexVec.size(); i+= 6) {
			System.out.println(formatF(vertexArray[i])+"\t"+formatF(vertexArray[i+1])+"\t"+formatF(vertexArray[i+2])+"\t"+formatF(vertexArray[i+3])+"\t"+formatF(vertexArray[i+4])+"\t"+formatF(vertexArray[i+5]));
		}*/
		return vertexArray;
	}

	@Override
	public IntBuffer getElementBuffer() {
		Vector<Integer> elementVec;
		if(wireframe) {//Index d'éléments pour lignes
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
		//cone du haut, sans la base
		for (int i = 1; i < n+1; i++) {//Pour chaque rayon/triangle de lignes (sauf le dernier)
			elementVec.add(0);
			elementVec.add(i);
		}

		//cone(s) tronqué(s)
		for (int i = 1; i < n-2; i++) {//Pour chaque rayon/triangle de lignes (sauf le dernier)
			for (int j = 1; j < n; j++){
				elementVec.add(j+(i-1)*n);  //ligne 1---2
				elementVec.add(j+1+(i-1)*n);//ligne 1---2
				elementVec.add(j+(i-1)*n);  //ligne 1---(1+n)
				elementVec.add(j+i*n);      //ligne 1---(1+n)
				elementVec.add(j+i*n);      //ligne (1+n)---2
				elementVec.add(j+1+(i-1)*n);//ligne (1+n)---2
			}
			//fermer le cone tronqué
			elementVec.add(n+(i-1)*n);//ligne (n)---1
			elementVec.add(1+(i-1)*n);//ligne (n)---1
			elementVec.add(n+(i-1)*n);//ligne (n)---(n+n)
			elementVec.add(n+i*n);    //ligne (n)---(n+n)
			elementVec.add(n+i*n);    //ligne (n+n)---1
			elementVec.add(1+(i-1)*n);//ligne (n+n)---1
		}

		//cone du bas
		for (int i = n*n-3*n+1; i < n*n-2*n; i++) {//Pour chaque rayon/triangle de lignes (sauf le dernier)
			elementVec.add( n*n-2*n+1);//ligne 1
			elementVec.add(i);  //ligne 1
			elementVec.add(i);  //ligne 2
			elementVec.add(i+1);  //ligne 2
		}
		//Dernier rayon, fermer le cone
		elementVec.add(n*n-2*n+1);//ligne 1 sommet
		elementVec.add(n*n-2*n);//ligne 1
		elementVec.add(n*n-2*n);//ligne 2
		elementVec.add(n*n-3*n+1);//ligne 2
		return elementVec;
	}

	private Vector<Integer> generateTrianglesElements(){
		int n = polygonDetail;
		Vector<Integer> elementVec = new Vector<Integer>();
		//cone du haut
		for (int i = 1; i < n; i++) {//Pour chaque rayon/triangle de lignes (sauf le dernier)
			elementVec.add(0);
			elementVec.add(i+1);
			elementVec.add(i);
		}
		//fermer le cone
		elementVec.add(0);
		elementVec.add(1);
		elementVec.add(n);


		//cone(s) tronqué(s)
		for (int i = 1; i < n-2; i++) {//Pour chaque rayon/triangle de lignes (sauf le dernier)
			for (int j = 1; j < n; j++){
				elementVec.add(j+(i-1)*n);  //vertex 1
				elementVec.add(j+1+(i-1)*n);//vertex 2
				elementVec.add(j+i*n);      //vertex 5
				elementVec.add(j+i*n);      //vertex 5
				elementVec.add(j+1+(i-1)*n);//vertex 2
				elementVec.add(j+1+i*n);      //vertex 6
			}
			//fermer le cone tronqué
			elementVec.add(n+(i-1)*n);//vertex 4
			elementVec.add(1+(i-1)*n);//vertex 1
			elementVec.add(n+i*n);    //vertex 8
			elementVec.add(n+i*n);    //vertex 8
			elementVec.add(1+(i-1)*n);//vertex 1
			elementVec.add(n+1+(i-1)*n);//vertex 5
		}

		//cone du bas
		for (int i = n*n-3*n+1; i < n*n-2*n; i++) {//Pour chaque rayon/triangle de lignes (sauf le dernier)
			elementVec.add(n*n-2*n+1);//sommet
			elementVec.add(i);  //ligne 1
			elementVec.add(i+1);  //ligne 2
		}
		//Dernier rayon, fermer le cone
		elementVec.add(n*n-2*n+1);//ligne 1 sommet
		elementVec.add(n*n-2*n);//ligne 2
		elementVec.add(n*n-3*n+1);//ligne 2
		return elementVec;
	}
}
