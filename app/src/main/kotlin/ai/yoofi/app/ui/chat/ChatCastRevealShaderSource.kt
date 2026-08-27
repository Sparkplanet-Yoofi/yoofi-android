package ai.yoofi.app.ui.chat

/**
 * 人物卡揭示特效的 AGSL 实现，逐分支移植自 iOS `ChatRelationshipShaders.metal` 的 `relationshipReveal`。
 *
 * 与 metal 的两点必要差异：
 * 1. metal 用 `layer.sample`，AGSL 用 `layer.eval`；
 * 2. AGSL 的颜色是预乘 alpha，所以 metal 里的 `color.a *= f` 在这里写成 `color *= f`，
 *    否则 rgb 不跟着衰减会出现发白的边缘。
 */
internal const val ChatCastRevealAgsl = """
uniform shader layer;
uniform float progress;
uniform float effectValue;
uniform float2 size;
uniform half4 accentColor;

const float PI = 3.1415926;

float randomValue(float value) {
    return fract(sin(value * 91.733) * 43758.5453);
}

float randomValue2D(float2 value) {
    return fract(sin(dot(value, float2(127.1, 311.7))) * 43758.5453);
}

float valueNoise(float2 value) {
    float2 cell = floor(value);
    float2 fraction = fract(value);
    float2 curve = fraction * fraction * (3.0 - 2.0 * fraction);
    float bottom = mix(randomValue2D(cell), randomValue2D(cell + float2(1.0, 0.0)), curve.x);
    float top = mix(randomValue2D(cell + float2(0.0, 1.0)), randomValue2D(cell + float2(1.0, 1.0)), curve.x);
    return mix(bottom, top, curve.y);
}

float layeredNoise(float2 value) {
    return valueNoise(value) * 0.57
        + valueNoise(value * 2.03 + 13.7) * 0.29
        + valueNoise(value * 4.11 + 31.2) * 0.14;
}

half4 chromaticSample(float2 position, float offset) {
    half4 base = layer.eval(position);
    half red = layer.eval(position + float2(offset, 0.0)).r;
    half blue = layer.eval(position - float2(offset, 0.0)).b;
    return half4(red, base.g, blue, base.a);
}

half4 main(float2 position) {
    float2 safeSize = max(size, float2(1.0));
    float2 uv = position / safeSize;
    float2 centered = uv - 0.5;
    float remaining = 1.0 - progress;
    int effect = int(effectValue + 0.5);

    // 全息扫描：发光扫描前沿一边推进一边消解 RGB 折射
    if (effect == 0) {
        float wave = sin((uv.y * 40.0 - progress * 18.0) * PI);
        float offset = wave * remaining * remaining * 7.0;
        half4 color = chromaticSample(position + float2(offset, 0.0), remaining * 10.0);
        float front = 1.0 - smoothstep(progress - 0.05, progress + 0.13, uv.y);
        float scanLine = 1.0 - smoothstep(0.0, 0.025, abs(uv.y - progress));
        color.rgb += half3(0.08, 0.32, 0.42) * half(scanLine * remaining);
        color *= half(max(front, smoothstep(0.64, 0.92, progress)));
        return color;
    }

    // 数据重组：横向条带各自错位后依次锁定归位
    if (effect == 1) {
        float band = floor(uv.y * 14.0);
        float noise = randomValue(band);
        float direction = noise > 0.5 ? 1.0 : -1.0;
        float strength = pow(remaining, 2.35);
        float joltA = smoothstep(0.06, 0.12, progress) * (1.0 - smoothstep(0.18, 0.25, progress));
        float joltB = smoothstep(0.28, 0.34, progress) * (1.0 - smoothstep(0.40, 0.47, progress));
        float offset = direction * ((5.0 + noise * 25.0) * strength + joltA * 8.0 + joltB * 4.0);
        half4 color = chromaticSample(position + float2(offset, 0.0), strength * 9.0 + joltA * 3.0);
        float visible = step(noise * 16.0, floor(progress * 16.0));
        color *= half(max(visible, smoothstep(0.42, 0.72, progress)));
        return color;
    }

    // 液态揭示：有机的径向边缘一边扩张一边折射人物
    if (effect == 2) {
        float angle = atan(centered.y, centered.x);
        float organic = sin(angle * 7.0 + progress * 5.0) * 0.025 + sin(angle * 13.0 - progress * 7.0) * 0.014;
        float radius = length(centered * float2(1.0, 0.72));
        float edge = progress * 0.64 + organic;
        float mask = 1.0 - smoothstep(edge - 0.035, edge + 0.025, radius);
        float2 normalDirection = normalize(centered + float2(0.0001));
        float refraction = (1.0 - smoothstep(0.0, 0.07, abs(radius - edge))) * remaining * 12.0;
        half4 color = layer.eval(position - normalDirection * refraction);
        float glow = 1.0 - smoothstep(0.0, 0.045, abs(radius - edge));
        color.rgb += half3(0.22, 0.12, 0.38) * half(glow * remaining);
        color *= half(max(mask, smoothstep(0.78, 1.0, progress)));
        return color;
    }

    // 空间传送：像素自景深处汇聚，附带径向色散拖影
    if (effect == 3) {
        float2 radial = centered * safeSize;
        float streak = remaining * remaining * 0.075;
        float2 samplePosition = position - radial * streak;
        half4 color = chromaticSample(samplePosition, remaining * 14.0);
        float rays = 0.92 + 0.08 * sin(atan(centered.y, centered.x) * 18.0 + progress * 20.0);
        color.rgb *= half(rays + progress * 0.08);
        color *= half(smoothstep(0.0, 0.24, progress));
        return color;
    }

    // 能量裂隙：中央亮口横向撕开露出卡面
    if (effect == 4) {
        float ripple = sin(uv.y * 34.0 + progress * 12.0) * 0.025 * remaining;
        float distanceToRift = abs(centered.x + ripple);
        float opening = progress * 0.58;
        float mask = 1.0 - smoothstep(opening - 0.035, opening + 0.015, distanceToRift);
        float edgeGlow = 1.0 - smoothstep(0.0, 0.035, abs(distanceToRift - opening));
        float pull = sign(centered.x) * edgeGlow * remaining * 16.0;
        half4 color = chromaticSample(position + float2(pull, 0.0), edgeGlow * remaining * 8.0);
        color.rgb += half3(0.48, 0.25, 0.62) * half(edgeGlow * remaining * 1.4);
        color *= half(max(mask, smoothstep(0.82, 1.0, progress)));
        return color;
    }

    // 数字故障：高频跳变色块、跳帧与强烈 RGB 分离
    if (effect == 5) {
        float timeStep = floor(progress * 18.0);
        float band = floor(uv.y * 22.0);
        float noise = randomValue(band + timeStep * 17.0);
        float gate = step(0.58, noise) * remaining;
        float offset = (noise - 0.5) * 42.0 * gate;
        half4 color = chromaticSample(position + float2(offset, 0.0), (3.0 + noise * 13.0) * remaining);
        float dropout = step(0.08 + progress * 0.6, randomValue(band * 3.0 + timeStep));
        color *= half(max(dropout, smoothstep(0.48, 0.76, progress)));
        color.rgb *= half(0.9 + randomValue(timeStep + band) * 0.18);
        return color;
    }

    // 卡牌召唤：径向光环与流动的镭射高光落定在卡面上
    if (effect == 6) {
        float radius = length(centered * float2(1.0, 0.72));
        float aura = 1.0 - smoothstep(0.18 + progress * 0.34, 0.33 + progress * 0.38, radius);
        float shimmer = 1.0 - smoothstep(0.0, 0.06, abs(uv.x + uv.y * 0.35 - progress * 1.25));
        half4 color = chromaticSample(position, remaining * 5.0);
        color.rgb += half3(0.30, 0.18, 0.48) * half(aura * remaining * 0.65);
        color.rgb += half3(0.36, 0.28, 0.42) * half(shimmer * remaining * 0.55);
        color *= half(smoothstep(0.0, 0.32, progress));
        return color;
    }

    // 金色召唤：有机溶解配克制的暖色描边
    float easedProgress = smoothstep(0.0, 1.0, progress);
    float noise = layeredNoise(uv * float2(5.2, 4.1) + float2(progress * 0.16, -progress * 0.10));
    float radius = length(centered * float2(1.0, 0.72));
    float field = radius * 0.88 + noise * 0.26;
    float threshold = mix(0.03, 0.82, easedProgress);
    float edgeWidth = 0.045;
    float revealMask = 1.0 - smoothstep(threshold, threshold + edgeWidth, field);
    float edge = 1.0 - smoothstep(0.0, edgeWidth, abs(field - threshold));

    float2 refraction = float2(
        sin((uv.y + noise) * 21.0),
        cos((uv.x - noise) * 17.0)
    ) * edge * remaining * 2.4;
    half4 color = layer.eval(position + refraction);
    float luminance = dot(float3(color.rgb), float3(0.299, 0.587, 0.114));
    half3 imageAccent = accentColor.rgb;
    half3 highlightedAccent = mix(imageAccent, half3(1.0), half(0.34 + luminance * 0.28));
    color.rgb = mix(color.rgb, highlightedAccent, half(edge * 0.72));
    color.rgb += imageAccent * half(edge * 0.24);
    color *= half(max(revealMask, smoothstep(0.88, 1.0, progress)));
    return color;
}
"""
