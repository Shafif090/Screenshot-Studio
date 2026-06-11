#version 150

uniform sampler2D DiffuseSampler;
uniform float Strength;
uniform float Time;
in vec2 texCoord;
out vec4 fragColor;

float noise(vec2 p) {
	return fract(sin(dot(p, vec2(12.9898, 78.233)) + Time) * 43758.5453);
}

void main() {
	vec4 color = texture(DiffuseSampler, texCoord);
	float grain = (noise(texCoord * 1920.0) - 0.5) * Strength;
	fragColor = vec4(clamp(color.rgb + grain, 0.0, 1.0), color.a);
}
