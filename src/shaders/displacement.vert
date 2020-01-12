#version 450

//Ce shader s'applique sur une surface 2D et augmente la dimension Z en fonction des valeurs de la texture.
//Displacement, mais limité à des mesh 2D

// Incoming vertex position, Model Space.
in vec2 position; //x, y coordinates
in vec2 texCoord; //u, v coordinates
in vec3 normal; //vertex normal

uniform sampler2D texMap; //texture map for displacement

// Projection/View/Model matrix
uniform mat4 proj;
uniform mat4 view;
uniform mat4 model;

// Outgoing color.
out vec3 interpolatedColor;

void main(){
	vec3 displacement =  vec3(texture(texMap,texCoord));
	float heightCoefficient = (displacement.x + displacement.y + displacement.z)/3;
    // Normally gl_Position is in Clip Space and we calculate it by multiplying together all the matrices
    gl_Position = proj * (view * (model * vec4(position, heightCoefficient, 1)));

    // We assign the color to the outgoing variable.
    //interpolatedColor = vec3(position, 1);//color with position
    interpolatedColor = vec3(1 ,1, 1);
}
