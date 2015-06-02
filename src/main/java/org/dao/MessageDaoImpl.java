package org.dao;

import org.apache.log4j.Logger;
import org.db.ConnectionManager;
import org.model.InfoMessage;
import sun.java2d.d3d.D3DRenderQueue;

import javax.sound.midi.MidiDevice;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by HP on 25.05.2015.
 */
public class MessageDaoImpl implements MessageDao{
    private Logger logger = Logger.getLogger(MessageDaoImpl.class.getName());
    private Lock lock = new ReentrantLock();
    private Object obj = new Object();
    @Override
    public void add(InfoMessage message) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            synchronized (obj) {
                connection = ConnectionManager.getConnection();
                connection.setAutoCommit(false);
                System.out.println(message.getID());
                preparedStatement = connection.prepareStatement("INSERT INTO messages (id, text, name, request) VALUES (?, ?, ?, ?)");
                preparedStatement.setInt(1, message.getID());
                preparedStatement.setString(2, message.getText());
                preparedStatement.setString(3, message.getNameUser());
                preparedStatement.setString(4, message.getRequst());
                preparedStatement.executeUpdate();
                connection.commit();
            }
        } catch (SQLException e) {
            connection.rollback();
            logger.error(e);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    @Override
    public void update(InfoMessage message) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = ConnectionManager.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement("Update     messages SET text = ?, request = ? WHERE id = ?");
            preparedStatement.setString(1, message.getText());
            preparedStatement.setString(2, message.getRequst());
            preparedStatement.setInt(3, message.getID());
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }

    }

    @Override
    public void delete(InfoMessage message) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = ConnectionManager.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement("Update     messages SET text = ?, request = ? WHERE id = ?");
            preparedStatement.setString(1, message.getText());
            preparedStatement.setString(2, message.getRequst());
            preparedStatement.setInt(3, message.getID());
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    @Override
    public InfoMessage selectById(InfoMessage message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<InfoMessage> selectAll() {
        List<InfoMessage> messages = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = ConnectionManager.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM messages");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String text = resultSet.getString("text");
                String request = resultSet.getString("request");
                String name =  resultSet.getString("name");
                messages.add(new InfoMessage(id, name, text, request));
            }
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
        return messages;

    }
}
