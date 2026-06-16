package com.steven.solomon.gateway.gray;

import cn.hutool.core.util.StrUtil;

import com.steven.solomon.gateway.core.TokenClaims;
import com.steven.solomon.gateway.properties.GatewayGrayProperties;
import java.util.Objects;

/**
 * 灰度版本选择器。
 *
 * <p>按租户编码、用户标识和请求路径做稳定哈希分桶，决定当前请求走稳定版还是候选版。
 * 相同输入始终产出相同结果，保证用户在一次会话中始终命中同一版本，避免体验割裂。</p>
 *
 * <p>分桶逻辑：{@code hash(tenantCode + userId + path) % 100 < candidateWeight} 时走候选版，
 * 否则走稳定版。{@code candidateWeight=100} 表示全部走候选版。</p>
 *
 * @author steven
 */
public class GrayReleaseSelector {

    private final GatewayGrayProperties properties;

    /**
     * 构造灰度选择器。
     *
     * @param properties 灰度配置
     * @throws IllegalArgumentException candidateWeight 不在 0-100 范围
     */
    public GrayReleaseSelector(GatewayGrayProperties properties) {
        properties.validate();
        this.properties = properties;
    }

    /**
     * 选择当前请求应走的版本。
     *
     * @param claims 令牌声明（含租户和用户标识）；公开路径可为 null
     * @param path   请求路径
     * @return 候选版本标识或稳定版本标识
     */
    public String selectVersion(TokenClaims claims, String path) {
        if (!properties.isEnabled()) {
            return properties.getStableVersion();
        }
        String tenantCode = claims != null ? claims.tenantCode() : "";
        String userId = claims != null ? claims.userId() : "";
        // 稳定哈希：拼接身份和路径，确保同一用户同一接口始终命中相同版本
        String key = tenantCode + ":" + userId + ":" + StrUtil.nullToEmpty(path);
        int bucket = Math.floorMod(Objects.hash(key), 100);
        if (bucket < properties.getCandidateWeight()) {
            return properties.getCandidateVersion();
        }
        return properties.getStableVersion();
    }

    /**
     * 返回当前是否启用灰度。
     *
     * @return true 表示灰度已开启
     */
    public boolean isEnabled() {
        return properties.isEnabled();
    }
}
