package geometries;

import java.nio.IntBuffer;

public abstract class Geometrie {
	protected static int drawMethod = -1;
	protected static int valueType = -1;
	protected static boolean textured = false;
	protected static boolean normals = false;
	protected static boolean triDimensional = false;
	protected static boolean rotate2D = false;
	protected static boolean rotate3D = false;
	protected static boolean repeatTexture = false;
	protected static boolean lightSource = false;
	protected static boolean wireframe = false;
	protected static String texturePath = "";
	protected static String vertexShaderPath = "";
	protected static String geomShaderPath = "";
	protected static String fragmentShaderPath = "";
	protected static float[] vertexData = {};
	protected static int[] elementData = {};
    protected static IntBuffer elementBuffer;
    protected static int byteStride = 0;
    protected static int byteOffset = 0;
    protected static int byteOffset2 = 0;
    protected static int elemIndexLength = 0;
    protected static int polygonDetail = 0;
	protected static float scale = 0.5f;

	/**@return Retourne l'échelle de scaling vertical (axe z) du modèle*/
	public static float getScale() {
		return scale;
	}

	/**@return Retourne la métode de "draw", (GL_LINES ou GL_TRIANGLES)	 */
	public int getDrawMethod() {
		return drawMethod;
	}

	/**@return Retourne le type des valeurs d'index, (GL_UNSIGNED_SHORT ou GL_UNSIGNED_INT)  */
	//TODO Implémenter le binding de buffer d'element DANS les sous classes géométries pour pouvoir choisir le type
	public int getValueType() {
		return valueType;
	}

	public boolean isTextured() {
		return textured;
	}
	public boolean hasNormals() {
		return normals;
	}
	public boolean isTriDimensional() {
		return triDimensional;
	}
	public boolean isRotate2D() {
		return rotate2D;
	}
	public boolean isRotate3D() {
		return rotate3D;
	}
	public boolean isRepeatTexture() {
		return repeatTexture;
	}
	public boolean hasLightSource() {
		return lightSource;
	}
	public String getTexturePath() {
		return texturePath;
	}
	public String getVertexShaderPath() {
		return vertexShaderPath;
	}
	public String getGeomShaderPath() {
		return geomShaderPath;
	}
	public String getFragmentShaderPath() {
		return fragmentShaderPath;
	}

	/**@return Retourne les données des vertices
	 * (coordonnées dans l'espace mais possiblement les coordonnées de textures, les vecteurs de normale etc)*/
	public float[] getVertexData() {
		return vertexData;
	}

	/**@return Retourne les paires indices de vertex permettant de dessiner la géométrie. */
	public IntBuffer getElementBuffer() {
		return elementBuffer;
	}

	public int getByteStride() {
		return byteStride;
	}
	public int getByteOffset() {
		return byteOffset;
	}
	public int getByteOffset2() {
		return byteOffset2;
	}
	public int getElemIndexLength() {
		return elemIndexLength;
	}
}
