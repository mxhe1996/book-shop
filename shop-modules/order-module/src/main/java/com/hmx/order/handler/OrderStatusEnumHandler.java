package com.hmx.order.handler;

import com.hmx.order.enums.OrderStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;

@MappedTypes({OrderStatus.class})
@MappedJdbcTypes({JdbcType.INTEGER})
public class OrderStatusEnumHandler extends BaseTypeHandler<OrderStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, OrderStatus orderStatus, JdbcType jdbcType) throws SQLException {
        preparedStatement.setInt(i, orderStatus.getValue());
    }

    @Override
    public OrderStatus getNullableResult(ResultSet resultSet, String s) throws SQLException {
        return OrderStatus.find(resultSet.getString(s));
    }

    @Override
    public OrderStatus getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return OrderStatus.find(resultSet.getString(i));
    }

    @Override
    public OrderStatus getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return null;
    }
}
