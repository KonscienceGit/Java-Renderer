#version 450

in	vec3 vertColor;
in	vec3 vertPosition;
uniform vec3 lightVector;
out vec4 outputColor;

void main(){
    vec3 normal = normalize(vertPosition);
    vec3 shade = vec3(max(dot(normal, lightVector), 0.0));
    outputColor = vec4(vertColor*shade, 1);
}
