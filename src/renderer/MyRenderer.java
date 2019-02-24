package renderer;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import com.jogamp.opengl.GL4;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES2.GL_RED;
import static com.jogamp.opengl.GL2ES3.GL_BLUE;
import static com.jogamp.opengl.GL2ES3.GL_GREEN;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import static com.jogamp.opengl.GL2GL3.GL_TEXTURE_SWIZZLE_RGBA;
import static renderer.MyShaderLoader.createProgram;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;

import geometries.*;
import geometries.wangTile.WangTile;

public class MyRenderer implements GLEventListener{
	
	private static GLCanvas glcanvas;
	private static long start;
	private static long last;
	private static long nbFrame;
	private static Geometrie geometrie = new WangTile();
	private static Animator animator;
	private static int vertexStride;
	private static int vertexOffset;
	private static int positionAttributeNumber; 
	private static final float pi = (float)Math.PI;
	   
	//Uniform locations
	private static int proj_uni_location;
	private static int view_uni_location;
	private static int model_uni_location;
	private static int texMap_uni_location; //texture map passed as uniform
	private static int lightVector_uni_location; //vecteur de lumiere (situé à l'infini)
	private static int displacementScale_uni_location; //scaling vertical du displacement mapping
	//attribute locations
	private static int pos_att_location;
	private static int vertColor_att_location;
	private static int texCoord_att_location;
	
	private static boolean benchmark = false;
	   
	//Shader program
	private static int shaderProgramName = 0;
	
	//Enum VAO_IDs
	private interface VAO_ID {
		    int VAO_0 = 0;
			int NbVAO = 1; // nombre total de VAO
	    }
	
	//Vertex Array Object(s)
	private IntBuffer vao = GLBuffers.newDirectIntBuffer(VAO_ID.NbVAO);
		
	//Enum VBO_IDs
	private interface Buffers_ID {
	    int Vertex = 0; //numero du VBO pour les vertex
		int Element = 1; //numero du VBO pour les elements
		int NbBuffer = 2; // nombre total de VBO
    }
	    
	//Buffer Object(s)
	private IntBuffer buffers = GLBuffers.newDirectIntBuffer(Buffers_ID.NbBuffer);
	
	private interface Textures_ID {
	    int Texture0 = 0;
		int NbTextures = 1;
    }
	
	private IntBuffer textures = GLBuffers.newDirectIntBuffer(Textures_ID.NbTextures);

	public static void main(String[] args) {
		final GLProfile profile = GLProfile.get(GLProfile.GL4);
		GLCapabilities capabilities = new GLCapabilities(profile);
		
		glcanvas = new GLCanvas(capabilities);
		MyRenderer mymainclass = new MyRenderer();
		glcanvas.addGLEventListener(mymainclass);
		glcanvas.setSize(1150, 1150);
	
		final JFrame  jframe = new JFrame(geometrie.getClass().getSimpleName());
		jframe.getContentPane().add(glcanvas);
		jframe.setSize(glcanvas.getHeight(),glcanvas.getWidth());
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setVisible(true);
        animator = new Animator(glcanvas);
        animator.setRunAsFastAsPossible(benchmark); //fps tres variables
        animator.start();
		start = last = System.currentTimeMillis();
	}//end of main

	@Override
	public void init(GLAutoDrawable drawable) {
		//get context
		GL4 gl = drawable.getGL().getGL4();
		
		initShaders(gl);
		initVAO(gl);
		
		if(geometrie.isTextured()) {
			initTextures(gl);	
		}
		if(benchmark) {
			gl.setSwapInterval(0);	//desactive la synchro verticale
		}
		
		gl.glEnable(GL_DEPTH_TEST);
		//gl.glDepthFunc(GL_ALWAYS); //par défaut sur LESS. peut prendre ALWAYS, NEVER, EQUAL, LEQUAL, GREATER, NOTEQUAL, GEQUAL
		gl.glEnable(GL_CULL_FACE);//activer seulement pour les formes geometriques de volume (pas les billboard par exemple)
		//gl.glCullFace(GL_FRONT);//permet de cacher les faces de devant au lieu de derriere (skybox, etc) (GL_BACK par defaut)
		//gl.glFrontFace(GL_CW); //permet de définir le sens caractérisant les fornt faces (clock wise CW ou counter clock wise CCW)
	}
	
	private void initShaders(GL4 gl) {
		//-------------------------------------
		//Shaders & Uniforms
		shaderProgramName = createProgram(gl, geometrie);//creating the program shader from a vertex and fragment shader
		gl.glUseProgram(shaderProgramName);//selecting the shader program as active

		if (geometrie.isTextured()) {
			texCoord_att_location = gl.glGetAttribLocation(shaderProgramName, "texCoord");//getting the attribute name for texture coordinate
			texMap_uni_location = gl.glGetUniformLocation(shaderProgramName, "texMap");//getting the attribute name for the texture map uniform
		}else {
			vertColor_att_location = gl.glGetAttribLocation(shaderProgramName, "color");
		}
		
		if(geometrie.hasLightSource()){
			lightVector_uni_location = gl.glGetUniformLocation(shaderProgramName, "lightVector");
			float[] lightVectorArray = {0.5f,-0.5f,0.5f};
			Toolbox.normalize3(lightVectorArray);
			FloatBuffer lightVectorBuffer = FloatBuffer.wrap(lightVectorArray);
			gl.glUniform3fv(lightVector_uni_location, 1, lightVectorBuffer );
		}

		if(geometrie.hasNormals()){
			displacementScale_uni_location = gl.glGetUniformLocation(shaderProgramName,"dispScale");
			gl.glUniform1f(displacementScale_uni_location, geometrie.getScale() );
		}
		
		pos_att_location = gl.glGetAttribLocation(shaderProgramName, "position");
		proj_uni_location = gl.glGetUniformLocation(shaderProgramName,"proj");
		view_uni_location = gl.glGetUniformLocation(shaderProgramName,"view");
		model_uni_location = gl.glGetUniformLocation(shaderProgramName,"model");

		FloatBuffer projMatrixBuffer = GLBuffers.newDirectFloatBuffer(FloatUtil.makeIdentity(new float[16]));
		gl.glUniformMatrix4fv(proj_uni_location, 1, false, projMatrixBuffer );
		FloatBuffer viewMatrixBuffer = GLBuffers.newDirectFloatBuffer(FloatUtil.makeIdentity(new float[16]));
		gl.glUniformMatrix4fv(view_uni_location, 1, false, viewMatrixBuffer	);
	}

	/** Génère et initialise le Vertex Array Object, un objet comprenant un VBO actif ainsi que ses attributs.<p/>
	 *  Il permet notament d'éviter de réattribuer les attributs au VBO dans le draw call dans le cas ou multiples VBO sont utilisés.
	 * @param gl contexte OpenGL
	 */
	private void initVAO(GL4 gl) {
		gl.glGenBuffers(VAO_ID.NbVAO, vao);
		gl.glBindVertexArray(vao.get(VAO_ID.VAO_0));
		
		gl.glGenBuffers(Buffers_ID.NbBuffer, buffers);//genere les noms pour les buffers Vertex et Element, met ces noms dans le buffer de noms de Buffer
		setupVBO(gl);
		setupVBOAttribs(gl);
		setupEBO(gl);
		gl.glBindVertexArray(0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	private void setupVBO(GL4 gl) {
		//-------------------------------------
		//Vertices
		float[] vertexData = geometrie.getVertexData();//get vertex array from the selected shape object	
	    FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);//automatically generate a buffer from the given array (size and content included)
		gl.glBindBuffer(GL_ARRAY_BUFFER, buffers.get(Buffers_ID.Vertex));//Bind le buffer de vertex (numero 0) à la target ARRAY_BUFFER
		gl.glBufferData(GL_ARRAY_BUFFER, vertexBuffer.capacity()*Float.BYTES, vertexBuffer, GL_STATIC_DRAW);//crée et initialise la banque de données d'un buffer object
	}

	private void setupVBOAttribs(GL4 gl) {
		vertexStride = geometrie.getByteStride();
		vertexOffset = geometrie.getByteOffset();
        
		if(geometrie.isTriDimensional()) {
        	positionAttributeNumber = 3;
        }else {
        	positionAttributeNumber = 2;
        }
		
        gl.glEnableVertexAttribArray(pos_att_location);//active l'attribut du shader pour ce buffer
        gl.glVertexAttribPointer(pos_att_location, positionAttributeNumber, GL_FLOAT, false, vertexStride, 0);//definis la position de l'attribut dans le buffer
        
        if(geometrie.isTextured()) {
        	gl.glEnableVertexAttribArray(texCoord_att_location);
        	gl.glVertexAttribPointer(texCoord_att_location, 2, GL_FLOAT, false, vertexStride, vertexOffset);
        }else {//Not textured, using vertex color
        	gl.glEnableVertexAttribArray(vertColor_att_location);
    		gl.glVertexAttribPointer(vertColor_att_location, 3, GL_FLOAT, false, vertexStride, vertexOffset);
        }
	}
	
	private void setupEBO(GL4 gl) {
		//--------------------------------------
		//Element Array
		Buffer elementBuffer = geometrie.getElementBuffer();
	    gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffers.get(Buffers_ID.Element));
		gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer.capacity()*Integer.BYTES, elementBuffer, GL_STATIC_DRAW);
	}
	
	private void initTextures(GL4 gl) {
		gl.glGenTextures(Textures_ID.NbTextures, textures); // generate the texture names in the textures buffer array
		gl.glActiveTexture(GL_TEXTURE0); //select a texture unit (0) to be the one active
        gl.glBindTexture(GL_TEXTURE_2D, textures.get(Textures_ID.Texture0)); //bind the texture name, create a texture object of the designated target type and associate the name to it.
        
        URL imgUrl = getClass().getClassLoader().getResource(geometrie.getTexturePath());
        if (imgUrl == null) {
        	System.out.println("Image not found");
    	}else {
    		System.out.println("Texture loaded: "+imgUrl.getPath());
    	}  
        
        File imgFile = new File(imgUrl.getPath());
        BufferedImage imgBuffered = null;
		try {
			imgBuffered = ImageIO.read(imgFile);
		} catch (IOException e) {
			System.out.println("Erreur lecture fichier");
			e.printStackTrace();
		}
        Raster imgRaster = imgBuffered.getData();
        int imgHeight = imgRaster.getHeight();
        int imgWidth = imgRaster.getWidth();
        float[] imgFloats = null;
        imgFloats = imgRaster.getPixels(0, 0, imgWidth, imgHeight, imgFloats);
        for (int i = 0; i < imgFloats.length ; i++) {
        	imgFloats[i]/=255;//put back the 0.0-255.0 float into 0.0-1.0 float range 
        }
                
        int mipmapLevel = 0; //base level, full resolution. each level above 1 is a power of 2 reduction of texture resolution.
		FloatBuffer imgFloatBuffer = FloatBuffer.wrap(imgFloats);

		gl.glTexImage2D(//specify the texture object properties, could use texStorage2D to give the object a final allocation size
				GL_TEXTURE_2D,//target
				mipmapLevel,
				GL_RGB,//internal color format (GL_RGB, etc)
				imgWidth,
				imgHeight,
				0,//border around the texture
				GL_RGB,//external color format RGB, BGR, etc etc...
				GL_FLOAT,//type of each color, ex GL_UNSIGNED_INT_8_8_8_8
				imgFloatBuffer//the data buffer
				);
		gl.glUniform1i(texMap_uni_location,GL_TEXTURE0); //set the uniform to be the attached texture unit
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0); //set base mipmap level
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, mipmapLevel); //set max mip map level?
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		gl.glSamplerParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		gl.glSamplerParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		IntBuffer swizzle = GLBuffers.newDirectIntBuffer(new int[]{GL_RED, GL_GREEN, GL_BLUE, GL_ONE});
		//swizzle the colors of the textures, see https://www.khronos.org/opengl/wiki/Texture#Swizzle_mask
		gl.glTexParameterIiv(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_RGBA, swizzle);
        gl.glBindTexture(GL_TEXTURE_2D, 0);
	}

    private void updateModelMatrix(GL4 gl) {
        // update model matrix to rotate
        long now = System.currentTimeMillis();
        float diff = (float) (now - start) / 3000;
        float[] scale = FloatUtil.makeScale(new float[16], true, 0.60f, 0.60f, 0.60f );
        FloatBuffer modelMatrixBuffer;
        float[] rotation = FloatUtil.makeRotationEuler(new float[16], 0, 4*pi/3, 0, 0);//(le "penche en avant")
		//float[] rotation = FloatUtil.makeRotationEuler(new float[16], 0, pi, 0, 0);//vue de dessus
    	float[] rotate;
        if(geometrie.isRotate2D()) {
        	rotate = FloatUtil.makeRotationAxis(new float[16], 0, diff, 0f, 0f, 0.5f, new float[3]);
        }else if(geometrie.isRotate3D()){
        	rotate = FloatUtil.makeRotationAxis(new float[16], 0, diff, 0.0f, 0f, 1.0f, new float[3]);
        }else {//do not rotate
        	rotate = FloatUtil.makeIdentity(new float[16]);
        }
        float[] modelMatrixArray = FloatUtil.multMatrix(scale, rotation);
        modelMatrixArray = FloatUtil.multMatrix(modelMatrixArray, rotate);
        modelMatrixBuffer = GLBuffers.newDirectFloatBuffer(modelMatrixArray);
        
        gl.glUniformMatrix4fv(model_uni_location, 1, false, modelMatrixBuffer );
    }
	
	@Override
	public void display(GLAutoDrawable drawable) {
		GL4 gl = drawable.getGL().getGL4();

		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);

		if(geometrie.isTextured()) {
			gl.glActiveTexture(GL_TEXTURE0); //select a texture unit (0) to be the one active
			gl.glBindTexture(GL_TEXTURE_2D, textures.get(Textures_ID.Texture0)); //attach the texture to the active unit (and tell it it's a 2D texture)
		}

		//--------------------------------
		//Binding
		gl.glUseProgram(shaderProgramName);
		gl.glBindVertexArray(vao.get(VAO_ID.VAO_0));
		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffers.get(Buffers_ID.Element));

		updateModelMatrix(gl);

        gl.glDrawElements(
        		geometrie.getDrawMethod(),
        		geometrie.getElemIndexLength(),//param2 is the number of elements (read vertices)
        		geometrie.getValueType(), 
        		0);
        gl.glFlush();

        //UnBinding
		gl.glBindVertexArray(0);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        gl.glBindTexture(GL_TEXTURE_2D,0);
        gl.glUseProgram(0);
        if (benchmark && System.currentTimeMillis() > last+5000) {
        	last = System.currentTimeMillis();
        	System.out.println("FPS: "+nbFrame/5);
        	nbFrame = 0;
        }
        nbFrame++;
  	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL4 gl = drawable.getGL().getGL4();
        gl.glViewport(x, y, width, height);
	}
	
	@Override
	public void dispose(GLAutoDrawable drawable) {
        GL4 gl = drawable.getGL().getGL4();
        animator.stop();
        gl.glDeleteProgram(shaderProgramName);	
        System.out.println("Window closed, cleaning context");
	}
}
