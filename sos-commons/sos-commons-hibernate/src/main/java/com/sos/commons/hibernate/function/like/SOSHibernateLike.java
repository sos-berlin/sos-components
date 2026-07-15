package com.sos.commons.hibernate.function.like;

import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.metamodel.model.domain.ReturnableType;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.type.StandardBasicTypes;

import com.sos.commons.hibernate.SOSHibernateFactory;

/** Hibernate SQL function that implements a portable LIKE expression with explicit ESCAPE handling.
 *
 * <p>
 * This function renders a SQL LIKE predicate in the form:
 * </p>
 *
 * <pre>
 *     &lt;expression&gt; LIKE &lt;pattern&gt; ESCAPE '\'
 * </pre>
 *
 * <p>
 * It is intended to be used with pre-escaped input patterns where wildcard characters have been converted according to application rules (e.g. glob-to-SQL
 * conversion).
 * </p>
 *
 * <h2>Usage in HQL</h2>
 * 
 * <pre>
 *     where sos_like(lower(e.name), :pattern)
 * </pre>
 *
 * <h2>Expected parameter format</h2> The second argument must already be escaped:
 * <ul>
 * <li>{@code \} -> {@code \\} (escaped backslash)</li>
 * <li>{@code _} -> {@code \_} (escaped literal underscore)</li>
 * <li>{@code %} -> {@code \%} (escaped literal percent sign)</li>
 * <li>{@code *} -> {@code %} (SQL wildcard)</li>
 * <li>{@code ?} -> {@code _} (single character wildcard)</li>
 * </ul>
 *
 * <h2>SQL generation</h2>
 * 
 * <pre>
 * lower(name) like ? escape '\'
 * </pre>
 *
 * <h2>Supported databases</h2> This function is compatible with:
 * <ul>
 * <li>MySQL</li>
 * <li>PostgreSQL</li>
 * <li>Oracle Database</li>
 * <li>Microsoft SQL Server</li>
 * </ul>
 *
 * <h2>Important notes</h2>
 * <ul>
 * <li>This function does NOT perform escaping itself.</li>
 * <li>Escaping must be handled by application code.</li>
 * <li>Only SQL LIKE rendering is provided.</li>
 * </ul>
 */
public class SOSHibernateLike extends StandardSQLFunction {

    public static final String NAME = "SOS_LIKE";

    private SOSHibernateFactory factory;

    public SOSHibernateLike(SOSHibernateFactory factory) {
        super(NAME, StandardBasicTypes.BOOLEAN);
        this.factory = factory;
    }

    @Override
    public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> arguments, ReturnableType<?> returnType, SqlAstTranslator<?> translator)
            throws QueryException {
        if (arguments == null || arguments.size() < 2) {
            throw new QueryException("missing arguments", null, null);
        }
        arguments.get(0).accept(translator);
        sqlAppender.appendSql(" like ");
        arguments.get(1).accept(translator);
        switch (this.factory.getDbms()) {
        case MYSQL:
            sqlAppender.appendSql(" escape '\\\\'");
            break;
        default:
            sqlAppender.appendSql(" escape '\\'");
            break;
        }
    }
}
