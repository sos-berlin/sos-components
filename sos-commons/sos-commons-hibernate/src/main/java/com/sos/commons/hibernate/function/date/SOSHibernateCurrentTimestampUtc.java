package com.sos.commons.hibernate.function.date;

import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.metamodel.model.domain.ReturnableType;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.type.StandardBasicTypes;

import com.sos.commons.hibernate.SOSHibernateFactory;

public class SOSHibernateCurrentTimestampUtc extends StandardSQLFunction {

    public static final String NAME = "SOS_CURRENT_TIMESTAMP_UTC";

    private SOSHibernateFactory factory;

    public SOSHibernateCurrentTimestampUtc(SOSHibernateFactory factory) {
        super(NAME, true, StandardBasicTypes.TIMESTAMP);
        this.factory = factory;
    }

    public static String getFunction() {
        return new StringBuilder(NAME).append("()").toString();
    }

    @Override
    public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> arguments, ReturnableType<?> returnType, SqlAstTranslator<?> translator)
            throws QueryException {
        sqlAppender.appendSql(factory.getCurrentTimestampUtcSqlExpression());
    }
}
