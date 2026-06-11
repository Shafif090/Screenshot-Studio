#version 150

uniform sampler2D DiffuseSampler;
uniform float FocusDistance;
uniform float Aperture;
in vec2 texCoord;
out vec4 fragColor;

void main() {
	vec2 center = texCoord - vec2(0.5);
	float blur = smoothstep(0.0, 0.7, length(center)) * Aperture * 0.001;
	vec4 sum = texture(DiffuseSampler, texCoord) * 0.36;
	sum += texture(DiffuseSampler, texCoord + vec2( blur, 0.0)) * 0.16;
	sum += texture(DiffuseSampler, texCoord + vec2(-blur, 0.0)) * 0.16;
	sum += texture(DiffuseSampler, texCoord + vec2(0.0,  blur)) * 0.16;
	sum += texture(DiffuseSampler, texCoord + vec2(0.0, -blur)) * 0.16;
	fragColor = sum;
}
