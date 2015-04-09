package northwind.service;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.IntegerType;

/**
 * Allows null version column by fixing the next() method.
 * Don't use this if you can make your version column not-null.
 */
public class NullableIntVersionType extends IntegerType {
    private static final long serialVersionUID = 1L;

    @Override
    public Integer next(Integer current, SessionImplementor session) {
        if (current == null) current = 0;
        return super.next(current, session);
    }

}
