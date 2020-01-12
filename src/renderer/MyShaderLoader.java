package renderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Vector;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.GL4;

import geometries.Geometry;

public class MyShaderLoader {
	public static int createProgram(GL4 gl, Geometry geometry) {
		int programName = gl.glCreateProgram();

		int vertShaderName = gl.glCreateShader(GL_VERTEX_SHADER);		
		compileShader(gl, vertShaderName, geometry.getVertexShaderPath(), programName);
		
		int geomShaderName;
		if (!"".equals(geometry.getGeomShaderPath())) {
			geomShaderName = gl.glCreateShader(GL_GEOMETRY_SHADER);
			compileShader(gl, geomShaderName, geometry.getGeomShaderPath(), programName);
		}
		
		int fragShaderName = gl.glCreateShader(GL_FRAGMENT_SHADER);
		compileShader(gl, fragShaderName, geometry.getFragmentShaderPath(), programName);
		
		gl.glLinkProgram(programName);
		
		//-----------------
		//Check Program Link
		IntBuffer programLinkIntBuffer = IntBuffer.allocate(1);
		gl.glGetProgramiv(programName, GL_LINK_STATUS, programLinkIntBuffer);
		if (programLinkIntBuffer.get(0) == GL_FALSE) {
			displayLinkError(gl, programName, programLinkIntBuffer);
		}else {
			System.out.println("Program link success");	
		}
		
		//-----------------
		//Validate Program
		gl.glValidateProgram(programName);
		IntBuffer programValidIntBuffer = IntBuffer.allocate(1);
        gl.glGetProgramiv(programName,GL_VALIDATE_STATUS,programValidIntBuffer);
		if (programValidIntBuffer.get(0) == GL_FALSE) {
			System.out.println("Program validation Error, TODO: implement its log reader");	
		}else {
			System.out.println("Program validation success");	
		}
		
		return programName;
	}
		
	private static void compileShader(GL4 gl, int shaderName, String shaderPath, int programName) {
		String[] shadStrArray = null;
		try {
			shadStrArray = readFileAt(shaderPath);
		} catch (IOException e) {
			System.out.println(shaderPath+" -> Reading shader Error");
			e.printStackTrace();
			System.exit(-1);
		}

		int[] lineLengthArray = getLineLengthArray(shadStrArray);

		gl.glShaderSource(shaderName, shadStrArray.length, shadStrArray, lineLengthArray, 0);
		gl.glCompileShader(shaderName);
		
		IntBuffer shaderIntBuffer = IntBuffer.allocate(1);
		gl.glGetShaderiv(shaderName, GL_COMPILE_STATUS, shaderIntBuffer);
		if (shaderIntBuffer.get(0) == GL_FALSE) {
			System.out.println("Shader "+shaderPath+" -> Compilation Error:");
			displayCompilError(gl, shaderName, shaderIntBuffer, shaderPath);	
		}else {
			System.out.println("Shader "+shaderPath+" -> Compilation success");
			gl.glAttachShader(programName, shaderName);
			gl.glDeleteShader(shaderName);//once they are compiled and attached to the program, the shaders can be tagged for deletion.
			//they'll be deleted as soon as the program is deleted, or as soon as the shader is detached by glDetachShader()
		}
	}
	
    private static String[] readFileAt(String shaderPath) throws IOException {
    	//read the file at the given path and return an array of each lines of the file
    	InputStream shaderStream = MyShaderLoader.class.getResourceAsStream("../"+shaderPath);
    	String line;
        Vector<String> stringVector = new Vector<>();
		if (shaderStream != null) {
			BufferedReader in = new BufferedReader(new InputStreamReader(shaderStream));
			while((line = in.readLine()) != null ){
				if (line.length()>0) {
					stringVector.add(line.trim()+"\n");
				}
			}
			in.close();
			//transfer the vector into an array
			String[] stringArray = new String[stringVector.size()];
			for (int i = 0; i < stringVector.size(); i++) {
				stringArray[i] = stringVector.get(i);
			}
			return stringArray;
		}else {
			throw new IOException("ShaderLoader failed to load: ../shaders/" + shaderPath + "\n");
		}
    }
    
    private static int[] getLineLengthArray(String[] stringArray){
    	//provide an int[] array of each lines' length in the given string array
    	int lineNumber = stringArray.length;
        int[] lineLengthArray = new int[lineNumber];
       
        for(int i = 0; i < lineNumber ; i++){
        	if (stringArray[i] == null) {
        		stringArray[i] = "";
        	}
        	lineLengthArray[i] = stringArray[i].length();
        }      
        return lineLengthArray;
    }
    
    private static void displayLinkError(GL4 gl, int progName, IntBuffer intBuffer) {
        int size = intBuffer.get(0);
        if (size > 0) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(size);
            gl.glGetProgramInfoLog(progName, size, intBuffer, byteBuffer);
            for (byte b : byteBuffer.array()) {
                System.err.print((char) b);
            }
        } else {
            System.out.println("Unknown Program Link Error");
        } 
    }
    
    private static void displayCompilError(GL4 gl, int shaderName, IntBuffer intBuffer, String shaderPath) {
        int size = intBuffer.get(0);
        if (size > 0) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(size);
            gl.glGetShaderInfoLog(shaderName, size, intBuffer, byteBuffer);
            for (byte b : byteBuffer.array()) {
                System.err.print((char) b);
            }
        } else {
            System.out.println(shaderPath+" -> Unknown Shader Compilation Error");
        } 
    }
}
