package com.steven.solomon.enums;

import com.steven.solomon.convert.ColumnConvert;
import com.steven.solomon.convert.impl.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public enum ConvertEnums {

    BIT_CONVERT(Types.BIT,new BooleanColumnConvert()),
    BOOLEAN_CONVERT(Types.BOOLEAN, new BooleanColumnConvert()),

    TINYINT_CONVERT(Types.TINYINT,new ByteColumnConvert()),

    FLOAT_CONVERT(Types.FLOAT,new FloatColumnConvert()),
    REAL_CONVERT(Types.REAL,new FloatColumnConvert()),

    SMALLINT_CONVERT(Types.SMALLINT,new ShortColumnConvert()),

    INTEGER_CONVERT(Types.INTEGER,new IntegerColumnConvert()),

    BIGINT_CONVERT(Types.BIGINT,new LongColumnConvert()),

    DOUBLE_CONVERT(Types.DOUBLE,new DoubleColumnConvert()),

    NUMERIC_CONVERT(Types.NUMERIC,new BigDecimalColumnConvert()),
    DECIMAL_CONVERT(Types.DECIMAL,new BigDecimalColumnConvert()),

    DATE_CONVERT(Types.DATE,new DateColumnConvert()),

    TIMESTAMP_CONVERT(Types.TIMESTAMP, new LocalDateTimeColumnConvert()),

    BINARY_CONVERT(Types.BINARY, new BytesColumnConvert()),
    VARBINARY_CONVERT(Types.VARBINARY, new BytesColumnConvert()),
    LONG_VARBINARY_CONVERT(Types.LONGVARBINARY, new BytesColumnConvert()),

    CHAR_CONVERT(Types.CHAR,new StringColumnConvert()),
    VARCHAR_CONVERT(Types.VARCHAR,new StringColumnConvert()),
    LONG_VARCHAR_CONVERT(Types.LONGVARCHAR,new StringColumnConvert()),
    BLOB_CONVERT(Types.BLOB, new StringColumnConvert()),
    CLOB_CONVERT(Types.CLOB, new StringColumnConvert()),
    NCHAR_CONVERT(Types.NCHAR, new StringColumnConvert()),
    NVARCHAR_CONVERT(Types.NVARCHAR, new StringColumnConvert()),
    LONG_NVARCHAR_CONVERT(Types.LONGNVARCHAR, new StringColumnConvert()),
    ;

    private final Integer columnType;

    private final ColumnConvert<?> convert;

    ConvertEnums(Integer columnType, ColumnConvert<?> convert) {
        this.columnType = columnType;
        this.convert = convert;
    }

    public Integer getColumnType() {
        return columnType;
    }

    public ColumnConvert<?> getConvert() {
        return convert;
    }
}
