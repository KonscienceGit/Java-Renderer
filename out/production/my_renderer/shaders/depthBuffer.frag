
#version 450 core


// Outgoing final color.
out vec4 outputColor;


void main()
{
    //get the Zbuffer as a color (shade of grey)
    outputColor = vec4(vec3(gl_FragCoord.z), 1.0);
}
