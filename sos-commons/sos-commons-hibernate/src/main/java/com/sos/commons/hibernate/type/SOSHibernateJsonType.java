package com.sos.commons.hibernate.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import com.sos.commons.hibernate.SOSHibernate.Dbms;
import com.sos.commons.hibernate.configuration.resolver.SOSHibernateFinalPropertiesResolver;

/** Column type:<br />
 * H2 (MySQL Mode) - JSON<br />
 * MSSQL (from 2016)- NVARCHAR(n)<br />
 * MYSQL (from 5.7.8) - JSON<br />
 * ORACLE (from 12.1.0.2)- CLOB<br />
 * PGSQL (from 9.4.2) -JSONB<br />
 */
public class SOSHibernateJsonType implements UserType<Object> {

    public static final String COLUMN_TRANSFORMER_WRITE_DEFAULT = "?";
    public static final String COLUMN_TRANSFORMER_WRITE_H2 = "? FORMAT JSON";

    private final int sqlType = Types.JAVA_OBJECT;
    private Dbms dbms;

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException,
            SQLException {
        if (dbms == null) {
            dbms = SOSHibernateFinalPropertiesResolver.retrieveDbms(session.getFactory());
        }
        if (value == null) {
            if (Dbms.ORACLE.equals(dbms)) {
                st.setNull(index, Types.CLOB);
            } else {
                st.setNull(index, Types.OTHER);
            }
            return;
        }
        if (Dbms.PGSQL.equals(dbms)) {
            st.setObject(index, value, Types.OTHER);
        } else {
            st.setObject(index, value);
        }
    }

    @Override
    public Object nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws HibernateException,
            SQLException {
        String val = rs.getString(position);
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
    public int getSqlType() {
        return sqlType;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
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