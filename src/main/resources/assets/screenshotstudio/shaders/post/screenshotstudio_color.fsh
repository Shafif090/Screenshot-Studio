#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
	vec2 OutSize;
	vec2 InSize;
};

layout(std140) uniform ScreenshotStudioConfig {
	vec4 ColorA;  // brightness, contrast, saturation, temperature
	vec4 ColorB;  // tint, vignette, chromatic aberration, film grain
	vec4 LensA;   // sharpness, focus distance, aperture, DoF enabled
	vec4 Runtime; // time, reserved, reserved, reserved
};

out vec4 fragColor;

float noise(vec2 p) {
	return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

vec2 safeUv(vec2 uv) {
	return clamp(uv, vec2(0.001), vec2(0.999));
}

vec3 scene(vec2 uv) {
	return texture(InSampler, safeUv(uv)).rgb;
}

vec3 softBlur(vec2 uv, float radiusPixels) {
	vec2 r = (1.0 / InSize) * radiusPixels;
	vec3 color = scene(uv) * 0.22;
	color += scene(uv + vec2( r.x, 0.0)) * 0.12;
	color += scene(uv + vec2(-r.x, 0.0)) * 0.12;
	color += scene(uv + vec2(0.0,  r.y)) * 0.12;
	color += scene(uv + vec2(0.0, -r.y)) * 0.12;
	color += scene(uv + vec2( r.x,  r.y)) * 0.075;
	color += scene(uv + vec2(-r.x,  r.y)) * 0.075;
	color += scene(uv + vec2( r.x, -r.y)) * 0.075;
	color += scene(uv + vec2(-r.x, -r.y)) * 0.075;
	return color;
}

void main() {
	vec2 texel = 1.0 / InSize;
	float brightness = ColorA.x;
	float contrast = ColorA.y;
	float saturation = ColorA.z;
	float temperature = ColorA.w;
	float tint = ColorB.x;
	float vignetteAmount = ColorB.y;
	float aberration = ColorB.z;
	float grainAmount = ColorB.w;
	float sharpness = LensA.x;
	float focusDistance = clamp(LensA.y, 0.0, 1.0);
	float aperture = clamp(LensA.z, 0.0, 1.0);
	float dofEnabled = LensA.w;

	vec3 color = scene(texCoord);

	if (aberration > 0.0001) {
		vec2 center = texCoord - vec2(0.5);
		vec2 offset = center * texel * aberration * 18.0;
		float red = scene(texCoord + offset).r;
		float blue = scene(texCoord - offset).b;
		color = mix(color, vec3(red, color.g, blue), aberration);
	}

	if (dofEnabled > 0.5 && aperture > 0.0001) {
		float fakeDepth = distance(texCoord, vec2(0.5)) * 1.41421356;
		float circleOfConfusion = abs(fakeDepth - focusDistance);
		float blurMask = smoothstep(0.035, 0.42, circleOfConfusion);
		float radius = aperture * blurMask * 12.0;
		vec3 blurred = softBlur(texCoord, radius);
		color = mix(color, blurred, blurMask * aperture);
	}

	if (abs(sharpness) > 0.0001) {
		vec3 blur = softBlur(texCoord, sharpness > 0.0 ? 1.0 : 2.5 + (-sharpness * 4.0));
		if (sharpness > 0.0) {
			color = color + (color - blur) * sharpness * 1.35;
		} else {
			color = mix(color, blur, min(1.0, -sharpness * 0.85));
		}
	}

	if (abs(brightness) > 0.0001) {
		color += brightness;
	}
	if (abs(contrast) > 0.0001) {
		float factor = contrast >= 0.0 ? 1.0 + contrast * 2.0 : 1.0 + contrast * 0.85;
		color = (color - 0.5) * max(0.05, factor) + 0.5;
	}
	if (abs(saturation) > 0.0001) {
		float luma = dot(color, vec3(0.2126, 0.7152, 0.0722));
		float factor = max(0.0, 1.0 + saturation * 1.25);
		color = mix(vec3(luma), color, factor);
	}
	if (abs(temperature) > 0.0001) {
		color.r += temperature;
		color.g += temperature * 0.18;
		color.b -= temperature;
	}
	if (abs(tint) > 0.0001) {
		color.r += tint * 0.55;
		color.b += tint * 0.55;
		color.g -= tint * 0.65;
	}

	if (vignetteAmount > 0.0001) {
		float dist = distance(texCoord, vec2(0.5));
		float vignette = smoothstep(0.32, 0.86, dist) * vignetteAmount * 0.82;
		color *= (1.0 - vignette);
	}

	if (grainAmount > 0.0001) {
		float grain = (noise(texCoord * InSize) - 0.5) * grainAmount * 0.16;
		color += grain;
	}

	fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
