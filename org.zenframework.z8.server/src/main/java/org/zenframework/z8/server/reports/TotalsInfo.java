package org.zenframework.z8.server.reports;

import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.math;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class TotalsInfo {
    Aggregation m_aggregationType = Aggregation.None;

    private primary m_value;
    private int m_count;

    public TotalsInfo(Aggregation aggregationType) {
        m_aggregationType = aggregationType;
    }

    public String format(IValue abstractValue) {
        if(m_aggregationType == Aggregation.Average && m_count > 0) {
            if(abstractValue.type() == FieldType.Decimal) {
                m_value = ((decimal)m_value).operatorDiv(new integer(m_count));
            }
            else if(abstractValue.type() == FieldType.Integer) {
                m_value = ((integer)m_value).operatorDiv(new integer(m_count));
            }
            if(abstractValue.type() == FieldType.Datespan) {
                m_value = new datespan(((datespan)m_value).z8_milliseconds().operatorDiv(new integer(m_count)).get());
            }
        }

        return m_value == null ? "" : m_value.toString();
    }

    public void update(Field field) {
        if(m_aggregationType == Aggregation.Count) {
            m_value = new integer(m_value == null ? 1 : ((integer)m_value).get() + 1);
        }

        if(m_aggregationType == Aggregation.Sum || m_aggregationType == Aggregation.Average) {
            if(field.type() == FieldType.Decimal) {
                assert (m_value == null || m_value instanceof decimal);

                if(m_value == null) {
                    m_value = new decimal();
                }

                m_value = ((decimal)m_value).operatorAdd((decimal)field.get());
            }
            else if(field.type() == FieldType.Decimal) {
                assert (m_value == null || m_value instanceof integer);

                if(m_value == null) {
                    m_value = new integer();
                }

                m_value = ((integer)m_value).operatorAdd((integer)field.get());
            }
            else if(field.type() == FieldType.Datespan) {
                assert (m_value == null || m_value instanceof datespan);

                if(m_value == null) {
                    m_value = new datespan();
                }

                m_value = ((datespan)m_value).operatorAdd((datespan)field.get());
            }

            m_count++;

        }
        else if(m_aggregationType == Aggregation.Min) {
            if(m_value == null) {
                m_value = field.get();
            }
            else if(field.type() == FieldType.Decimal) {
                m_value = math.z8_min((decimal)m_value, (decimal)field.get());
            }
            else if(field.type() == FieldType.Integer) {
                m_value = math.z8_min((integer)m_value, (integer)field.get());
            }
            else if(field.type() == FieldType.Date) {
                m_value = math.z8_min((date)m_value, (date)field.get());
            }
            else if(field.type() == FieldType.Datetime) {
                m_value = math.z8_min((datetime)m_value, (datetime)field.get());
            }
            else if(field.type() == FieldType.Datespan) {
                m_value = math.z8_min((datespan)m_value, (datespan)field.get());
            }
            else if(field.type() == FieldType.String) {
                m_value = math.z8_min((string)m_value, (string)field.get());
            }
        }
        else if(m_aggregationType == Aggregation.Max) {
            if(m_value == null) {
                m_value = field.get();
            }
            else if(field.type() == FieldType.Decimal) {
                m_value = math.z8_max((decimal)m_value, (decimal)field.get());
            }
            else if(field.type() == FieldType.Integer) {
                m_value = math.z8_max((integer)m_value, (integer)field.get());
            }
            else if(field.type() == FieldType.Date) {
                m_value = math.z8_max((date)m_value, (date)field.get());
            }
            else if(field.type() == FieldType.Datetime) {
                m_value = math.z8_max((datetime)m_value, (datetime)field.get());
            }
            else if(field.type() == FieldType.Datespan) {
                m_value = math.z8_max((datespan)m_value, (datespan)field.get());
            }
            else if(field.type() == FieldType.String) {
                m_value = math.z8_max((string)m_value, (string)field.get());
            }
        }

    }
};
