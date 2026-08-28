package com.leaf.skiller.util;

/**
 * Configuration for entity outline appearance.
 * 实体轮廓外观配置。
 * <p>
 * This class defines the visual properties of entity outlines, including color and transparency.
 * 此类定义实体轮廓的视觉属性，包括颜色和透明度。
 * </p>
 *
 * @param red Red color component (0.0-1.0)
 *            红色分量 (0.0-1.0)
 * @param green Green color component (0.0-1.0)
 *              绿色分量 (0.0-1.0)
 * @param blue Blue color component (0.0-1.0)
 *             蓝色分量 (0.0-1.0)
 * @param alpha Alpha transparency component (0.0-1.0)
 *              Alpha透明度分量 (0.0-1.0)
 * @since 1.0.0
 */
public record ColorConfig(float red, float green, float blue, float alpha) {

    /**
     * Creates a new outline configuration with the specified color values.
     * 使用指定的颜色值创建新的轮廓配置。
     *
     * @param red Red color component (0.0-1.0) - 红色分量 (0.0-1.0)
     * @param green Green color component (0.0-1.0) - 绿色分量 (0.0-1.0)
     * @param blue Blue color component (0.0-1.0) - 蓝色分量 (0.0-1.0)
     * @param alpha Alpha transparency component (0.0-1.0) - Alpha透明度分量 (0.0-1.0)
     * @throws IllegalArgumentException if any color value is outside 0.0-1.0 range
     *                                    如果任何颜色值超出0.0-1.0范围
     * @since 1.0.0
     */
    public ColorConfig {
        if (red < 0.0f || red > 1.0f) throw new IllegalArgumentException("Red must be between 0.0 and 1.0");
        if (green < 0.0f || green > 1.0f) throw new IllegalArgumentException("Green must be between 0.0 and 1.0");
        if (blue < 0.0f || blue > 1.0f) throw new IllegalArgumentException("Blue must be between 0.0 and 1.0");
        if (alpha < 0.0f || alpha > 1.0f) throw new IllegalArgumentException("Alpha must be between 0.0 and 1.0");
    }

    /**
     * Creates a red outline configuration.
     * 创建红色轮廓配置。
     */
    public static ColorConfig redConfig() {
        return new ColorConfig(1.0f, 0.0f, 0.0f, 1.0f);
    }

    /**
     * Creates a green outline configuration.
     * 创建绿色轮廓配置。
     */
    public static ColorConfig greenConfig() {
        return new ColorConfig(0.0f, 1.0f, 0.0f, 1.0f);
    }

    /**
     * Creates a blue outline configuration.
     * 创建蓝色轮廓配置。
     */
    public static ColorConfig blueConfig() {
        return new ColorConfig(0.0f, 0.0f, 1.0f, 1.0f);
    }

    /**
     * Creates a white outline configuration.
     * 创建白色轮廓配置。
     */
    public static ColorConfig whiteConfig() {
        return new ColorConfig(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
