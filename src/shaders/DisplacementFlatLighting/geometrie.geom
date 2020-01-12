#version 450 core

layout (triangles) in;
layout (triangle_strip, max_vertices = 3) out;
out vec3 vertNormalPostGeom;

void main() {
	vertNormalPostGeom = normalize(
		cross(
			vec3(gl_in[1].gl_Position) - vec3(gl_in[0].gl_Position),
			vec3(gl_in[2].gl_Position) - vec3(gl_in[0].gl_Position)
		)
	);
    for (int i = 0; i < 3; i++){
        gl_Position = gl_in[i].gl_Position;
        EmitVertex();
    }
    EndPrimitive();
}
