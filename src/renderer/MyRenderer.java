package renderer;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.Animator;

import static renderer.MyShaderLoader.createProgram;
import geometries.*;
import geometries.wangTile.WangTile;

public class MyRenderer implements GLEventListener{
	
	private static GLCanvas glcanvas;
	private static long start;
	private static long last;
	private static long nbFrame;
	private static Geometrie geometrie = new DisplacementMeshShaded();
	private static Animator animator;
	private static int vertexStride;
	private static int vertexOffset;
	private static int positionAttributeNumber; 
	private static final float pi = (float)Math.PI;
	private static boolean benchmark = false;
	private static int _shaderProgram = -2;
	   
	//Uniform locations
	private static int proj_uni_location;
	private static int view_uni_location;
	private static int model_uni_location;
	private static int lightVector_uni_location; //vecteur de lumiere
	private static int displacementScale_uni_location; //scaling vertical du displacement mapping
	//attribute locations
	private static int pos_att_location;
	private static int vertColor_att_location;
	private static int texCoord_att_location;

	//Vertex Array Object(s)
	private interface VAO_ID {
	    int VAO_0 = 0;
		int NbVAO = 1; // nombre total de VAO
    }
	private IntBuffer vao = IntBuffer.allocate(VAO_ID.NbVAO);
		
	//Buffer Object(s)
	private interface Buffers_ID {
	    int Vertex = 0; //numero du VBO pour les vertex
		int Element = 1; //numero du VBO pour les elements
		int NbBuffer = 2; // nombre total de VBO
    }
	private IntBuffer buffers = IntBuffer.allocate(Buffers_ID.NbBuffer);
	private interface Textures_ID {
	    int Texture0 = 0;
		int NbTextures = 1;
    }
	private IntBuffer textures = IntBuffer.allocate(Textures_ID.NbTextures);

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
		//System.out.println(Toolbox.getGraphicCapabilities(gl));
		initShaders(gl);
		initVAO(gl);
		//displayUniformStatus();
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
		System.out.println(Toolbox.getGraphicCapabilities(gl));
	}
	
	private void initShaders(GL4 gl) {
		//-------------------------------------
		//Shaders & Uniforms
		_shaderProgram = createProgram(gl, geometrie);//creating the program shader from a vertex and fragment shader
		gl.glUseProgram(_shaderProgram);//selecting the shader program as active

		if (geometrie.isTextured()) {
			texCoord_att_location = gl.glGetAttribLocation(_shaderProgram, "texCoord");//getting the attribute name for texture coordinates
		}else {
			vertColor_att_location = gl.glGetAttribLocation(_shaderProgram, "color");
		}
		
		if(geometrie.hasLightSource()){
			lightVector_uni_location = gl.glGetUniformLocation(_shaderProgram, "lightVector");
			float[] lightVectorArray = {0.5f,-0.5f,0.5f};
			Toolbox.normalize3(lightVectorArray);
			FloatBuffer lightVectorBuffer = FloatBuffer.wrap(lightVectorArray);
			gl.glUniform3fv(lightVector_uni_location, 1, lightVectorBuffer );
		}

		if(geometrie.hasNormals()){
			displacementScale_uni_location = gl.glGetUniformLocation(_shaderProgram,"dispScale");
			gl.glUniform1f(displacementScale_uni_location, geometrie.getScale() );
		}

		pos_att_location = gl.glGetAttribLocation(_shaderProgram, "position");
		proj_uni_location = gl.glGetUniformLocation(_shaderProgram,"proj");
		view_uni_location = gl.glGetUniformLocation(_shaderProgram,"view");
		model_uni_location = gl.glGetUniformLocation(_shaderProgram,"model");

		FloatBuffer projMatrixBuffer = FloatBuffer.wrap(FloatUtil.makeIdentity(new float[16]));
		gl.glUniformMatrix4fv(proj_uni_location, 1, false, projMatrixBuffer );
		FloatBuffer viewMatrixBuffer = FloatBuffer.wrap(FloatUtil.makeIdentity(new float[16]));
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
	    FloatBuffer vertexBuffer = FloatBuffer.wrap(vertexData);//automatically generate a buffer from the given array (size and content included)
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
		
        gl.glVertexAttribPointer(pos_att_location, positionAttributeNumber, GL_FLOAT, false, vertexStride, 0);//definis la position de l'attribut dans le buffer
        gl.glEnableVertexAttribArray(pos_att_location);//active l'attribut du shader pour ce buffer
        
        if(geometrie.isTextured()) {
        	gl.glVertexAttribPointer(texCoord_att_location, 2, GL_FLOAT, false, vertexStride, vertexOffset);
        	gl.glEnableVertexAttribArray(texCoord_att_location);
        }else {//Not textured, using vertex color
    		gl.glVertexAttribPointer(vertColor_att_location, 3, GL_FLOAT, false, vertexStride, vertexOffset);
    		gl.glEnableVertexAttribArray(vertColor_att_location);
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
        gl.glBindTexture(GL_TEXTURE_2D, textures.get(Textures_ID.Texture0)); //bind the texture name, 


		int mipmapLevel = 0; //base level, full resolution. each level above 1 is a power of 2 reduction of texture resolution.
		loadTexture(gl, geometrie.getTexturePath(), mipmapLevel);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0); //set base mipmap level
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, mipmapLevel); //set max mip map level?
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	}

	private void loadTexture(GL4 gl, String texturePath, int mipmapLevel) {

        URL imgUrl = getClass().getClassLoader().getResource(texturePath);
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
                
		FloatBuffer imgFloatBuffer = FloatBuffer.wrap(imgFloats);
		
		gl.glTexImage2D(//specify the texture object properties, could use texStorage2D to give the object a final allocation size
				GL_TEXTURE_2D,//target
				mipmapLevel,
				GL_RGB8,//internal color format (GL_RGB, etc)
				imgWidth,
				imgHeight,
				0,//border around the texture
				GL_RGB,//external color format RGB, BGR, etc etc...
				GL_FLOAT,//type of each color, ex GL_UNSIGNED_INT_8_8_8_8
				imgFloatBuffer//the data buffer
				);
	}

    private void updateModelMatrix(GL4 gl) {
        // update model matrix to rotate
        long now = System.currentTimeMillis();
        float diff = (float) (now - start) / 3000;
        float[] scale = FloatUtil.makeScale(new float[16], true, 0.80f, 0.80f, 0.60f );
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
        FloatBuffer modelMatrixBuffer = FloatBuffer.wrap(modelMatrixArray);
        
        gl.glUniformMatrix4fv(model_uni_location, 1, false, modelMatrixBuffer );
    }

	@Override
	public void display(GLAutoDrawable drawable) {
		GL4 gl = drawable.getGL().getGL4();

		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);

		if(geometrie.isTextured()) {
			gl.glActiveTexture(GL_TEXTURE0); //select a texture unit (0) to be the one active
			//pas besoin de réattacher la texture à la texture Unit si on 
			// utilise 1 sampler différent pour chaque texture
			//gl.glBindTexture(GL_TEXTURE_2D, textures.get(Textures_ID.Texture0)); 
		}

		//Binding
		gl.glUseProgram(_shaderProgram);
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
        //gl.glBindTexture(GL_TEXTURE_2D,0);
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
        gl.glDeleteProgram(_shaderProgram);	
        System.out.println("Window closed, cleaning context");
	}
}
