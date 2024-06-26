/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.plugin.accumulo.serializers;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.airlift.slice.Slice;
import io.trino.plugin.accumulo.Types;
import io.trino.spi.TrinoException;
import io.trino.spi.block.Block;
import io.trino.spi.block.SqlMap;
import io.trino.spi.type.Type;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.hadoop.io.Text;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static io.trino.plugin.accumulo.io.AccumuloPageSink.ROW_ID_COLUMN;
import static io.trino.spi.StandardErrorCode.NOT_SUPPORTED;
import static io.trino.spi.type.BigintType.BIGINT;
import static io.trino.spi.type.BooleanType.BOOLEAN;
import static io.trino.spi.type.DateType.DATE;
import static io.trino.spi.type.DoubleType.DOUBLE;
import static io.trino.spi.type.IntegerType.INTEGER;
import static io.trino.spi.type.RealType.REAL;
import static io.trino.spi.type.SmallintType.SMALLINT;
import static io.trino.spi.type.TimeType.TIME_MILLIS;
import static io.trino.spi.type.TimestampType.TIMESTAMP_MILLIS;
import static io.trino.spi.type.TinyintType.TINYINT;
import static io.trino.spi.type.VarbinaryType.VARBINARY;
import static io.trino.spi.type.VarcharType.VARCHAR;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Implementation of {@link StringRowSerializer} that encodes and decodes Trino column values as human-readable String objects.
 */
public class StringRowSerializer
        implements AccumuloRowSerializer
{
    private final Table<String, String, String> familyQualifierColumnMap = HashBasedTable.create();
    private final Map<String, Object> columnValues = new HashMap<>();
    private final Text rowId = new Text();
    private final Text family = new Text();
    private final Text qualifier = new Text();
    private final Text value = new Text();

    private boolean rowOnly;
    private String rowIdName;

    @Override
    public void setRowIdName(String name)
    {
        this.rowIdName = name;
    }

    @Override
    public void setRowOnly(boolean rowOnly)
    {
        this.rowOnly = rowOnly;
    }

    @Override
    public void setMapping(String name, String family, String qualifier)
    {
        columnValues.put(name, null);
        familyQualifierColumnMap.put(family, qualifier, name);
    }

    @Override
    public void reset()
    {
        columnValues.clear();
    }

    @Override
    public void deserialize(Entry<Key, Value> entry)
    {
        if (!columnValues.containsKey(rowIdName)) {
            entry.getKey().getRow(rowId);
            columnValues.put(rowIdName, rowId.toString());
        }

        if (rowOnly) {
            return;
        }

        entry.getKey().getColumnFamily(family);
        entry.getKey().getColumnQualifier(qualifier);

        if (family.equals(ROW_ID_COLUMN) && qualifier.equals(ROW_ID_COLUMN)) {
            return;
        }

        value.set(entry.getValue().get());
        columnValues.put(familyQualifierColumnMap.get(family.toString(), qualifier.toString()), value.toString());
    }

    @Override
    public boolean isNull(String name)
    {
        return columnValues.get(name) == null;
    }

    @Override
    public Block getArray(String name, Type type)
    {
        throw new TrinoException(NOT_SUPPORTED, "arrays are not (yet?) supported for StringRowSerializer");
    }

    @Override
    public void setArray(Text text, Type type, Block block)
    {
        throw new TrinoException(NOT_SUPPORTED, "arrays are not (yet?) supported for StringRowSerializer");
    }

    @Override
    public boolean getBoolean(String name)
    {
        return Boolean.parseBoolean(getFieldValue(name));
    }

    @Override
    public void setBoolean(Text text, Boolean value)
    {
        text.set(value.toString().getBytes(UTF_8));
    }

    @Override
    public byte getByte(String name)
    {
        return Byte.parseByte(getFieldValue(name));
    }

    @Override
    public void setByte(Text text, Byte value)
    {
        text.set(value.toString().getBytes(UTF_8));
    }

    @Override
    public long getDate(String name)
    {
        return Long.parseLong(getFieldValue(name));
    }

    @Override
    public void setDate(Text text, long value)
    {
        text.set(Long.toString(value).getBytes(UTF_8));
    }

    @Override
    public double getDouble(String name)
    {
        return Double.parseDouble(getFieldValue(name));
    }

    @Override
    public void setDouble(Text text, Double value)
    {
        text.set(value.toString().getBytes(UTF_8));
    }

    @Override
    public float getFloat(String name)
    {
        return Float.parseFloat(getFieldValue(name));
    }

    @Override
    public void setFloat(Text text, Float value)
    {
        text.set(value.toString().getBytes(UTF_8));
    }

    @Override
    public int getInt(String name)
    {
        return Integer.parseInt(getFieldValue(name));
    }

    @Override
    public void setInt(Text text, Integer value)
    {
        text.set(value.toString().getBytes(UTF_8));
    }

    @Override
    public long getLong(String name)
    {
        return Long.parseLong(getFieldValue(name));
    }

    @Override
    public void setLong(Text text, Long value)
    {
        text.set(value.toString().getBytes(UTF_8));
    }

    @Override
    public SqlMap getMap(String name, Type type)
    {
        throw new TrinoException(NOT_SUPPORTED, "maps are not (yet?) supported for StringRowSerializer");
    }

    @Override
    public void setMap(Text text, Type type, SqlMap map)
    {
        throw new TrinoException(NOT_SUPPORTED, "maps are not (yet?) supported for StringRowSerializer");
    }

    @Override
    public short getShort(String name)
    {
        return Short.parseShort(getFieldValue(name));
    }

    @Override
    public void setShort(Text text, Short value)
    {
        text.set(value.toString().getBytes(UTF_8));
    }

    @Override
    public Time getTime(String name)
    {
        return new Time(Long.parseLong(getFieldValue(name)));
    }

    @Override
    public void setTime(Text text, Time value)
    {
        text.set(Long.toString(value.getTime()).getBytes(UTF_8));
    }

    @Override
    public Timestamp getTimestamp(String name)
    {
        return new Timestamp(Long.parseLong(getFieldValue(name)));
    }

    @Override
    public void setTimestamp(Text text, Timestamp value)
    {
        text.set(Long.toString(value.getTime()).getBytes(UTF_8));
    }

    @Override
    public byte[] getVarbinary(String name)
    {
        return getFieldValue(name).getBytes(UTF_8);
    }

    @Override
    public void setVarbinary(Text text, byte[] value)
    {
        text.set(value);
    }

    @Override
    public String getVarchar(String name)
    {
        return getFieldValue(name);
    }

    @Override
    public void setVarchar(Text text, String value)
    {
        text.set(value.getBytes(UTF_8));
    }

    private String getFieldValue(String name)
    {
        return columnValues.get(name).toString();
    }

    @Override
    public byte[] encode(Type type, Object value)
    {
        Text text = new Text();
        if (Types.isArrayType(type)) {
            throw new TrinoException(NOT_SUPPORTED, "arrays are not (yet?) supported for StringRowSerializer");
        }
        if (Types.isMapType(type)) {
            throw new TrinoException(NOT_SUPPORTED, "maps are not (yet?) supported for StringRowSerializer");
        }
        if (type.equals(BIGINT) && value instanceof Integer) {
            setLong(text, ((Integer) value).longValue());
        }
        else if (type.equals(BIGINT) && value instanceof Long) {
            setLong(text, (Long) value);
        }
        else if (type.equals(BOOLEAN)) {
            setBoolean(text, value.equals(Boolean.TRUE));
        }
        else if (type.equals(DATE)) {
            setDate(text, (long) value);
        }
        else if (type.equals(DOUBLE)) {
            setDouble(text, (Double) value);
        }
        else if (type.equals(INTEGER) && value instanceof Integer) {
            setInt(text, (Integer) value);
        }
        else if (type.equals(INTEGER) && value instanceof Long) {
            setInt(text, ((Long) value).intValue());
        }
        else if (type.equals(REAL)) {
            setFloat(text, (Float) value);
        }
        else if (type.equals(SMALLINT)) {
            setShort(text, (Short) value);
        }
        else if (type.equals(TIME_MILLIS)) {
            setTime(text, (Time) value);
        }
        else if (type.equals(TIMESTAMP_MILLIS)) {
            setTimestamp(text, (Timestamp) value);
        }
        else if (type.equals(TINYINT)) {
            setByte(text, (Byte) value);
        }
        else if (type.equals(VARBINARY) && value instanceof byte[]) {
            setVarbinary(text, (byte[]) value);
        }
        else if (type.equals(VARBINARY) && value instanceof Slice) {
            setVarbinary(text, ((Slice) value).getBytes());
        }
        else if (type.equals(VARCHAR) && value instanceof String) {
            setVarchar(text, ((String) value));
        }
        else if (type.equals(VARCHAR) && value instanceof Slice) {
            setVarchar(text, ((Slice) value).toStringUtf8());
        }
        else {
            throw new TrinoException(NOT_SUPPORTED, format("StringLexicoder does not support encoding type %s, object class is %s", type, value.getClass()));
        }

        return text.copyBytes();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T decode(Type type, byte[] value)
    {
        String strValue = new String(value, UTF_8);
        if (type.equals(BIGINT)) {
            return (T) (Long) Long.parseLong(strValue);
        }
        if (type.equals(BOOLEAN)) {
            return (T) (Boolean) Boolean.parseBoolean(strValue);
        }
        if (type.equals(DATE)) {
            return (T) (Long) Long.parseLong(strValue);
        }
        if (type.equals(DOUBLE)) {
            return (T) (Double) Double.parseDouble(strValue);
        }
        if (type.equals(INTEGER)) {
            return (T) (Long) ((Integer) Integer.parseInt(strValue)).longValue();
        }
        if (type.equals(REAL)) {
            return (T) (Double) ((Float) Float.parseFloat(strValue)).doubleValue();
        }
        if (type.equals(SMALLINT)) {
            return (T) (Long) ((Short) Short.parseShort(strValue)).longValue();
        }
        if (type.equals(TIME_MILLIS)) {
            return (T) (Long) Long.parseLong(strValue);
        }
        if (type.equals(TIMESTAMP_MILLIS)) {
            return (T) (Long) Long.parseLong(strValue);
        }
        if (type.equals(TINYINT)) {
            return (T) (Long) ((Byte) Byte.parseByte(strValue)).longValue();
        }
        if (type.equals(VARBINARY)) {
            return (T) value;
        }
        if (type.equals(VARCHAR)) {
            return (T) strValue;
        }
        throw new TrinoException(NOT_SUPPORTED, "Unsupported type " + type);
    }
}
