#version 450

// Incoming vertex position, Model Space.
in vec3 position;
in vec3 color;

uniform mat4 proj;
uniform mat4 view;
uniform mat4 model;

// Outgoing color.
out VS_OUT {
	vec3 vertColor;
} vs_out;


void main(){

    // Normally gl_Position is in Clip Space and we calculate it by multiplying together all the matrices
    gl_Position = proj * (view * (model * vec4(position, 1)));

    // We assign the color to the outgoing variable.
    vs_out.vertColor = color;
    //vs_out.vertPosition = position;
}
