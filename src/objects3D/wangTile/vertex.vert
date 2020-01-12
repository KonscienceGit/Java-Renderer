#version 450

//Ce shader s'applique sur une surface 2D et augmente la dimension Z en fonction des valeurs de la texture.
//Displacement, mais limité à des mesh 2D

// Incoming vertex position, Model Space.
in vec2 position; //x, y coordinates
in vec2 texCoord; //u, v coordinates

uniform sampler2D texMap; //texture map for displacement

uniform float dispScale;

// Projection/View/Model matrix
uniform mat4 proj;
uniform mat4 view;
uniform mat4 model;

void main(){
	vec3 displacement = vec3(texture(texMap,texCoord));
	float heightCoefficient = dispScale*(displacement.x + displacement.y + displacement.z)/3;
    gl_Position = proj * (view * (model * vec4(position, heightCoefficient, 1)));
}
