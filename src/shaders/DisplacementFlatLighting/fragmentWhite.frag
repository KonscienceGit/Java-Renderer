#version 450

in vec3 vertNormalPostGeom;

uniform vec3 lightVector;

out vec4 outputColor;

void main(){
    vec3 shade = vec3(max(dot(vertNormalPostGeom, lightVector), 0.0));
    outputColor = vec4(1.0,1.0,1.0, 1.0);
}
