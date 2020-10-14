package org.yzh.framework.orm.fields;

import io.netty.buffer.ByteBuf;
import org.yzh.framework.orm.FieldMetadata;
import org.yzh.framework.orm.annotation.Field;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;

public class FieldString extends FieldMetadata<String> {
    private byte pad;
    private Charset charset;

    public FieldString(Field field, Method readMethod, Method writeMethod, Method lengthMethod) {
        super(field, readMethod, writeMethod, lengthMethod);
        this.pad = field.pad();
        this.charset = Charset.forName(field.charset());
    }

    @Override
    public String readValue(ByteBuf buf, int length) {
        int len = length > 0 ? length : buf.readableBytes();
        byte[] bytes = new byte[len];
        buf.readBytes(bytes);

        int st = 0;
        while ((st < len) && (bytes[st] == pad))
            st++;
        while ((st < len) && (bytes[len - 1] == pad))
            len--;
        return new String(bytes, st, len - st, charset);
    }

    @Override
    public void writeValue(ByteBuf buf, String value) {
        byte[] bytes = value.getBytes(charset);
        if (length > 0) {
            int srcPos = length - bytes.length;

            if (srcPos > 0) {
                byte[] pads = new byte[srcPos];
                if (pad != 0x00)
                    Arrays.fill(pads, pad);
                buf.writeBytes(pads);
                buf.writeBytes(bytes);
            } else if (srcPos < 0) {
                buf.writeBytes(bytes, -srcPos, length);
                log.warn("字符长度超出限制: {}长度[{}],数据长度[{}],{}", desc, length, bytes.length, value);
            } else {
                buf.writeBytes(bytes);
            }
        } else {
            buf.writeBytes(bytes);
        }
    }
}