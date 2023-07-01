package com.yomahub.liteflow.parser.redis;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.StrUtil;
import com.yomahub.liteflow.parser.el.ClassXmlFlowELParser;
import com.yomahub.liteflow.parser.redis.exception.RedisException;
import com.yomahub.liteflow.parser.redis.vo.RedisParserVO;
import com.yomahub.liteflow.property.LiteflowConfig;
import com.yomahub.liteflow.property.LiteflowConfigGetter;
import com.yomahub.liteflow.util.JsonUtil;

import java.util.Objects;

public class RedisXmlELParser extends ClassXmlFlowELParser {

    private static final String ERROR_COMMON_MSG = "ruleSourceExtData or map is empty";

    private static final String ERROR_MSG_PATTERN = "uleSourceExtData {} is blank";

    public RedisXmlELParser() {
        LiteflowConfig liteflowConfig = LiteflowConfigGetter.get();

        try{
            RedisParserVO redisParserVO = null;
            if (MapUtil.isNotEmpty((liteflowConfig.getRuleSourceExtDataMap()))) {
                redisParserVO = BeanUtil.toBean(liteflowConfig.getRuleSourceExtDataMap(),
                        RedisParserVO.class, CopyOptions.create());
            }
            else if (StrUtil.isNotBlank(liteflowConfig.getRuleSourceExtData())) {
                redisParserVO = JsonUtil.parseObject(liteflowConfig.getRuleSourceExtData(), RedisParserVO.class);
            }
            if (Objects.isNull(redisParserVO)) {
                throw new RedisException(ERROR_COMMON_MSG);
            }

            //检查配置文件
            checkParserVO(redisParserVO);
        }
        catch (RedisException redisException){
            throw redisException;
        }
        catch (Exception e){
            throw new RedisException(e.getMessage());
        }
    }

    @Override
    public String parseCustom() {
        //todo
        return null;
    }

    private void checkParserVO(RedisParserVO redisParserVO) {
        if (StrUtil.isEmpty(redisParserVO.getHost())){
            throw new RedisException(StrFormatter.format(ERROR_MSG_PATTERN, "host"));
        }
        if (StrUtil.isEmpty(redisParserVO.getPort())){
            throw new RedisException(StrFormatter.format(ERROR_MSG_PATTERN, "port"));
        }
    }
}
