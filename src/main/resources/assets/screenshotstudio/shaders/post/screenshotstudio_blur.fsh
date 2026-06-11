#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 Direction;
uniform float Radius;
in vec2 texCoord;
out vec4 fragColor;

void main() {
	vec2 step = Direction * Radius;
	vec4 color = texture(DiffuseSampler, texCoord) * 0.40;
	color += texture(DiffuseSampler, texCoord + step) * 0.24;
	color += texture(DiffuseSampler, texCoord - step) * 0.24;
	color += texture(DiffuseSampler, texCoord + step * 2.0) * 0.06;
	color += texture(DiffuseSampler, texCoord - step * 2.0) * 0.06;
	fragColor = color;
}
