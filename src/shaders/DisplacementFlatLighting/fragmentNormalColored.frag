#version 450

in vec3 vertNormalPostGeom;
out vec4 outputColor;

void main(){
    outputColor = vec4(vertNormalPostGeom, 1.0);
}
