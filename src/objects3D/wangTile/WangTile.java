package objects3D.wangTile;

import java.nio.IntBuffer;
import java.util.Vector;
import com.jogamp.opengl.GL4ES3;
import geometries.Geometry;

public class WangTile extends Geometry {
	//dimensions du wang tile original
	private static int wangTileH = 4;
	private static int wangTileW = 4;
	//dimensions du virtual tile généré depuis le wang tile
	private static int genTileH, genTileW;
	
	private float[] wangTilePhysCoord = new float[wangTileH*wangTileW*2];
	private boolean[][] wangTilePhysBorder = {
			//nord, sud, est, ouest
			//false -> vide, true -> bord présent
			new boolean[] {false,true ,false,false},  new boolean[] {false,true ,true ,false},  new boolean[] {false,true ,true ,true },  new boolean[] {false,true ,false,true },
			new boolean[] {true ,true ,false,false},  new boolean[] {true ,true ,true ,false},  new boolean[] {true ,true ,true ,true },  new boolean[] {true ,true ,false,true },
			new boolean[] {true ,false,false,false},  new boolean[] {true ,false,true ,false},  new boolean[] {true ,false,true ,true },  new boolean[] {true ,false,false,true },
			new boolean[] {false,false,false,false},  new boolean[] {false,false,true ,false},  new boolean[] {false,false,true ,true },  new boolean[] {false,false,false,true }
	};
	
	private short[] genTileIndex;
	
	/*public short[] genTileIndex = {
			//index des équivalences des tiles entre le tile généré et le wangtile original
			 1,  3, 12,  1,  3,
			 9, 11, 12,  9, 11,
			12, 12, 12, 12, 12,
			 1,  3, 12,  1,  3,
			 9, 11, 12,  9, 11
	};*/
	
	// COUCOU, 9*22
	/*public short[] genTileIndex = {
			//index des équivalences des tiles entre le tile généré et le wangtile original
			12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,
			12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,
			 12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,
			 12,1,14, 12,1,14,3, 12,4,12,4, 12,1,14, 12,1,14,3, 12,4,12,4,
			 12,4,12, 12,4,12,4, 12,4,12,4, 12,4,12, 12,4,12,4, 12,4,12,4,
			 12,9,14, 12,9,14,11,12,9,14,11,12,9,14, 12,9,14,11,12,9,14,11,
			 12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,
			 12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,
			 12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12
	};*/
	/*public short[] genTileIndex = {
			//index des équivalences des tiles entre le tile généré et le wangtile original
			 0,1,2,3,
			 4,5,6,7,
			 8,9,10,11,
			 12,13,14,15
	};*/


	/**Géométrie utilisant un atlas de texture pour générer un "labyrinthe", egalement appelé Wang Tile*/
	public WangTile() {
		drawMethod = GL4ES3.GL_TRIANGLES;	
		wireframe = false;	
		valueType = GL4ES3.GL_UNSIGNED_INT;
		textured = true;
		triDimensional = false;
		rotate2D = false;
		rotate3D = true;
		texturePath = "images/wang_tile.png";
		lightSource = true;
		normals = true;
		vertexShaderPath = "objects3D/wangTile/vertex.vert";
		geomShaderPath = "objects3D/wangTile/geometrie.geom";
		//geomShaderPath = "geometries/wangTile/geometriePoints.geom";
		fragmentShaderPath = "objects3D/wangTile/fragment.frag";
		//fragmentShaderPath = "geometries/wangTile/fragmentWhite.frag";
		//fragmentShaderPath = "geometries/wangTile/fragmentNormalColored.frag";
		//fragmentShaderPath = "geometries/wangTile/fragmentDepthBuffDebug.frag";

		byteStride = 4*Float.BYTES;
		byteOffset = 2*Float.BYTES;
		genTileH = genTileW = 50;
		polygonDetail = genTileH*20;
		displacementScale = 0.8f/genTileH;
		genTileIndex = generateTileIndex();

		for (int y = 0; y < wangTileH; y++) {
			for (int x = 0; x < wangTileW; x++) {
				wangTilePhysCoord[2*(x+y*wangTileW)]   = (float)x/(float)wangTileW;//coord x
				wangTilePhysCoord[2*(x+y*wangTileW)+1] = (float)y/(float)wangTileH;//coord y
				//System.out.println("paire: "+(x+y*(wangTileW))+" X:"+wangTilePhysCoord[2*(x+y*(wangTileW))]+" Y:"+wangTilePhysCoord[2*(x+y*(wangTileW))+1]);
			}	
		}
	}

	/**Génère un tileset virtuel, un tableau a deux dimensions d'index des tiles du wang tile, de sorte à ce que chaque
	 *  tile connecte proprement avec les autres.
	 * @return Retourne l'index des tiles*/
	private short[] generateTileIndex(){
		 int wangTileCount = wangTileH*wangTileW;
		 int genTileCount = genTileH*genTileW;
		 short[] tileIndexArray = new short[genTileCount];
		 for (int y = 0; y < genTileH; y++) {
			 for (int x = 0; x < genTileW; x++) {
				 //on cherche les bordures autour du tile
				 //System.out.println("Pour tile ["+x+"] ["+y+"]");
				 boolean northBorder;
				 boolean westBorder;
				 boolean southBorderEnd;
				 boolean eastBorderEnd;
				 
				 if(y==0) {//si on est sur la premiere ligne de tile, pas de bordure nord
					 northBorder = false;
				 }else {//sinon, récupérer la bordure sud du tile au nord
					 northBorder = wangTilePhysBorder[tileIndexArray[x+(y-1)*genTileW]][1];
				 }
				 if(x == 0) {//si on est sur la premiere colonne de tile, pas de bordure ouest
					 westBorder = false;
				 }else {//sinon, récupérer la bordure Est du tile a l'ouest
					 westBorder = wangTilePhysBorder[tileIndexArray[(x-1)+y*genTileW]][2];
				 }
				 //si on est pas en bout sud ou en bout est, pas de restrictions, bordure au choix
				 //true == ne pas mettre de bordure
				 southBorderEnd = (y == genTileH-1);
				 eastBorderEnd =  (x == genTileW-1);
				 int randomTileNum = (int)(Math.random()*wangTileCount);
				 int counter =  randomTileNum;
				 int endCounter = randomTileNum+wangTileCount;
				 boolean match = false;
				 while (counter < endCounter && !match ) {//pour chaque tile, trouver un tile qui correspond à l'emplacement
					 int validBorders = 0;
					 int tileNum = counter%wangTileCount;
					 if(northBorder == wangTilePhysBorder[tileNum][0]) {validBorders++;}//nord
					 if(!southBorderEnd || !wangTilePhysBorder[tileNum][1] ) {validBorders++;}//sud
					 if(!eastBorderEnd || !wangTilePhysBorder[tileNum][2] ) {validBorders++;}//ouest
					 if(westBorder == wangTilePhysBorder[tileNum][3]) {validBorders++;}//est
					 
					 if(validBorders == 4) {
						 match = true;
						 tileIndexArray[x+y*genTileW] = (short) tileNum;
					 }
					 counter++;
				 }
			 }
		 }
		 return tileIndexArray;		
	}

	/**Normalise les coordonnées de l'espace texture (uv 0/1) à l'espace modèle (xy -1/1).
	 * @return Retourne la coordonnée normalisée*/
	private float center(float coord) {//Normalize and Center the vertex in normalized model space -1 / 1
		coord /= (polygonDetail-1)*0.5;
		coord--;
		return coord;
	}

	/**Donne, à partir des coordonnées de vertex passés en paramètres, les coordonnées du wang tile physique, en
	 * fonction du tile virtuel généré.
	 * @return Retourne une paire de coordonnées x y correspondant à l'espace de texture*/
	private float[] getPhysicalTextCoord(float vertCoordX,float vertCoordY) {
		//obtiens les coordonnées du wangTile original
		//System.out.println("X: "+vertCoordX+" Y: "+vertCoordY);
		//calcul de l'index (x, y) du tile dans le tileset généré
		float tileCoordX = (float) (vertCoordX*(genTileW-0.0001));//on soustrait une infime partie pour que la derniere valeur ne s'arrondisse pas au dessus.
		float tileCoordY = (float) (vertCoordY*(genTileH-0.0001));
		int tileIndexX = (int) Math.floor(tileCoordX);
		int tileIndexY = (int) Math.floor(tileCoordY);
		//System.out.println("tileIndexX: "+tileIndexX+" tileIndexY: "+tileIndexY);
		//calcul du reste sur les coordonnées du tile, à l'echelle du wangTile 
		//(correspond à la coordonnée de texture du tile du wangTile, donc modulo 1/largeurWangTile)
		float restex = ((tileCoordX)-tileIndexX)/wangTileW;
		float restey = ((tileCoordY)-tileIndexY)/wangTileH;
		//System.out.println("reste x: "+restex+" reste y: "+restey);
		//Récupère avec les coordonnées du tile dans le tileset, le tile correpsondant dans le wangTile original
		int genTileNumber = tileIndexX+tileIndexY*genTileW;
		int wangTileNumber = getAssociatedTile( genTileNumber );
		//System.out.println("genTileNumber: "+genTileNumber);
		//System.out.println("wangTileNumber: "+wangTileNumber);
		int wangTileXCoordinate = 2*(wangTileNumber);
		int wangTileYCoordinate = 2*(wangTileNumber)+1;
		float texCoordX = wangTilePhysCoord[wangTileXCoordinate] + restex;
		float texCoordY = wangTilePhysCoord[wangTileYCoordinate] + restey;
		//System.out.println("TextCoord x: "+texCoordX);
		//System.out.println("TextCoord y: "+texCoordY);
		return new float[]{texCoordX,texCoordY};
		//System.out.println();
	}

	/**@return Retourne le tile virtuel à la position passée en parametre*/
	private int getAssociatedTile(int genTileNumber) {
		return genTileIndex[genTileNumber];
	}
	
	@Override
	public float[] getVertexData() {
		Vector<Float> vertexVec = new Vector<>();
		for (int y = 0; y < polygonDetail; y++) {
			float vertCoordY = center(y);
			for (int x = 0; x < polygonDetail; x++) {
				vertexVec.add(center(x));//coordonnee x
				vertexVec.add(vertCoordY);//coordonnee y
				float fdivider = (float)(polygonDetail-1);
				float[] textCoordXY = getPhysicalTextCoord(x/fdivider,y/fdivider);
				vertexVec.add(textCoordXY[0]);//coordonnee text x
				vertexVec.add(textCoordXY[1]);//coordonnee text y
				//vertexVec.add(normTexture(x));//coordonnee text x
				//vertexVec.add(normTexture(y));//coordonnee text x
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
		elementVec = generateTrianglesElements();
		elementData = new int[elementVec.size()];
		for (int i = 0; i < elementVec.size(); i++) {
			elementData[i] = elementVec.get(i);
		}
		
		elemIndexLength = elementData.length;
		elementBuffer = IntBuffer.wrap(elementData);
		return elementBuffer;
	}

	/**@return Retourne un vecteur d'index de vertex, chaque trio d'index représentant un triangle.*/
	private Vector<Integer> generateTrianglesElements(){
		int n = polygonDetail;
		Vector<Integer> elementVec = new Vector<>();
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
