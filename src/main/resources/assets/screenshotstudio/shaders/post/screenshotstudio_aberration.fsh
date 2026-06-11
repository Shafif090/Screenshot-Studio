#version 150

uniform sampler2D DiffuseSampler;
uniform float Strength;
in vec2 texCoord;
out vec4 fragColor;

void main() {
	vec2 center = texCoord - vec2(0.5);
	vec2 offset = center * Strength * 0.01;
	float r = texture(DiffuseSampler, texCoord + offset).r;
	float g = texture(DiffuseSampler, texCoord).g;
	float b = texture(DiffuseSampler, texCoord - offset).b;
	fragColor = vec4(r, g, b, 1.0);
}
