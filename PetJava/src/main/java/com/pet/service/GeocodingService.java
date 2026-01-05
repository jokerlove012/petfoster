package com.pet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;

/**
 * 地理编码服务 - 使用高德地图 Web 服务 API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final ObjectMapper objectMapper;
    
    // 高德地图 Web 服务 API Key（需要在高德开放平台申请 Web 服务类型的 Key）
    @Value("${amap.web-service-key:fdb01183188d0621e5c3b70249c65983}")
    private String amapKey;

    /**
     * 地址转坐标
     * @param address 地址字符串
     * @return 经纬度数组 [longitude, latitude]，失败返回 null
     */
    public BigDecimal[] geocode(String address) {
        if (address == null || address.trim().isEmpty()) {
            return null;
        }

        try {
            URI uri = UriComponentsBuilder
                .fromHttpUrl("https://restapi.amap.com/v3/geocode/geo")
                .queryParam("key", amapKey)
                .queryParam("address", address)
                .build()
                .encode()
                .toUri();
            
            log.info("Geocode request URL: {}", uri.toString());

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(uri, String.class);
            
            log.info("Geocode response: {}", response);
            
            JsonNode root = objectMapper.readTree(response);
            String status = root.path("status").asText();
            
            if ("1".equals(status)) {
                JsonNode geocodes = root.path("geocodes");
                if (geocodes.isArray() && geocodes.size() > 0) {
                    String location = geocodes.get(0).path("location").asText();
                    String[] parts = location.split(",");
                    if (parts.length == 2) {
                        BigDecimal longitude = new BigDecimal(parts[0]);
                        BigDecimal latitude = new BigDecimal(parts[1]);
                        log.info("Geocode success: {} -> [{}, {}]", address, longitude, latitude);
                        return new BigDecimal[]{longitude, latitude};
                    }
                }
            } else {
                String info = root.path("info").asText();
                log.warn("Geocode failed for '{}': {}", address, info);
            }
        } catch (Exception e) {
            log.error("Geocode error for '{}': {}", address, e.getMessage());
        }

        return null;
    }
}
