#version 330

// Incoming interpolated (between vertices) texture coordinates.
in vec2 interpolatedTexCoord;


// Uniform 2D sampler for our texture object.
uniform sampler2D texMap;


// Outgoing final color.
out vec4 outputColor;


void main()
{
    // We sample texture0 at the interpolatedTexCoord
    outputColor = texture(texMap, interpolatedTexCoord);
}
