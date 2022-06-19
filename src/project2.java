/*
Name: Evan Hausman
Course: CNT 4714 Summer 2022
Assignment title: Project 2 � A Two-tier Client-Server Application
Date: June 26, 2022
Class: CNT4714 Summer Section 001
*/

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.miginfocom.swing.MigLayout;

public class project2 extends JFrame
{
	private static final String OPLOG_FILENAME = "operationslog.properties";
	private ResultSetTableModel tableModel;
	private ResultSetTableModel operationsLogModel;
	private JTextArea queryField;
	private JTextArea connectionStatus;
	private JTable resultTable;
	private String propertiesFileName;
	private String query;
	private Properties properties;
	
    public project2() throws IOException
    {
    	// General Setup
    	super("SQL Query App Version 1 - (CNT417 - Summer 2022 - Project 2)");
    	query = "";
    	
    	// Establish a connection to the operations log
 	    properties = new Properties();
 	    properties.load(new FileInputStream(OPLOG_FILENAME));
 	    try {
			operationsLogModel = new ResultSetTableModel(OPLOG_FILENAME);
 	    }
			catch ( ClassNotFoundException classNotFound ) 
            {
         	   connectionStatus.setText("Connection failed. Invalid drivers.");  
            }
            catch ( SQLException dbError ) 
            {
         	   connectionStatus.setText("Connection failed. Database error.");  
            }
 	    
        JPanel connectFrame = new JPanel();
        JPanel queryFrame = new JPanel();
        JPanel resultFrame = new JPanel();
        
        getContentPane().setLayout(new MigLayout("", "[][]", "[][][]"));
        connectFrame.setLayout(new MigLayout("", "[]7[]", "[]7[]7[]7[]7[]"));
        queryFrame.setLayout(new MigLayout("", "[]15[]", "[]15[]15[]"));
        resultFrame.setLayout(new MigLayout("", "[]", "[15][][][]"));
       
        
        // Connect To Database Field
        JButton connectButton = new JButton("Connect to Database");
        JPasswordField passBox = new JPasswordField();
        JTextField userBox = new JTextField();
        JComboBox<String> propertiesDropDown = new JComboBox<String>();
        
        // Trims path from filename and adds to propertiesDropDown menu
        findPropFiles().forEach(s -> propertiesDropDown.addItem(s.substring(s.lastIndexOf(File.separator)+1)));
        
        JLabel connectionDetailsLabel = new JLabel("Connection Details", SwingConstants.CENTER);
        JLabel usernameLabel = new JLabel("Username:", SwingConstants.CENTER);
        JLabel passwordLabel = new JLabel("Password:", SwingConstants.CENTER);
        JLabel propertiesLabel = new JLabel("Properties File:", SwingConstants.CENTER);
        
        
        // Cosmetics for the Connection Details section
        userBox.setPreferredSize(new Dimension(120, 20));
        passBox.setPreferredSize(new Dimension(120,20));
        propertiesDropDown.setPreferredSize(new Dimension(120,20));
        connectionDetailsLabel.setFont(new Font("Verdana", Font.BOLD, 14));
        connectionDetailsLabel.setForeground(Color.BLUE);
        
        // Cosmetics for the labels
        usernameLabel.setOpaque(true); 
        passwordLabel.setOpaque(true); 
        propertiesLabel.setOpaque(true);
        usernameLabel.setPreferredSize(new Dimension(100, 20)); 
        passwordLabel.setPreferredSize(new Dimension(100, 20)); 
        propertiesLabel.setPreferredSize(new Dimension(100, 20)); 
        usernameLabel.setBackground(Color.LIGHT_GRAY); 
        passwordLabel.setBackground(Color.LIGHT_GRAY); 
        propertiesLabel.setBackground(Color.LIGHT_GRAY);
        
        
        // Positioning for the Connection Details section
        connectFrame.add(connectionDetailsLabel, "spanx 2, alignx center, wrap");
        connectFrame.add(propertiesLabel);
        connectFrame.add(propertiesDropDown, "wrap");
        connectFrame.add(usernameLabel);
        connectFrame.add(userBox, "wrap");
        connectFrame.add(passwordLabel);
        connectFrame.add(passBox, "wrap");
        connectFrame.add(connectButton, "spanx 2,alignx center,wrap");
        
        
        // Query Field
        JLabel enterCommandLabel = new JLabel("Enter an SQL Command", SwingConstants.CENTER);
        queryField = new JTextArea();
        JScrollPane queryPane = new JScrollPane(queryField);
        JButton clearCommandButton = new JButton("Clear SQL Command");
        JButton executeCommandButton = new JButton("Execute SQL Command");
        
        enterCommandLabel.setFont(new Font("Verdana", Font.BOLD, 14));
        enterCommandLabel.setForeground(Color.BLUE);
        queryField.setWrapStyleWord(true);
        queryField.setLineWrap(true);
        queryPane.setPreferredSize(new Dimension(400,200));
        
        queryFrame.add(enterCommandLabel, "spanx 2, wrap");
        queryFrame.add(queryPane, "spanx 2, wrap");
        queryFrame.add(clearCommandButton);
        queryFrame.add(executeCommandButton);
        
        // Connection Status Field
        connectionStatus = new JTextArea("No Connection");
        connectionStatus.setOpaque(true);
        connectionStatus.setBackground(Color.BLACK);
        connectionStatus.setForeground(Color.RED);
        connectionStatus.setFont(new Font("Verdana", Font.BOLD, 14));
        connectionStatus.setPreferredSize(new Dimension(600, 20));
        connectionStatus.setEditable(false);
        connectionStatus.setWrapStyleWord(true);
        
        // Results Field
        JLabel resultLabel = new JLabel("SQL Execution Results Window", SwingConstants.CENTER);
        resultTable = new JTable();
        JButton resultClearButton = new JButton("Clear Results");
        JScrollPane resultPane = new JScrollPane(resultTable);
        
        resultLabel.setFont(new Font("Verdana", Font.BOLD, 14));
        resultLabel.setForeground(Color.BLUE);
        resultTable.setGridColor(Color.BLACK);
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        resultFrame.add(resultLabel, "cell 0 0");
        resultFrame.add(resultPane, "cell 0 1");
        resultFrame.add(resultClearButton, "cell 0 2");
        
        
        // General Cleanup
        getContentPane().add(connectFrame);
        getContentPane().add(queryFrame, "wrap");
        getContentPane().add(connectionStatus, "spanx 2,alignx left,wrap");
        getContentPane().add(resultFrame, "spanx 2,alignx left,wrap");
        
        
        // Listeners
        connectButton.addActionListener(
    		new ActionListener()
    		{
    			public void actionPerformed( ActionEvent event )
                {
                   try 
                   {
                	   properties = new Properties();
                	   
                	   propertiesFileName = (String) propertiesDropDown.getSelectedItem();

                	   properties.load(new FileInputStream(propertiesFileName));
                	   
                	   if (!properties.getProperty("MYSQL_DB_USERNAME").equals(userBox.getText()) || !properties.getProperty("MYSQL_DB_PASSWORD").equals(new String(passBox.getPassword())))
                	   {
                		   connectionStatus.setText("Connection failed. Invalid credentials.");
                		   return;
                	   }
                	   connectionStatus.setText("Connecting...");
                	   tableModel = new ResultSetTableModel(propertiesFileName);
                	   connectionStatus.setText("Successful connection to:\n" + properties.getProperty("MYSQL_DB_URL"));
                	   
                   }
                   catch ( ClassNotFoundException classNotFound ) 
                   {
                	   connectionStatus.setText("Connection failed. Invalid drivers.");  
                   }
                   catch ( SQLException dbError ) 
                   {
                	   connectionStatus.setText("Connection failed. Database error.");  
                   }
                   catch ( IOException classNotFound ) 
                   {
                	   connectionStatus.setText("Connection failed. Error when reading " + propertiesFileName);  
                   }
                   
                }
    		}
    	);
        clearCommandButton.addActionListener(
        	new ActionListener()
        	{
        		public void actionPerformed( ActionEvent event )
                {
        			queryField.setText("");
                }
        	}
        );
        
        executeCommandButton.addActionListener(
            new ActionListener()
            	{
            		public void actionPerformed( ActionEvent event )
                    {
            			try {
            				// Update num_queries in operationslog database
            				operationsLogModel.setUpdate("UPDATE operationscount SET num_queries = num_queries + 1");
            				
            				if (tableModel == null)
        					{
        						JOptionPane.showMessageDialog( null, "You are not connected to a database.", "Database error", JOptionPane.ERROR_MESSAGE );
        						return;
            				}
            				query = queryField.getText();
            				
            				if (query.trim().toLowerCase().startsWith("select"))
            				{
            					tableModel.setQuery(query);
            					resultTable.setModel(tableModel);
            				}
            				else
            					tableModel.setUpdate(query);
							
							// Update num_updates in operationslog database
							operationsLogModel.setUpdate("UPDATE operationscount SET num_updates = num_updates + 1");
						} 
            			catch (IllegalStateException e) {
            				JOptionPane.showMessageDialog( null, e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE );
						}
            			catch (SQLException e) {
            				JOptionPane.showMessageDialog( null, e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE );
            			}
                    }
            	}
        );
        resultClearButton.addActionListener(
            new ActionListener()
            	{
            		public void actionPerformed( ActionEvent event )
                    {
            			resultTable.setModel(new DefaultTableModel());
                    }
            	}
        );
    	
    	// Final cleanup
    	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 600);
        setVisible(true); 
        
        // Closes the connection when the application closes
        addWindowListener(new WindowAdapter() 
           {
              public void windowClosed(WindowEvent event)
              {
            	 if (tableModel != null)
            		 tableModel.disconnectFromDatabase();
                 System.exit(0);
              }
           }
        );
        
        
        
     	}
    
    // Inspired by Mkyong's examples on java's file-walk functionality
    private static List<String> findPropFiles() throws IOException
    {
    	List<String> result = null;
    	
    	try (Stream<Path> walk = Files.walk(Paths.get(System.getProperty("user.dir")))) {
    		result = walk.filter(p -> !Files.isDirectory(p))
                    .map(p -> p.toString().toLowerCase())
                    .filter(f -> f.endsWith(".properties"))
                    .filter(f -> !f.contains(OPLOG_FILENAME))
                    .collect(Collectors.toList());
    	}
    	catch (Exception e) {e.printStackTrace();}
    	return result;
    }
    
    public static void main(String [] args) throws IOException
    {
        new project2();
    }
}