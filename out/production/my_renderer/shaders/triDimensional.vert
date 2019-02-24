#version 450

// Incoming vertex position, Model Space.
in vec3 position;

// Incoming vertex color.
in vec3 color;
//vec3 color2 = vec3 (1.0,1.0,1.0);

// Projection matrix, distorting the world space into camera projection shape
uniform mat4 proj;

//view matrix, moving the world under the camera
uniform mat4 view;

// model matrix, moving the model to its place in the scene
uniform mat4 model;

// Outgoing color.
out vec3 interpolatedColor;

void main(){

    // Normally gl_Position is in Clip Space and we calculate it by multiplying together all the matrices
    gl_Position = proj * (view * (model * vec4(position, 1)));

    // We assign the color to the outgoing variable.
    interpolatedColor = color;
}
