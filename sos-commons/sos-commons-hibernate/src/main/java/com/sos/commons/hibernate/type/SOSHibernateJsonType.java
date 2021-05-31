package com.sos.commons.hibernate.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import com.sos.commons.hibernate.SOSHibernateFactory;

/** Column type:<br />
 * H2 (MySQL Mode) - JSON<br />
 * MSSQL (from 2016)- NVARCHAR(n)<br />
 * MYSQL (from 5.7.8) - JSON<br />
 * ORACLE (from 12.1.0.2)- CLOB<br />
 * PGSQL (from 9.4.2) -JSONB<br />
 */
public class SOSHibernateJsonType implements UserType {

    public static final String COLUMN_TRANSFORMER_WRITE_DEFAULT = "?";
    public static final String COLUMN_TRANSFORMER_WRITE_H2 = "? FORMAT JSON";
    public static final String TYPE_NAME = "sos_json";

    private final int[] sqlTypes = new int[] { Types.JAVA_OBJECT };
    private Enum<SOSHibernateFactory.Dbms> dbms;

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException,
            SQLException {
        if (dbms == null) {
            dbms = SOSHibernateFactory.getDbms(session.getFactory().getJdbcServices().getDialect());
        }
        if (value == null) {
            if (dbms.equals(SOSHibernateFactory.Dbms.ORACLE)) {
                st.setNull(index, Types.CLOB);
            } else {
                st.setNull(index, Types.OTHER);
            }
            return;
        }
        if (dbms.equals(SOSHibernateFactory.Dbms.PGSQL)) {
            st.setObject(index, value, Types.OTHER);
        } else {
            st.setObject(index, value);
        }
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException,
            SQLException {
        String val = rs.getString(names[0]);
        if (val == null) {
            return null;
        }
        return val;
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (String) this.deepCopy(value);
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return this.deepCopy(cached);
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    @Override
    public int[] sqlTypes() {
        return sqlTypes;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class returnedClass() {
        return String.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == null) {
            return y == null;
        }
        return x.equals(y);
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

}