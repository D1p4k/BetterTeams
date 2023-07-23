package com.booksaw.betterTeams.database.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import com.booksaw.betterTeams.Main;

/**
 * API Class to managing databases
 * 
 * @author booksaw
 *
 */
public class Database {

	/**
	 * Stores the information to create a connection to the database
	 */
	private String host;
	private int port;
	private String database;
	private String user;
	private String password;

	//Connection connection;
	HikariDataSource ds;

	/**
	 * Used to setup a connection from the provided data
	 * 
	 * @param section The configuration section which contains the database
	 *                information
	 */
	public void setupConnectionFromConfiguration(ConfigurationSection section) {

		host = section.getString("host", "localhost");
		port = section.getInt("port", 3306);
		database = section.getString("database", "spigot");
		user = section.getString("user", "root");
		password = section.getString("password", "password");

		setupConnection();

	}

	/**
	 * Used to setup a connection from the provided data
	 */
	public void setupConnection() {

		Bukkit.getLogger().info("[BetterTeams] Attempting to connect to database");

		try {
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false");
			config.setUsername(user);
			config.setPassword(password);

			ds = new HikariDataSource(config);
			//testDataSource();

		} catch (Exception e) {
			database = null;
			for (int i = 0; i < 3; i++) {
				Bukkit.getLogger().severe("[BetterTeams] ");
			}
			Bukkit.getLogger()
					.severe("[BetterTeams] Connection to the database could not be established, disabling plugin");

			Bukkit.getLogger().severe(
					"[BetterTeams] To use BetterTeams either change the storage type (config.yml/storageType) or correct the database credentials");

			for (int i = 0; i < 3; i++) {
				Bukkit.getLogger().severe("[BetterTeams] ");
			}

			e.printStackTrace();

			Main.plugin.getServer().getPluginManager().disablePlugin(Main.plugin);
			return;
		}

		Bukkit.getLogger().info("[BetterTeams] Connection with the database established");

	}

	/*
	 * This is a bit of a hacky fix - you should look into connection pooling as a
	 * more permanent solution. A popular library is HikariCP
	 * (https://github.com/brettwooldridge/HikariCP)
	 */
	/*private void resetConnection() {
		if (connection == null) {
			throw new IllegalStateException("No SQL connection has been established");
		}

		try {
			connection.close();
			// Also, just a suggestion, but it's not recommended to use autoReconnect=true
			// as per the MySQL Connector/J developer docs.
			// https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-connp-props-high-availability-and-clustering.html
			connection = DriverManager.getConnection(
					"jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false", user,
					password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}*/

	public Connection getConnection() {
		System.out.println("getConnection");
		try {
			return ds.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Used to test if the connection is valid
	 * 
	 * @throws SQLException If a connection cannot be established, this error will
	 *                      be thrown
	 */
	/*private void testDataSource() throws Exception {
		if (connection == null || connection.isClosed()) {
			throw new Exception("SQL connection is not setup correctly");
		}
	}*/

	/**
	 * Used to close the connection to the database
	 */
	public void closeConnection() {
		try {
			ds.close();
		} catch (Exception e) {
			Bukkit.getLogger().severe("[BetterTeams] There was an error closing the database connection");
			e.printStackTrace();
		}
	}

	/**
	 * Used to execute an SQL statement
	 * 
	 * @param statement    The SQL statement to execute
	 * @param placeholders The placeholders for the statement
	 */
	public void executeStatement(String statement, String... placeholders) {
		System.out.println("executeStatement");
		for (int i = 0; i < placeholders.length; i++) {
			statement = statement.replaceFirst("\\?", placeholders[i]);
		}

		statement = statement.replace("'false'", "false");
		statement = statement.replace("'true'", "true");
		try (PreparedStatement ps = getConnection().prepareStatement(statement)) {
//			System.out.println("executing: " + ps.toString());
			ps.executeUpdate();
			//Shouldn't have negative impact, just need to figure out why it is marked as redundant.
			ps.close();
		} catch (SQLException e) {
			Bukkit.getLogger().severe("Something went wrong while executing SQL");
			Bukkit.getLogger().severe("SQL: " + statement);
			e.printStackTrace();
		}
	}

	/**
	 * Used to execute an sql query
	 * 
	 * @param query        The query to execute
	 * @param placeholders The placeholders within that query
	 * @return The results of the query
	 */
	public PreparedStatement executeQuery(String query, String... placeholders) {
		System.out.println("executeQuery query:" + query);
		for (int i = 0; i < placeholders.length; i++) {
			System.out.println("placeholders[i]: " + placeholders[i]);
			query = query.replaceFirst("\\?", placeholders[i]);
		}

		try {
			PreparedStatement ps = getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
//			System.out.println("executing: " + ps.toString());
			return ps;
		} catch (SQLException e) {
			Bukkit.getLogger().severe("Something went wrong while executing SQL");
			Bukkit.getLogger().severe("SQL: " + query);
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * Used to create a table if the table does not currently exist
	 * 
	 * @param tableName The name of the table
	 * @param tableInfo The column information about the table
	 */
	public void createTableIfNotExists(String tableName, String tableInfo) {

		executeStatement("CREATE TABLE IF NOT EXISTS " + tableName + "(" + tableInfo + ");");

	}

}
