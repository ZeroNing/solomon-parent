package com.steven.solomon.serializer;

import com.steven.solomon.verification.ValidateUtils;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;


public class BaseRedisSerializer implements RedisSerializer<Object> {

	@Override
    public byte[] serialize(Object t) throws SerializationException {
        if (ValidateUtils.isEmpty(t)) {
            return new byte[0];
        }
        return SerializeUtil.serialize(t);
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (ValidateUtils.isEmpty(bytes)) {
            return null;
        }
        return SerializeUtil.unserialize(bytes);
    }

}
