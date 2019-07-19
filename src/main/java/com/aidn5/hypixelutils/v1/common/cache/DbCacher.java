
package com.aidn5.hypixelutils.v1.common.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.aidn5.hypixelutils.v1.common.annotation.IHelpTools;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;

/**
 * Cacher uses Database to cache data.
 * 
 * <p>
 * <b>Note: </b>Database library is required to use this class
 * 
 * @author aidn5
 *
 * @category ICacher
 * 
 * @since 1.0
 * @version 1.0
 * 
 * @see ICacher
 */
@IHypixelUtils
@IHelpTools
public class DbCacher implements ICacher<String, String> {

  private static final String QUERY_CREATE_TABLE;

  static {
    String createTable;
    createTable = "CREATE TABLE IF NOT EXISTS cache (";
    createTable += "key TEXT NOT NULL PRIMARY KEY UNIQUE,";
    createTable += "value TEXT,";
    createTable += "time INTEGER)"; // Unix timestamp

    QUERY_CREATE_TABLE = createTable;
  }

  private static final String QUERY_ADD_CONTENTS;

  static {
    String createTable;
    createTable = "INSERT OR REPLACE INTO cache (";
    createTable += "key, value, time)";
    createTable += " VALUES (?,?,?)";

    QUERY_ADD_CONTENTS = createTable;
  }

  private final Connection connection;
  private final int duration;
  private final TimeUnit durationUnit;

  /**
   * Create an instance and prepare the connection for use.
   * 
   * @param connection
   *          the connection to the database to use.
   * @param duration
   *          the length of time after an entry is created
   *          that it should be removed
   * @param durationUnit
   *          the unit that {@code duration} is expressed in
   * 
   * @throws SQLException
   *           if an error occurs while creating the table.
   */
  public DbCacher(Connection connection, int duration, TimeUnit durationUnit) throws SQLException {
    this.connection = connection;
    this.duration = duration;
    this.durationUnit = durationUnit;

    initDB();
  }

  private void initDB() throws SQLException {
    Statement statement = connection.createStatement();
    statement.setQueryTimeout(15);
    statement.execute(QUERY_CREATE_TABLE);
    statement.close();
  }

  @Override
  @Nullable
  public synchronized CachedSet<String, String> getCacheByKey(@Nonnull String key) {
    try {
      Objects.requireNonNull(key);

      PreparedStatement st = connection.prepareStatement("SELECT * FROM cache WHERE key = ?");
      st.setString(1, key);


      ResultSet rs = st.executeQuery();
      if (rs.first()) {
        // resource will automatically be released.
        // no need to close it
        return new CachedSet<>(key, rs.getString("value"), rs.getLong("time"));
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return null;
  }

  @Override
  @Nonnull
  public synchronized Iterable<CachedSet<String, String>> getCacheByValue(String value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public synchronized void clearCache() {
    try {
      Statement st = connection.createStatement();
      st.executeQuery("DELETE FROM cache");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public synchronized void cleanCache() {
    try {
      Statement st = connection.createStatement();

      double lastTime = (int) (System.currentTimeMillis() - durationUnit.toMillis(duration));
      st.executeQuery("DELETE FROM cache WHERE time < " + lastTime);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void loadCache() {
    // this is DB.
    // all data is already ready to be retrieved from the disk
    // without any further loading and parsing
  }

  @Override
  public void saveCache() {
    // this is DB.
    // all data is already saved to the disk
  }

  @Override
  public synchronized void cacheNewSet(String key, String value) {
    try {
      PreparedStatement st = connection.prepareStatement(QUERY_ADD_CONTENTS);

      st.setString(1, key);
      st.setString(2, value);
      st.setInt(3, (int) System.currentTimeMillis());

      st.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

  }
}
