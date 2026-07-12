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

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<PGpoint> returnedClass() {
        return PGpoint.class;
    }

    @Override
    public boolean equals(PGpoint x, PGpoint y) {
        if (x == null && y == null) return true;
        if (x == null || y == null) return false;
        return Double.compare(x.x, y.x) == 0 && Double.compare(x.y, y.y) == 0;
    }

    @Override
    public int hashCode(PGpoint x) {
        return x == null ? 0 : Objects.hash(x.x, x.y);
    }

    @Override
    public PGpoint nullSafeGet(ResultSet rs, int position,
                               WrapperOptions options) throws SQLException {
        Object obj = rs.getObject(position);
        if (obj == null || rs.wasNull()) return null;
        if (obj instanceof PGpoint p) return p;
        return new PGpoint(obj.toString());
    }

    @Override
    public void nullSafeSet(PreparedStatement st, PGpoint value, int index,
                            WrapperOptions options) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            st.setObject(index, value, Types.OTHER);
        }
    }

    @Override
    public PGpoint deepCopy(PGpoint value) {
        if (value == null) return null;
        return new PGpoint(value.x, value.y);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(PGpoint value) {
        return deepCopy(value);
    }

    @Override
    public PGpoint assemble(Serializable cached, Object owner) {
        return deepCopy((PGpoint) cached);
    }
}
