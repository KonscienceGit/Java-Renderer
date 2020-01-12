#version 450

// Incoming vertex position, Model Space.
in vec3 position;

// Incoming vertex texture mapping.
in vec2 texCoord;

// Projection matrix, distorting the world space into camera projection shape
uniform mat4 proj;
uniform mat4 view;
uniform mat4 model;

// Outgoing texture coordinates.
out vec2 interpolatedTexCoord;

void main(){
    // Normally gl_Position is in Clip Space and we calculate it by multiplying together all the matrices
    gl_Position = proj * (view * (model * vec4(position, 1)));

    // We assign the texture coordinate to the outgoing variable.
    interpolatedTexCoord = texCoord;
}
