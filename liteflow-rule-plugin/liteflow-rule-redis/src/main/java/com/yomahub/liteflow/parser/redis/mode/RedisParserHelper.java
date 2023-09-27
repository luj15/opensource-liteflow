package com.yomahub.liteflow.parser.redis.mode;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.StrUtil;
import com.yomahub.liteflow.builder.LiteFlowNodeBuilder;
import com.yomahub.liteflow.enums.NodeTypeEnum;
import com.yomahub.liteflow.log.LFLog;
import com.yomahub.liteflow.log.LFLoggerManager;
import com.yomahub.liteflow.parser.helper.NodeConvertHelper;
import com.yomahub.liteflow.parser.redis.vo.RedisParserVO;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;

/**
 * Redis 解析器通用接口
 *
 * @author hxinyu
 * @since 2.11.0
 */

public interface RedisParserHelper {

    LFLog LOG = LFLoggerManager.getLogger(RedisParserHelper.class);

    String SINGLE_REDIS_URL_PATTERN = "redis://{}:{}";

    String SENTINEL_REDIS_URL_PATTERN = "redis://{}";

    String CHAIN_XML_PATTERN = "<chain name=\"{}\">{}</chain>";

    String NODE_XML_PATTERN = "<nodes>{}</nodes>";

    String NODE_ITEM_XML_PATTERN = "<node id=\"{}\" name=\"{}\" type=\"{}\"><![CDATA[{}]]></node>";

    String NODE_ITEM_WITH_LANGUAGE_XML_PATTERN = "<node id=\"{}\" name=\"{}\" type=\"{}\" language=\"{}\"><![CDATA[{}]]></node>";

    String XML_PATTERN = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><flow>{}{}</flow>";

    String getContent();

    void listenRedis();


    /**
     * 获取Redisson客户端的Config配置通用方法(单点模式)
     * @param redisParserVO redisParserVO
     * @param dataBase redis连接的数据库号
     * @return redisson config
     */
    default Config getSingleRedissonConfig(RedisParserVO redisParserVO, Integer dataBase) {
        Config config = new Config();
        String redisAddress = StrFormatter.format(SINGLE_REDIS_URL_PATTERN, redisParserVO.getHost(), redisParserVO.getPort());
        //如果配置了用户名和密码
        if (StrUtil.isNotBlank(redisParserVO.getUsername()) && StrUtil.isNotBlank(redisParserVO.getPassword())) {
            config.useSingleServer().setAddress(redisAddress)
                    .setUsername(redisParserVO.getUsername())
                    .setPassword(redisParserVO.getPassword())
                    .setDatabase(dataBase);
        }
        //如果配置了密码
        else if (StrUtil.isNotBlank(redisParserVO.getPassword())) {
            config.useSingleServer().setAddress(redisAddress)
                    .setPassword(redisParserVO.getPassword())
                    .setDatabase(dataBase);
        }
        //没有配置密码
        else {
            config.useSingleServer().setAddress(redisAddress)
                    .setDatabase(dataBase);
        }
        return config;
    }

    /**
     * 获取Redisson客户端的Config配置通用方法(哨兵模式)
     * @param redisParserVO redisParserVO
     * @param dataBase redis连接的数据库号
     * @return redisson Config
     */
    default Config getSentinelRedissonConfig(RedisParserVO redisParserVO, Integer dataBase) {
        Config config = new Config();
        SentinelServersConfig sentinelConfig = config.useSentinelServers()
                .setMasterName(redisParserVO.getMasterName());
        redisParserVO.getSentinelAddress().forEach(address -> {
            sentinelConfig.addSentinelAddress(StrFormatter.format(SENTINEL_REDIS_URL_PATTERN, address));
        });
        //如果配置了用户名和密码
        if(StrUtil.isNotBlank(redisParserVO.getUsername()) && StrUtil.isNotBlank(redisParserVO.getPassword())) {
            sentinelConfig.setUsername(redisParserVO.getUsername())
                    .setPassword(redisParserVO.getPassword())
                    .setDatabase(dataBase);
        }
        //如果配置了密码
        else if(StrUtil.isNotBlank(redisParserVO.getPassword())) {
            sentinelConfig.setPassword(redisParserVO.getPassword())
                    .setDatabase(dataBase);
        }
        //没有配置密码
        else {
            sentinelConfig.setDatabase(dataBase);
        }
        return config;
    }

    /**
     * script节点的修改/添加
     *
     * @param scriptFieldValue 新的script名
     * @param newValue         新的script值
     */
    static void changeScriptNode(String scriptFieldValue, String newValue) {
        NodeConvertHelper.NodeSimpleVO nodeSimpleVO = NodeConvertHelper.convert(scriptFieldValue);
        // 有语言类型
        if (StrUtil.isNotBlank(nodeSimpleVO.getLanguage())) {
            LiteFlowNodeBuilder.createScriptNode()
                    .setId(nodeSimpleVO.getNodeId())
                    .setType(NodeTypeEnum.getEnumByCode(nodeSimpleVO.getType()))
                    .setName(nodeSimpleVO.getName())
                    .setScript(newValue)
                    .setLanguage(nodeSimpleVO.getLanguage())
                    .build();
        }
        // 没有语言类型
        else {
            LiteFlowNodeBuilder.createScriptNode()
                    .setId(nodeSimpleVO.getNodeId())
                    .setType(NodeTypeEnum.getEnumByCode(nodeSimpleVO.getType()))
                    .setName(nodeSimpleVO.getName())
                    .setScript(newValue)
                    .build();
        }
    }
}
