#version 150

uniform sampler2D DiffuseSampler;
uniform float Strength;
in vec2 texCoord;
out vec4 fragColor;

void main() {
	vec4 color = texture(DiffuseSampler, texCoord);
	float d = distance(texCoord, vec2(0.5));
	float vignette = smoothstep(0.35, 0.8, d) * Strength;
	fragColor = vec4(color.rgb * (1.0 - vignette), color.a);
}
