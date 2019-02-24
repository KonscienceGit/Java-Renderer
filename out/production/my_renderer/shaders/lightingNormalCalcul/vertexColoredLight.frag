#version 450

// Incoming interpolated (between vertices) color.
in vec3 vertColorPostGeom;
in vec3 vertNormalPostGeom;

uniform vec3 lightVector;

// Outgoing final color.
out vec4 outputColor;


void main(){

    vec3 normal = vertNormalPostGeom;
    vec3 shade = vec3(max(dot(normal, lightVector), 0.0));
    outputColor = vec4(vertColorPostGeom*shade, 1);

}
