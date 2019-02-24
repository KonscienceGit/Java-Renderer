package renderer;


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
}
