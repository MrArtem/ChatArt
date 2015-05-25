package org.dao;

import org.model.InfoMessage;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by HP on 20.05.2015.
 */
public interface MessageDao {
    void add(InfoMessage message) throws SQLException;
    void update(InfoMessage message);
    void delete(InfoMessage message);
    InfoMessage selectById(InfoMessage message);
    List<InfoMessage> selectAll();
}
