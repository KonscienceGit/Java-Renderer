package renderer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.*;

public class Toolbox {
	/** Normalise un vecteur3 de flottants*/
	public static void normalize3(float[] vector) {
		float length = (float) Math.sqrt(vector[0]*vector[0]+vector[1]*vector[1]+vector[2]*vector[2]);
		vector[0] /= length;
		vector[1] /= length;
		vector[2] /= length;	
	}
	
	/**@return Retourne un flottant formatté pour afficher 2 décimales apres la virgule. */
	public static String formatF(float f){
		return String.format("%.2f", f);
	}
	
	/**@return Retourne un rapport sur les capacitées de la carte graphique, ainsi que les limitations du contexte OpenGL actuel */
	public static String getGraphicCapabilities(GL4 gl){
		String report = "";
		IntBuffer value1 = IntBuffer.allocate(1);
		IntBuffer value2 = IntBuffer.allocate(1);
		gl.glGetIntegerv(GL_MAJOR_VERSION, value1);
		gl.glGetIntegerv(GL_MINOR_VERSION, value2);
		report = report.concat("OpenGL version:        "+value1.get(0)+"."+value2.get(0)+"\n");
		String vendor = gl.glGetString(GL_VENDOR);
		report = report.concat("Vendor:                "+vendor+"\n");
		report = report.concat("Vendor version:        "+gl.glGetString(GL_VERSION)+"\n");
		report = report.concat("Renderer:              "+gl.glGetString(GL_RENDERER)+"\n");
		report = report.concat("Shading lang. version: "+gl.glGetString(GL_SHADING_LANGUAGE_VERSION)+"\n");
		
		report = report.concat("\n\nHardware Info\n\n");
		if (vendor.contains("ATI")) {
			if (gl.isExtensionAvailable("GL_ATI_meminfo")) {
				report = report.concat("GL_ATI_meminfo:\n");
				
				report = report.concat("VBO memory:\n");
				report = report.concat(concatMemInfo(report, gl, 0x87FB));
				
				report = report.concat("TEXTURE memory:\n");
				report = report.concat(concatMemInfo(report, gl, 0x87FC));
				
				report = report.concat("RENDER BUFFER Free memory:\n");
				report = report.concat(concatMemInfo(report, gl, 0x87FD));
			}	
		}
		
		report = report.concat("\nImplémentation:\n\n(Valeurs constantes, peuvent être éronnées.)\n");
		report = report.concat("Max texture units      : "+GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS+" units\n");
		report = report.concat("Max texture size       : "+GL_MAX_TEXTURE_SIZE+" texels\n");
		report = report.concat("Max 3D texture size    : "+GL_MAX_3D_TEXTURE_SIZE+" couches\n");
		report = report.concat("Max texture buffer size: "+GL_MAX_TEXTURE_BUFFER_SIZE+" texels\n");
		report = report.concat("\nLes simulations d'allocations de texture, donnent les résultats suivants:\n(Pour des textures carrées)\n");
		report = report.concat(getMaxTextureSize1D(gl));
		report = report.concat(getMaxTextureSize2D(gl));
		report = report.concat(getMaxTextureSize3D(gl));
		report = report.concat("\n");

		
		report = report.concat("\nSupported usefull extentions:");
		String[] extentions = {
				"GL_ARB_sparse_texture",
				"GL_AMD_sparse_texture"
		};
		for (String ext: extentions) {
			if (gl.isExtensionAvailable(ext)) {
				report = report.concat("Extention: "+ext+" is supported!\n");
			}
		}
		report = report.concat("\n");
		
		return report;
	}
	
	/**@return Retourne le rapport individuel sur l'état de la mémoire du GPU*/
	public static String concatMemInfo(String report, GL4 gl, int code){
		IntBuffer memInfo = IntBuffer.allocate(4);
		String strMemInfo = "";
		gl.glGetIntegerv(code, memInfo);
		strMemInfo = strMemInfo.concat("total memory free in the pool:        "+memInfo.get(0)+" KB\n");
		strMemInfo = strMemInfo.concat("largest available free block in pool: "+memInfo.get(1)+" KB\n");
		strMemInfo = strMemInfo.concat("total auxiliary memory free:          "+memInfo.get(2)+" KB\n");
		strMemInfo = strMemInfo.concat("largest auxiliary free block:         "+memInfo.get(3)+" KB\n\n");
		return strMemInfo;
	}

	public static void printDebug(GL4 gl, int _shaderProgram, int _vertexBufferName){
		System.out.println();
		System.out.println("/*********/");
		System.out.println("/* DEBUG */");
		System.out.println("/*********/");
		
		ByteBuffer byteValueBuffer = ByteBuffer.allocate(1);
		IntBuffer intValueBuffer = IntBuffer.allocate(1);
		
		gl.glGetIntegerv(GL_CONTEXT_FLAGS, intValueBuffer);
		System.out.println("Debug context: "+(intValueBuffer.get(0) == GL_CONTEXT_FLAG_DEBUG_BIT));
		System.out.println("getError says: "+getErrorCode(gl));
		
		gl.glGetBooleanv(GL_DEPTH_TEST, byteValueBuffer);
		System.out.println("Depth test is: "+byteValueBuffer.get(0));
		
		gl.glGetIntegerv(GL_POINT_SIZE, intValueBuffer);
		System.out.println("Point Size is: "+intValueBuffer.get(0));
		System.out.println();
		
		System.out.println("var _shaderProgram is: "+_shaderProgram);
		gl.glGetIntegerv(GL_CURRENT_PROGRAM, intValueBuffer);
		System.out.println("Active shaderprogram is: "+intValueBuffer.get(0));
		
		System.out.println("var _vertexBufferName is: "+_vertexBufferName);
		gl.glGetIntegerv(GL_ARRAY_BUFFER_BINDING, intValueBuffer);
		System.out.println("Active ArrayBuffer is: "+intValueBuffer.get(0));
	}
	
	public static String getErrorCode(GL4 gl){
		int errorCode = gl.glGetError();
		String errorStr;
		switch (errorCode) {
		case GL_NO_ERROR:
			errorStr = "GL_NO_ERROR";
			break;
		case GL_INVALID_ENUM:
			errorStr = "GL_INVALID_ENUM";
			break;
		case GL_INVALID_VALUE:
			errorStr = "GL_INVALID_VALUE";
			break;
		case GL_INVALID_OPERATION:
			errorStr = "GL_INVALID_OPERATION";
			break;
		case GL_INVALID_FRAMEBUFFER_OPERATION:
			errorStr = "GL_INVALID_FRAMEBUFFER_OPERATION";
			break;
		case GL_OUT_OF_MEMORY:
			errorStr = "GL_OUT_OF_MEMORY";
			break;
		case GL_STACK_UNDERFLOW:
			errorStr = "GL_STACK_UNDERFLOW";
			break;
		case GL_STACK_OVERFLOW:
			errorStr = "GL_STACK_OVERFLOW";
			break;
		default:
			errorStr = "UNKNOWN_ERROR";
			break;
		}
		return errorStr;
	}

	/**@return Retourne la dimension maximale de texture (pour une dimension de texture carrée, puissance de deux) */
	public static String getMaxTextureSize1D(GL4 gl){
		FloatBuffer imgFloatBuffer = null;
		int size = 1024;
		int increment = 0;
		int maxSize = 0;
		boolean maxReached = false;
		boolean potIncrement = true;
		while (!maxReached) {
			gl.glTexImage1D(//specify the texture object properties, could use texStorage2D to give the object a final allocation size
					GL_PROXY_TEXTURE_1D,//target
					0,//mipmap level
					GL_RGB8,//internal color format (GL_RGB, etc)
					size,
					0,//border around the texture
					GL_RGB,//external color format RGB, BGR, etc etc...
					GL_FLOAT,//type of each color, ex GL_UNSIGNED_INT_8_8_8_8
					imgFloatBuffer//the data buffer
					);
			IntBuffer value = IntBuffer.allocate(1);
			gl.glGetTexLevelParameteriv(GL_PROXY_TEXTURE_1D,0,GL_TEXTURE_HEIGHT, value);
			if (value.get(0) == 0) {//si la simulation d'allocation échoue
				if (potIncrement) {//si on incrémentait en puissance de deux
					potIncrement = false;//alors on passe en incrément logarithmique
					increment = maxSize/2;
					size = maxSize+increment;
				}else if (increment>1) {//tant qu'on incrémente par un entier positif pair
					increment = increment/2;
					size = maxSize+increment;
				}else {//sinon, on a atteint la taille maximale
					maxReached = true;
				}
				
			}else{//si la simulation d'allocation réussit
				maxSize = size;//met a jour la taille de texture max
				if(potIncrement) {
					size = 2*size;	
				}else {
					size = size+increment;
				}
			}
		}
		String report = "";
		report = report.concat("Taille texture 1D max: "+maxSize+" pixels.\n");
		return report;
	}
	
	/**@return Retourne la dimension maximale de texture (pour une dimension de texture carrée, puissance de deux) */
	public static String getMaxTextureSize2D(GL4 gl){
		FloatBuffer imgFloatBuffer = null;
		int size = 1024;
		int increment = 0;
		int maxSize = 0;
		boolean maxReached = false;
		boolean potIncrement = true;
		while (!maxReached) {
			gl.glTexImage2D(//specify the texture object properties, could use texStorage2D to give the object a final allocation size
					GL_PROXY_TEXTURE_2D,//target
					0,//mipmap level
					GL_RGB8,//internal color format (GL_RGB, etc)
					size,
					size,
					0,//border around the texture
					GL_RGB,//external color format RGB, BGR, etc etc...
					GL_FLOAT,//type of each color, ex GL_UNSIGNED_INT_8_8_8_8
					imgFloatBuffer//the data buffer
					);
			IntBuffer value = IntBuffer.allocate(1);
			gl.glGetTexLevelParameteriv(GL_PROXY_TEXTURE_2D,0,GL_TEXTURE_HEIGHT, value);
			if (value.get(0) == 0) {//si la simulation d'allocation échoue
				if (potIncrement) {//si on incrémentait en puissance de deux
					potIncrement = false;//alors on passe en incrément logarithmique
					increment = maxSize/2;
					size = maxSize+increment;
				}else if (increment>1) {//tant qu'on incrémente par un entier positif pair
					increment = increment/2;
					size = maxSize+increment;
				}else {//sinon, on a atteint la taille maximale
					maxReached = true;
				}
				
			}else{//si la simulation d'allocation réussit
				maxSize = size;//met a jour la taille de texture max
				if(potIncrement) {
					size = 2*size;	
				}else {
					size = size+increment;
				}
			}
		}
		String report = "";
		report = report.concat("Taille texture 2D max: "+maxSize+" x "+maxSize+" pixels.\n");
		return report;
	}

	/**@return Retourne la dimension maximale de texture (pour une dimension de texture carrée, puissance de deux) */
	public static String getMaxTextureSize3D(GL4 gl){
		FloatBuffer imgFloatBuffer = null;
		int size = 1024;
		int increment = 0;
		int maxSize = 0;
		boolean maxReached = false;
		boolean potIncrement = true;
		while (!maxReached) {
			gl.glTexImage3D(//specify the texture object properties, could use texStorage2D to give the object a final allocation size
					GL_PROXY_TEXTURE_3D,//target
					0,//mipmap level
					GL_RGB8,//internal color format (GL_RGB, etc)
					size,
					size,
					size,
					0,//border around the texture
					GL_RGB,//external color format RGB, BGR, etc etc...
					GL_FLOAT,//type of each color, ex GL_UNSIGNED_INT_8_8_8_8
					imgFloatBuffer//the data buffer
					);
			IntBuffer value = IntBuffer.allocate(1);
			gl.glGetTexLevelParameteriv(GL_PROXY_TEXTURE_3D,0,GL_TEXTURE_HEIGHT, value);
			if (value.get(0) == 0) {//si la simulation d'allocation échoue
				if (potIncrement) {//si on incrémentait en puissance de deux
					potIncrement = false;//alors on passe en incrément logarithmique
					increment = maxSize/2;
					size = maxSize+increment;
				}else if (increment>1) {//tant qu'on incrémente par un entier positif pair
					increment = increment/2;
					size = maxSize+increment;
				}else {//sinon, on a atteint la taille maximale
					maxReached = true;
				}
				
			}else{//si la simulation d'allocation réussit
				maxSize = size;//met a jour la taille de texture max
				if(potIncrement) {
					size = 2*size;	
				}else {
					size = size+increment;
				}
			}
		}
		String report = "";
		report = report.concat("Taille texture 3D max: "+maxSize+" x "+maxSize+" x "+maxSize+" pixels.\n");
		return report;
	}
}
