package com.gaguraczi.paw.global.persistence;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.usertype.UserType;
import org.postgresql.geometric.PGpoint;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;


// PostgreSQL 전용 공간데이터타입 Point 사용을 위한 정의 클래스
public class PGpointUserType implements UserType<PGpoint> {

    public static final PGpointUserType INSTANCE = new PGpointUserType();

    /**
     * Identifies the SQL type used to store the PostgreSQL point value.
     *
     * @return the {@link Types#OTHER} SQL type code
     */
    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    /**
     * Identifies the PostgreSQL point type handled by this user type.
     *
     * @return the {@link PGpoint} class
     */
    @Override
    public Class<PGpoint> returnedClass() {
        return PGpoint.class;
    }

    /**
     * Determines whether two PostgreSQL points have identical coordinates.
     *
     * @param x the first point
     * @param y the second point
     * @return {@code true} if both points are null or have equal coordinates, {@code false} otherwise
     */
    @Override
    public boolean equals(PGpoint x, PGpoint y) {
        if (x == null && y == null) return true;
        if (x == null || y == null) return false;
        return Double.compare(x.x, y.x) == 0 && Double.compare(x.y, y.y) == 0;
    }

    /**
     * Computes a hash code from the point's coordinates.
     *
     * @param x the point to hash
     * @return 0 for a null point; otherwise, a hash code based on its coordinates
     */
    @Override
    public int hashCode(PGpoint x) {
        return x == null ? 0 : Objects.hash(x.x, x.y);
    }

    /**
     * Reads a PostgreSQL point value from the specified result-set column.
     *
     * @param rs       the result set containing the value
     * @param position the column position
     * @param options  the wrapper options
     * @return the point value, or {@code null} if the column is SQL {@code NULL}
     * @throws SQLException if the value cannot be read from the result set
     */
    @Override
    public PGpoint nullSafeGet(ResultSet rs, int position,
                               WrapperOptions options) throws SQLException {
        Object obj = rs.getObject(position);
        if (obj == null || rs.wasNull()) return null;
        if (obj instanceof PGpoint p) return p;
        return new PGpoint(obj.toString());
    }

    /**
     * Binds a PostgreSQL point value to a prepared statement.
     *
     * @param st      the prepared statement
     * @param value   the point value to bind, or {@code null}
     * @param index   the parameter index
     * @param options the Hibernate wrapper options
     * @throws SQLException if the value cannot be bound
     */
    @Override
    public void nullSafeSet(PreparedStatement st, PGpoint value, int index,
                            WrapperOptions options) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            st.setObject(index, value, Types.OTHER);
        }
    }

    /**
     * Creates an independent copy of a PostgreSQL point value.
     *
     * @param value the point to copy
     * @return a copy of the point, or {@code null} if the value is {@code null}
     */
    @Override
    public PGpoint deepCopy(PGpoint value) {
        if (value == null) return null;
        return new PGpoint(value.x, value.y);
    }

    /**
     * Indicates that mapped point values may be modified.
     *
     * @return {@code true}
     */
    @Override
    public boolean isMutable() {
        return true;
    }

    /**
     * Creates a detached copy of a point for caching.
     *
     * @param value the point to disassemble
     * @return a copied point, or {@code null} if {@code value} is {@code null}
     */
    @Override
    public Serializable disassemble(PGpoint value) {
        return deepCopy(value);
    }

    /**
     * Reconstructs a point from its cached representation.
     *
     * @param cached the cached point value
     * @param owner  the entity owning the value
     * @return a copy of the cached point, or {@code null} if the cached value is {@code null}
     */
    @Override
    public PGpoint assemble(Serializable cached, Object owner) {
        return deepCopy((PGpoint) cached);
    }
}
