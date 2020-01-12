#version 450

in vec3 position;
in vec3 color;
uniform mat4 proj;
uniform mat4 view;
uniform mat4 model;
out	vec3 vertColor;
out	vec3 vertPosition;

void main(){
    // Normally gl_Position is in Clip Space and we calculate it by multiplying together all the matrices
    gl_Position = proj * (view * (model * vec4(position, 1)));

    // We assign the color to the outgoing variable.
    vertColor = color;
    vertPosition = position;
}
