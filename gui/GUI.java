import java.sql.*;
import java.util.ArrayList;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Scanner;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.util.Arrays;
import java.util.List;
import java.util.HashMap;

// TO RUN::
// compile with 'javac *.java'
// run with 'java -cp ".;postgresql-42.2.8.jar" GUI'

public class GUI extends JFrame implements ActionListener {
    static JFrame f;
    static JFrame manager_frame;
    static JFrame employee_frame;
    static JFrame inventory_frame;    static Integer num_drinks = 0;
    static double total_cost = 0.0;
    static boolean paid = false;

    // drink list per order
    static ArrayList<String> order_drinks = new ArrayList<>();

    static ArrayList<String> selected_items = new ArrayList<>();

    // all customizations per order
    static ArrayList<String> order_customizations = new ArrayList<>();

    // all ingredients per order
    static ArrayList<String> order_ingredients = new ArrayList<>();

    // drink names
    static ArrayList<String> drink_names = null;


    static JFrame drinks_frame;
    static JFrame reports_frame;
    static JTextField text_input;
    static JTextArea text_output;
    static JTextField text_input_inventory;
    static JTextArea text_output_inventory;
    static JTextField update_text_input;
    static JTextArea update_text_output;
    static JTextField update_input_inventory;
    static JTextArea update_output_inventory;
    static JTextArea out;
    static JTable table_menu;
    static JTable table_inventory;
    static JFrame add_menu;
    static JFrame add_inventory;
    static JFrame update_menu;
    static JFrame update_inventory;
    static JPanel p_inventory;
    static JPanel p_menu;
    static JPanel p_reports;
    static Boolean menu_check = false;
    static Boolean inventory_check = false;
    static JTextField ingredients;
    static JTextArea ingredient_out;


   /*
   Creates a database connection using JDBC to connect to a PostgreSQL database.
   @return A Connection object representing the database connection.
   */
    
    public static Connection createConnection() {
      Connection conn = null;
      try {
        conn = DriverManager.getConnection(
          "jdbc:postgresql://csce-315-db.engr.tamu.edu/csce315331_08r_db",
          "csce315_971_cevancura",
          "password");
      } catch (Exception e) {
        e.printStackTrace();
        System.err.println(e.getClass().getName()+": "+e.getMessage());
        System.exit(0);
      }
      return conn;
    }

    /*
   Closes a database connection.
   @param conn a Connection name
   @return A Connection object representing the database connection.
   */
    public static void closeConnection(Connection conn) {
      try {
        conn.close();
        JOptionPane.showMessageDialog(null,"Connection Closed.");
      } catch(Exception e) {
        JOptionPane.showMessageDialog(null,"Connection NOT Closed.");
      }
    }

    /*
    @param button_name The button name, a panel JPanel and a GUI s
    @return None, void function
    */

    public static void implementButton(String button_name, JPanel panel, GUI s) {
      JButton button = new JButton(button_name);
      panel.add(button);
      button.addActionListener(s);
    }

    /*
    @param splitted Array of String, formatting An integer for different cases
    @return drink_name 
    */

    public static String getDrinkName(String[] splitted, int formatting) {
      int splitted_length = splitted.length;
      String drink_name = "";
      for (int i = 1; i < splitted_length - formatting; ++i) {
          drink_name += splitted[i];

          if (i != splitted_length - formatting - 1){

              drink_name+= " ";

          }
      }
      return drink_name;
    }

    /*
    For adding or updating the menu or inventory
    @param text_in, JTextField for entering the text, text_out JTextArea for displaying the input text, conn A connection to the database, is_add a Boolean to check if we're adding or updating, is_menu A boolean to check if if we're dealing with menu or inventory
    @return None, void function
    */

    public static void dataFeature(JTextField text_in, JTextArea text_out, JTextField ingredients, JTextArea ingredient_out, Connection conn, Boolean is_add, Boolean is_menu) {
      text_out.setText(text_in.getText());
      text_in.setText("enter the text");
      ingredient_out.setText(ingredients.getText());
      ingredients.setText("enter ingredient ID for the drink");

      String update = "";
      String ingredient_string = "";
      ingredient_string = ingredient_out.getText();
      String add_ingredient_to_menu = "";

      String text = "Inventory";
      if (is_menu) {text = "Menu";}
      int formatting = 3;
      if (is_menu) {formatting = 1;}

      if (!(text_out.getText().equals("") || text_out.getText().equals("enter the text"))) {
        try{
            
            Statement stmt = conn.createStatement();
            String[] splitted = text_out.getText().split("\\s+");
            
            if (is_add) {
              if (is_menu) { 
                update = "INSERT INTO drink_dictionary (drink_id, name, price, ingredients) VALUES";
              }
              else {update = "INSERT INTO inventory (product_id, itemname, total_amount, current_amount, restock) VALUES";}
            }
            else {
              if(is_menu) { update = "UPDATE drink_dictionary SET name = \'"; }
              else {update = "UPDATE inventory SET itemname = \'";}
            }
            
            int splitted_length = splitted.length;
            String drink_name = getDrinkName(splitted, formatting);
            if (is_add) {
              if(is_menu) { 
                update += " (\'" + splitted[0] + "\', \'" + drink_name + "\', " + splitted[splitted_length -1] + ", \'" + ingredient_string + "\');";
              }
              else { update += " (" + splitted[0] + ", \'" + drink_name + "\', " + splitted[splitted_length -3] + ", " + splitted[splitted_length -2] + ", \'" + splitted[splitted_length -1] + "\');"; }
            }
            else {
              if(is_menu) { update += drink_name + "\', price = " + splitted[splitted_length -1] + "WHERE drink_id = \'" + splitted[0] + "\';";}
              else {update += drink_name + "\', total_amount = " + splitted[splitted_length -3] + ", current_amount = " + splitted[splitted_length -2] +  ", restock = \'" + splitted[splitted_length-1]+ "\' WHERE product_id = " + splitted[0] + ";";}
            }
            
            stmt.execute(update);
            if(is_add && is_menu){
              stmt.execute(add_ingredient_to_menu);
            }
            out.setText("The " + text + " has been updated to " + text_out.getText());
            
            }catch (Exception n){
              n.printStackTrace();
              System.err.println(n.getClass().getName()+": "+n.getMessage());
              JOptionPane.showMessageDialog(null,"Error executing command.");
            }
      }
      updateTable(conn);
    }


    /*
    Updates the Table when the items are added or updated
    @param conn A connection to the database
    @return None, void function
    */

    public static void updateTable(Connection conn) {
      
      if (menu_check) {
        Component[] component_list_menu = p_menu.getComponents();

        //Loop through the components
        for(Component c : component_list_menu){

            //Find the components to remove
            if(!(c instanceof JButton)){

                //Remove it
                p_menu.remove(c);
            }
        }
        p_menu.revalidate();
        p_menu.repaint();
      }
      if(inventory_check) {
        Component[] component_list = p_inventory.getComponents();

        //Loop through the components
        for(Component c : component_list){

          //Find the components to remove
          if(!(c instanceof JButton)){

              //Remove it
              p_inventory.remove(c);
          }
        }
        p_inventory.revalidate();
        p_inventory.repaint();
      }

      String[] inventory_cols = {"product_id", "itemname", "total_amount", "current_amount", "restock"};
      String[] menu_cols = {"drink_id", "name", "price"};
      ArrayList<ArrayList<String>> data_inventory = new ArrayList<>();
      ArrayList<ArrayList<String>> data_menu = new ArrayList<>();
      
      try{
        //create a statement object
        Statement stmt = conn.createStatement();
        if (inventory_check) {
          //create a SQL statement
          String sql_statement_m = "SELECT * FROM inventory;";
          ResultSet result = stmt.executeQuery(sql_statement_m);
          while (result.next()) {
            ArrayList<String> curr = new ArrayList<>();
            curr.add(result.getString("product_id"));
            curr.add(result.getString("itemname"));
            curr.add(result.getString("total_amount"));
            curr.add(result.getString("current_amount"));
            curr.add(result.getString("restock"));

            data_inventory.add(curr);          
          }
          String[][] arr = data_inventory.stream().map(l -> l.stream().toArray(String[]::new)).toArray(String[][]::new);
          table_inventory = new JTable(arr, inventory_cols);
          table_inventory.setBounds(30,40,200,500);
          JScrollPane sp = new JScrollPane(table_inventory);
          inventory_frame.getContentPane().add(sp);
          p_inventory.add(sp);

          p_inventory.revalidate();
          p_inventory.repaint();
        }
        if (menu_check) {
          //getting menu items from drinks dictionary
          String menu_command = "SELECT * FROM drink_dictionary;";
          ResultSet menuresult = stmt.executeQuery(menu_command);
          while (menuresult.next()) {          
            ArrayList<String> curr = new ArrayList<>();
            curr.add(menuresult.getString("drink_id"));
            curr.add(menuresult.getString("name"));
            curr.add(menuresult.getString("price"));

            data_menu.add(curr); 
          }

          String[][] arr_menu = data_menu.stream().map(l -> l.stream().toArray(String[]::new)).toArray(String[][]::new);
          table_menu = new JTable(arr_menu, menu_cols);
          table_menu.setBounds(30,40,400,500);
          JScrollPane sp_menu = new JScrollPane(table_menu);
          drinks_frame.getContentPane().add(sp_menu);
          p_menu.add(sp_menu);

          p_menu.revalidate();
          p_menu.repaint();
        }
      } catch (Exception e){
        e.printStackTrace();
        System.err.println(e.getClass().getName()+": "+e.getMessage());
        JOptionPane.showMessageDialog(null,"Error accessing drink and inventory.");
      }
    }

    /*
    Checks inventory levels to see if restock needed or not
    @param conn A connection to the database
    @return None, void function
    */
    public static void checkInventoryLevels(Connection conn) {
      // for each item in inventory get current data
      ArrayList<ArrayList<String>> inventory_list = new ArrayList<ArrayList<String>>();

      //create a statement object
      try {
        Statement stmt = conn.createStatement();
        //create a SQL statement
        String sql_statement = "SELECT * FROM inventory ORDER BY product_id asc;";
        //send statement to DBMS
        ResultSet result = stmt.executeQuery(sql_statement);
        while (result.next()) {
          ArrayList<String> single_item = new ArrayList<String>();

          single_item.add(result.getString("product_id"));
          single_item.add(result.getString("total_amount"));
          single_item.add(result.getString("current_amount"));
          single_item.add(result.getString("restock"));

          // make list of lists with all id, total, and current amount included for each item
          inventory_list.add(single_item);
        }
      } catch (Exception e){
        JOptionPane.showMessageDialog(null,"Error accessing Database.");
      }

      // update values
      for (ArrayList<String> item : inventory_list) {
        //create a SQL statement
        String sql_statement = "UPDATE inventory";
        sql_statement += " SET restock = ";
        
        if (Float.valueOf(item.get(2)) < Float.valueOf(item.get(1))) {
          // if current amount < needed amount update restock to "t"
          sql_statement += "true";
        }
        else {
          // update restock to "f"
          sql_statement += "false";
        }

        sql_statement += " WHERE product_id = ";
        sql_statement += item.get(0);
        sql_statement += ";";

        try{
          //create a statement object
          Statement stmt = conn.createStatement();
          //send statement to DBMS
          stmt.execute(sql_statement);
        } catch (Exception e){
          JOptionPane.showMessageDialog(null,"Error accessing Database.");
        }
      }
    }

/*
    Main function that opens and closes database
    @param args A string array
    @return None, void function
    */
    public static void main(String[] args)
    {
      Connection conn = createConnection();
      JOptionPane.showMessageDialog(null,"Opened database successfully");
      String name = "";
      // create a new frame
      f = new JFrame("DB GUI");
      // close connection when click "x" button
      f.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

      // check inventory to start
      checkInventoryLevels(conn);

      manager_frame = new JFrame("Manager GUI");
      employee_frame = new JFrame("Employee GUI");

      // create a object
      GUI s = new GUI();

      // create a panel
      JPanel p = new JPanel();

      //creating buttons for the main page
      JButton b = new JButton("Close");
      JButton manager = new JButton("Manager");
      JButton employee = new JButton("Employee");

      // add actionlistener to button
      b.addActionListener(s);
      manager.addActionListener(s);
      employee.addActionListener(s);
      
      JTextArea text = new JTextArea(name);
      p.add(text);
      p.add(b);
      p.add(manager);
      p.add(employee);
      // add panel to frame
      f.add(p);
      f.setSize(400, 400);
      f.setVisible(true);

      // manager frame
      manager_frame.setSize(400, 400);
      JPanel p_man = new JPanel();


      implementButton("Menu", p_man, s);
      implementButton("Inventory", p_man, s);

      // add reports button
      implementButton("Reports", p_man, s);


      inventory_frame = new JFrame("Inventory Window");
      drinks_frame = new JFrame("Drinks Window");

      try {
        reports_frame = reportsWindow(conn);
      } 
      catch (Exception e){
        JOptionPane.showMessageDialog(null,"Error accessing Database.");
      }

      p_inventory = new JPanel();
      p_menu = new JPanel();
      p_reports = new JPanel();

      implementButton("Add Menu Item", p_menu, s);
      implementButton("Add Inventory Item", p_inventory, s);

      //creating a pop up for when the user wants to add or update iteam 
      add_menu = new JFrame("Add Item Frame");
      JPanel p_add_menu = new JPanel();
      add_inventory = new JFrame("Add Item Inventory Frame");
      JPanel p_add_inventory = new JPanel();
      update_menu = new JFrame("Update Menu Item");
      JPanel p_update_menu = new JPanel();
      update_inventory= new JFrame("Update Inventory Item");
      JPanel p_update_inventory = new JPanel();


      add_menu.setSize(200, 200);
      add_menu.add(p_add_menu);
      add_inventory.setSize(200, 200);
      add_inventory.add(p_add_inventory);
      update_menu.setSize(200, 200);
      update_menu.add(p_update_menu);
      update_inventory.setSize(200, 200);
      update_inventory.add(p_update_inventory);

      implementButton("Save Menu Item", p_add_menu, s);
      implementButton("Save Updates for Menu Item", p_update_menu, s);
      implementButton("Save Inventory Item", p_add_inventory, s);
      implementButton("Save Updates for Inventory Item", p_update_inventory, s);

      
      
      
      //inserting into the database
      
      text_input = new JTextField("enter the text");
      text_output = new JTextArea("");
      ingredients = new JTextField("enter ingredient ID for item: ");
      ingredient_out = new JTextArea("");
      p_add_menu.add(text_input);
      p_add_menu.add(text_output);
      p_add_menu.add(ingredients);
      p_add_menu.add(ingredient_out);


      text_input_inventory = new JTextField("enter the text");
      text_output_inventory = new JTextArea("");
      p_add_inventory.add(text_input_inventory);
      p_add_inventory.add(text_output_inventory);


      implementButton("Update Menu", p_menu, s);
      implementButton("Update Inventory", p_inventory, s);


      //text area for UPDATES to menu and inventory 
      update_text_input = new JTextField("enter the text");
      update_text_output = new JTextArea("");
      p_update_menu.add(update_text_input);
      p_update_menu.add(update_text_output);


      update_input_inventory = new JTextField("enter the text");
      update_output_inventory = new JTextArea("");
      p_update_inventory.add(update_input_inventory);
      p_update_inventory.add(update_output_inventory);

      menu_check = true;
      inventory_check = true;
      updateTable(conn);
      menu_check = false;
      inventory_check = false;

      //output changes
      out = new JTextArea();
      p_menu.add(out);

      inventory_frame.add(p_inventory);
      inventory_frame.setSize(800, 800);
      drinks_frame.add(p_menu);
      drinks_frame.setSize(800, 800);

      manager_frame.add(p_man);

      employee_frame.setSize(800, 800);

      JPanel p_emplo = new JPanel(new GridLayout(2, 4));

      JButton milk_tea = new JButton("Milk Tea");
      JButton brewed_tea = new JButton("Brewed Tea");
      JButton fruit_tea = new JButton("Fruit Tea");
      JButton fresh_milk = new JButton("Fresh Milk");
      JButton ice_blended = new JButton("Ice Blended");
      JButton tea_mojito = new JButton("Tea Mojito");
      JButton creama = new JButton("Creama");

      //seasonal item
      JButton seasonal = new JButton("Seasonal");
      // JButton customizations = new JButton("Customizations");
      JButton employee_exit = new JButton("Employee Exit");
      employee_exit.setBackground(Color.GRAY);
      employee_exit.setOpaque(true);

      JButton cancel_order = new JButton("Cancel Order");
      cancel_order.setBackground(Color.RED);
      cancel_order.setOpaque(true);

      JButton order = new JButton("View Order");
      order.setBackground(Color.GREEN);
      order.setOpaque(true);
      // order.setBorderPainted(false);

      p_emplo.add(milk_tea);
      p_emplo.add(brewed_tea);
      p_emplo.add(fruit_tea);
      p_emplo.add(fresh_milk);
      p_emplo.add(ice_blended);
      p_emplo.add(tea_mojito);
      p_emplo.add(creama);
      //seasonal
      p_emplo.add(seasonal);
      // p_emplo.add(customizations);
      p_emplo.add(employee_exit);
      p_emplo.add(cancel_order);
      p_emplo.add(order);

      
      

      milk_tea.addActionListener(s);
      brewed_tea.addActionListener(s);
      fruit_tea.addActionListener(s);
      fresh_milk.addActionListener(s);
      ice_blended.addActionListener(s);
      tea_mojito.addActionListener(s);
      creama.addActionListener(s);
      // customizations.addActionListener(s);
      employee_exit.addActionListener(s);
      cancel_order.addActionListener(s);
      order.addActionListener(s);
      seasonal.addActionListener(s);

      employee_frame.add(p_emplo);


      // update drink array
      try {
        drink_names = getDrinkNamesTable(conn);
      }
      catch (IOException error1) {
        error1.printStackTrace();
      }

      while (f.isDisplayable()) {
        if (paid) {
          // update inventory
          updateInventoryTable(conn);

          // get current order number (next after max)
          String prev_order_id_str = "";
          Integer current_order_id_int = 0;
          String current_order_id_str = "";
          try{
            //create a statement object
            Statement stmt = conn.createStatement();
            //create a SQL statement
            String sql_statement = "SELECT MAX(order_id) FROM order_history;";
            //send statement to DBMS
            ResultSet result = stmt.executeQuery(sql_statement);
            if (result.next()) {
              prev_order_id_str += result.getString("max");
            }
            try {
              current_order_id_int = Integer.parseInt(prev_order_id_str) + 1;
              current_order_id_str = String.valueOf(current_order_id_int);
            }
            catch (NumberFormatException e) {}
          } catch (Exception e){
            JOptionPane.showMessageDialog(null,"Error accessing Database.");
          }

          // get and format date and time
          LocalDate current_date = LocalDate.now();
          LocalTime current_time = LocalTime.now();
          DateTimeFormatter date_format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
          DateTimeFormatter time_format = DateTimeFormatter.ofPattern("HH:mm:ss");
          String formatted_date = current_date.format(date_format);
          String formatted_time = current_time.format(time_format);

          // drink codes for drinks 1-10 (0000 if none)
          fillIDList(10);

          // change order ingredients to string
          String order_ingredients_string = "";
          for (String ingr : order_ingredients) {
            if (!ingr.equals(order_ingredients.get(0))) {
              order_ingredients_string += ",";
            }
            order_ingredients_string += ingr;
          }

          // full order string
          String order_str = "('" + current_order_id_str + "', '" + formatted_date + "', '" + formatted_time + "', '" + String.valueOf(num_drinks) + "', '" + String.valueOf(total_cost);
          for (String id : order_drinks) {
            order_str += "', '" + id;
          }
          order_str += "', '" + order_ingredients_string + "');";
          // order_str += "');";

          // write order
          // System.out.println(order_str);

          try{
            //create a statement object
            Statement stmt = conn.createStatement();
            //create a SQL statement
            String sql_statement = "INSERT INTO order_history VALUES " + order_str;
            //send statement to DBMS
            stmt.execute(sql_statement);
          } catch (Exception e){
            System.out.println(e.toString());
            JOptionPane.showMessageDialog(null,"Error accessing Database.");
          }
          paid = false;
          // reset values
          num_drinks = 0;
          total_cost = 0.0;
          order_drinks.clear();
          selected_items.clear();
          order_customizations.clear();
          order_ingredients.clear();
        }
      }

      //closing the connection
      closeConnection(conn);
    }
  

    /*
    Sales Report window that displays order ID's of orders with a certain menu ID in a given timeframe
    @param conn A connection to the database
    @return sales_frame a JFrame for the sales report
    */
    // sales window
    public static JFrame salesWindow(Connection conn) throws IOException {
      JFrame sales_frame = new JFrame();
        sales_frame.setSize(800, 600);

        JPanel sales_panel = new JPanel();
        sales_panel.setLayout(new GridLayout(7, 2));

        JLabel startDateLabel = new JLabel("Start Date (yyyy-mm-dd):");
        JTextField startDateField = new JTextField();
        JLabel endDateLabel = new JLabel("End Date (yyyy-mm-dd):");
        JTextField endDateField = new JTextField();
        JLabel startTimeLabel = new JLabel("Start Time (hh:mm:ss):");
        JTextField startTimeField = new JTextField();
        JLabel endTimeLabel = new JLabel("End Time (hh:mm:ss):");
        JTextField endTimeField = new JTextField();
        JLabel menuItemLabel = new JLabel("Menu Item (0010-0060):");
        JTextField menuItemField = new JTextField();

        JButton searchButton = new JButton("Search");
        JTextArea sales_text = new JTextArea();
        sales_text.setEditable(false);

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String startDate = startDateField.getText();
                String endDate = endDateField.getText();
                String startTime = startTimeField.getText();
                String endTime = endTimeField.getText();
                String menuItem = menuItemField.getText();

                // Create a SQL statement to query the database
                String sqlStatement = "SELECT * FROM order_history WHERE (order_date || ' ' || order_time) BETWEEN '" + startDate + " ' || '" + startTime + "' AND '" + endDate + " ' || '" + endTime +
                        "' AND (drink1 = '" + menuItem + "' OR drink2 = '" + menuItem + "' OR drink3 = '" + menuItem +
                        "' OR drink4 = '" + menuItem + "' OR drink5 = '" + menuItem + "' OR drink6 = '" + menuItem +
                        "' OR drink7 = '" + menuItem + "' OR drink8 = '" + menuItem + "' OR drink9 = '" + menuItem +
                        "' OR drink10 = '" + menuItem + "');" ;

                try {
                    Statement stmt = conn.createStatement();
                    ResultSet result = stmt.executeQuery(sqlStatement);
                    sales_text.setText(""); // Clear previous results

                    while (result.next()) {
                        sales_text.append("Order ID: " + result.getInt("order_id") + "\n");
                        sales_text.append("Order Date: " + result.getString("order_date") + "\n");
                        sales_text.append("Order Time: " + result.getString("order_time") + "\n");
                      
                        sales_text.append("\n");
                      
                    }
                } catch (Exception ex) {
                    System.out.println(ex.toString());
                    JOptionPane.showMessageDialog(null, "Error querying the database.");
                }
            }
        });

        sales_panel.add(startDateLabel);
        sales_panel.add(startDateField);
        sales_panel.add(startTimeLabel);
        sales_panel.add(startTimeField);
        sales_panel.add(endDateLabel);
        sales_panel.add(endDateField);
        sales_panel.add(endTimeLabel);
        sales_panel.add(endTimeField);
        sales_panel.add(menuItemLabel);
        sales_panel.add(menuItemField);
        sales_panel.add(searchButton);

        JScrollPane scrollable_pane = new JScrollPane(sales_text);

        sales_frame.add(sales_panel, BorderLayout.NORTH);
        sales_frame.add(scrollable_pane, BorderLayout.CENTER);

        return sales_frame;
      }

    /*
    Excess Report window that displays the list of inventory items that only sold less than 10% of their inventory between the timestamp and the current time
    @param conn A connection to the database
    @return excess_frame a JFrame for the sales report
    */
    // excess window
    public static JFrame excessWindow(Connection conn) throws IOException {
      //create window and get input
      JFrame excess_frame = new JFrame();
      excess_frame.setSize(800, 600);

      JPanel excess_panel = new JPanel();
      excess_panel.setLayout(new GridLayout(7, 2));

      JLabel startDateLabel = new JLabel("Start Date - (yyyy-mm-dd):");
      JTextField startDateField = new JTextField();
      JLabel startTimeLabel = new JLabel("Start Time - (hh:mm:ss):");
      JTextField startTimeField = new JTextField();

      JButton excessButton = new JButton("Get Excess Report");
      JTextArea excess_text = new JTextArea();
      excess_text.setEditable(false);

      excessButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
              String start_date = startDateField.getText();
              String start_time = startTimeField.getText();
              // get current time and format date and time
              LocalDate current_date = LocalDate.now();
              LocalTime current_time = LocalTime.now();
              DateTimeFormatter date_format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
              DateTimeFormatter time_format = DateTimeFormatter.ofPattern("HH:mm:ss");
              String end_date = current_date.format(date_format);
              String end_time = current_time.format(time_format);

              //create hashmap for inventory analysis
              HashMap<Integer, ArrayList<Object>> inventory_report = new HashMap<Integer, ArrayList<Object>>(); // product id maps to item name, current(end) amount, and previous(start) amount
              String sql_inventory_amounts = "SELECT * FROM inventory;";

              //populate inventory report with item name and values, populate drink analysis
              try {
                  Statement stmt = conn.createStatement();
                  ResultSet result = stmt.executeQuery(sql_inventory_amounts);

                  while (result.next()) {
                      int curr_item;
                      curr_item = result.getInt("product_id");
                      int curr_amount;
                      curr_amount = result.getInt("current_amount");
                      String item_name = result.getString("itemname");
                      inventory_report.put(curr_item,new ArrayList<>(Arrays.asList(item_name, curr_amount, curr_amount)));
                  }
              } catch (Exception ex) {
                  System.out.println(ex.toString());
                  JOptionPane.showMessageDialog(null, "Error querying the database.");
              }

              
              //create hashmap for drink analysis
              HashMap <String, String> drink_analysis = new HashMap<String, String>(); // drink id corresponds to ingredients
              String sql_drink_query = "SELECT * FROM drink_dictionary;";

              //populate drink analysis
              try {
                  Statement stmt = conn.createStatement();
                  ResultSet result = stmt.executeQuery(sql_drink_query);

                  while (result.next()) {
                    String drink_id = "";
                    String drink_ingredients = "";
                    drink_id = result.getString("drink_id");
                    drink_ingredients = result.getString("ingredients");
                    drink_analysis.put(drink_id, drink_ingredients);
                  }
              } catch (Exception ex) {
                  System.out.println(ex.toString());
                  JOptionPane.showMessageDialog(null, "Error querying the database.");
              }

              //Create a SQL statement to query the database which returns order history from a certain time to current time
              String sqlStatement = "SELECT * FROM order_history WHERE (order_date || ' ' || order_time) BETWEEN '" + start_date + "' || '" + start_time + "' AND '" + end_date + "' || '" + end_time + "';" ;
              //work backwards to figure out how much of each ingredient/ inventory item was sold during time window
              try {
                  Statement stmt = conn.createStatement();
                  ResultSet result = stmt.executeQuery(sqlStatement);

                  while (result.next()) {
                    String[] inventory_used = null;
                    String unparsed_string = "";
                    if (result.getString("ingredients") != null) {
                      unparsed_string = result.getString("ingredients");
                    }
                    else {
                      //if ingredients are not already listed in order history, go through drinks to determine ingredients
                      for (int i = 1; i < 11; i++) {
                        String drink = "";
                        drink = result.getString("drink"+i);
                        //get ingredients from drink information
                        if ((drink != null) && (!drink.equals("null")) && (!drink.equals("0000"))){
                          unparsed_string += drink_analysis.get(drink)+ ",";
                        }
                      }

                      //add default customizations
                      unparsed_string += "600001,600003,600005,600004";
                    }
                    if (!unparsed_string.equals("")){
                      inventory_used = unparsed_string.split(",");
                    }

                    //find in inventoryReport and add necessary values because working backwards
                    if (inventory_used != null){
                      for (String id : inventory_used) {
                        if ((id != null) && (!id.equals("null")) && (!id.equals(""))) {
                          //System.out.println("ID parsing: " + id);
                          int id_int = Integer.parseInt(id);
                          String item_name = (String) inventory_report.get(id_int).get(0);
                          int curr = (Integer) inventory_report.get(id_int).get(1);
                          int updated = (Integer) inventory_report.get(id_int).get(2) + 1;
                          inventory_report.put(id_int, new ArrayList<>(Arrays.asList(item_name, curr, updated)));
                        }
                      }
                    }
                  }
              } catch (Exception ex) {
                  System.out.println(ex.toString());
                  JOptionPane.showMessageDialog(null, "Error querying the database.");
              }

              //display results
              excess_text.setText("Excess Inventory \n"); // Clear previous results
              for (int i : inventory_report.keySet()) {
                int start_amount = (Integer) inventory_report.get(i).get(2);
                int end_amount = (Integer) inventory_report.get(i).get(1);

                //check if less than 10% of inventory item was sold and display accordingly
                if ((start_amount - end_amount) < (.10 * start_amount)) {
                  excess_text.append("ID: " + i + ", Item Name: " + inventory_report.get(i).get(0) + "\n");
                }
              }
          }
      });

      //add all elements to panel and frame
      excess_panel.add(startDateLabel);
      excess_panel.add(startDateField);
      excess_panel.add(startTimeLabel);
      excess_panel.add(startTimeField);
      excess_panel.add(excessButton);

      JScrollPane scrollable_pane = new JScrollPane(excess_text);

      excess_frame.add(excess_panel, BorderLayout.NORTH);
      excess_frame.add(scrollable_pane, BorderLayout.CENTER);

      return excess_frame;
    }

    // restock window
/*
    Restock Window displays list of items needing restock
    @param conn A connection to the database
    @return restock_frame a JFrame for the restock report
    */
    public static JFrame restockWindow(Connection conn) throws IOException {
      JFrame restock_frame = new JFrame();
      restock_frame.setSize(400, 400);

      JPanel restock_panel = new JPanel();
      JPanel scrollable_panel = new JPanel();

      // SELECT productid from Inventory WHERE Restock = 't';
      JTextArea restock_text = new JTextArea();
      restock_text.setEditable(false);

      //create a statement object
      try {
        Statement stmt = conn.createStatement();
        //create a SQL statement
        String sql_statement = "SELECT * FROM inventory WHERE restock = 't' ORDER BY product_id asc ;";
        //send statement to DBMS

        ResultSet result = stmt.executeQuery(sql_statement);
        while (result.next()) {
          restock_text.append(result.getString("itemname"));
          restock_text.append("\n");
        }
      } catch (Exception e){
        System.out.println(e.toString());
        JOptionPane.showMessageDialog(null,"Error calling restock.");
      }

      restock_panel.add(restock_text);
      JScrollPane scrollable_pane = new JScrollPane(restock_panel);
      restock_frame.add(scrollable_pane);

      return restock_frame;
    }
/*
    Popularity analysis of menu items display the popularity of a given number of menu items given timeframe
    @param conn A connection to the database
    @param start String with the given start timestamp
    @param end String with the given end timestamp
    @param num_items String with the given number of items
    @return pop_frame a JFrame for the Popularity Analysis
    */
    public static JFrame popularityCalculation(Connection conn, String start, String end, String num_items) throws IOException {
      JFrame pop_frame = new JFrame("Popularity Output");
      pop_frame.setSize(400,400);

      JTextArea pop_text = new JTextArea();

      ArrayList<ArrayList<String>> max_list = new ArrayList<ArrayList<String>>();

      // from start to end
      // need to tally up items in drink_dictionary and count how many times used
      LocalDate start_date = LocalDate.parse("2000-01-01");
      LocalDate end_date = LocalDate.parse("2000-01-01");
      LocalDate current_date = LocalDate.parse("2000-01-01");
      try {
        start_date = LocalDate.parse(start);
        end_date = LocalDate.parse(end);
        current_date = start_date;
      }
      catch (Exception e) {
        JOptionPane.showMessageDialog(null,"Error, Date in Wrong Format.");
        return null;
      }

      // first, get items in drink_dictionary
      ArrayList<ArrayList<String>> drink_dictionary_list = new ArrayList<ArrayList<String>>();
      //create a statement object
      try {
        Statement stmt = conn.createStatement();
        //create a SQL statement
        String sql_statement = "SELECT * FROM drink_dictionary ORDER BY drink_id asc;";
        //send statement to DBMS
        ResultSet result = stmt.executeQuery(sql_statement);
        while (result.next()) {
          ArrayList<String> single_item = new ArrayList<String>();

          single_item.add(result.getString("drink_id"));
          single_item.add("0");
          single_item.add(result.getString("name"));

          // make list of lists with ids and amount included for each item
          drink_dictionary_list.add(single_item);
        }
      } catch (Exception e){
        JOptionPane.showMessageDialog(null,"Error accessing Drink Dictionary.");
      }

      // from start date to end date
      if (!current_date.isBefore(end_date)) {
        JOptionPane.showMessageDialog(null,"Invalid Date Entry");
        return pop_frame;
      }
      while (!current_date.equals(end_date)) {
        // go through each order and tally values
        try {
          Statement stmt = conn.createStatement();
          //create a SQL statement
          String sql_statement = "SELECT * FROM order_history WHERE order_date = '" + current_date + "' ORDER BY order_id asc;";
          //send statement to DBMS
          ResultSet result = stmt.executeQuery(sql_statement);
          while (result.next()) {
            Integer num_drinks = Integer.valueOf(result.getString("num_drinks"));

            for (int i=1; i<=num_drinks; i++) {
              String current_drink = result.getString("drink" + String.valueOf(i));
              for (ArrayList<String> drink : drink_dictionary_list) {
                if (drink.contains(current_drink)) {
                  drink.set(1, String.valueOf(Integer.valueOf(drink.get(1)) + 1));
                }
              }
            }
          }
        } catch (Exception e){
          JOptionPane.showMessageDialog(null,"Error accessing Order by Date.");
        }

        current_date = current_date.plusDays(1);
      }

      // find top num_items
      for (int i=0; i<Integer.valueOf(num_items); i++) {
        ArrayList<String> current_max = new ArrayList<>();
        int max_value = 0;
        for (ArrayList<String> drink : drink_dictionary_list) {
          if (Integer.valueOf(drink.get(1)) > max_value) {
            current_max = drink;
            max_value = Integer.valueOf(drink.get(1));
          }
        }
        max_list.add(current_max);
        drink_dictionary_list.remove(current_max);
      }

      // output top items
      for (ArrayList<String> max_drink : max_list) {
        pop_text.append(max_drink.get(1) + " " + max_drink.get(2) + "\n");
      }

      pop_frame.add(pop_text);

      pop_frame.setVisible(true);

      return pop_frame;
    }

    // popularity window
/*
    Popularity Window displays the popularity analysis results
    @param conn A connection to the database
    @return popularity a JFrame for the popularity window
    */
    public static JFrame popularityWindow(Connection conn) throws IOException {
      JFrame popularity_frame = new JFrame();
      popularity_frame.setSize(400, 400);

      JPanel popularity_panel = new JPanel();

      JTextField start_date = new JTextField("Start Date");
      JTextField end_date = new JTextField("End Date");
      JTextField items = new JTextField("Number of Items");

      JButton popularity_go = new JButton("Go");

      // check if clicked
      popularity_go.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          // calculate information
          JFrame pop_frame = new JFrame();
          try {
            pop_frame = popularityCalculation(conn, start_date.getText(), end_date.getText(), items.getText());
          }
          catch (Exception f) {
            JOptionPane.showMessageDialog(null,"Error calculating Popularity.");
            f.printStackTrace();
          }

          // reset values
          start_date.setText("Start Date");
          end_date.setText("End Date");
          items.setText("Number of Items");
        }
      });

      popularity_panel.add(start_date);
      popularity_panel.add(end_date);
      popularity_panel.add(items);
      // popularity_panel.add(output);

      popularity_panel.add(popularity_go);

      popularity_frame.add(popularity_panel);

      return popularity_frame;
    }
    //sales together window
/*
    Sales Together Report displays list of pairs of menu items that sell together often
    @param conn A connection to the database
    @return sales_together_frame a JFrame for the What Sales Together report
    */
    public static JFrame sales_together_window(Connection conn) throws IOException {
      JFrame sales_together_frame = new JFrame();
      sales_together_frame.setSize(400, 400);

      JPanel sales_together_panel = new JPanel();
        
      JTextField start_date = new JTextField("Start Date");
      JTextField end_date = new JTextField("End Date");  

      JButton sales_together_go = new JButton("Go");

      JTextArea results_sales = new JTextArea(10, 30);
      results_sales.setEditable(false);
      JScrollPane scrollPane = new JScrollPane(results_sales);

      // check if clicked
      sales_together_go.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            
          results_sales.setText(""); // Clear previous content

          String start_date_text = start_date.getText();
          String end_date_text = end_date.getText();
          String sql_statement = "SELECT a.drink1, a.drink2, COUNT(*) AS frequency " +
                                "FROM order_history a " +
                                "INNER JOIN order_history b ON a.order_id = b.order_id " +
                                "WHERE a.drink1 < a.drink2 " +
                                "AND a.order_date BETWEEN '" + start_date_text + "' AND '" + end_date_text + "' " +
                                "GROUP BY a.drink1, a.drink2 " +
                                "ORDER BY frequency DESC";
          try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(sql_statement);
            while (result.next()) {
              results_sales.append(result.getString("drink1") + ", " + result.getString("drink2") + ", Frequency: " + result.getString("frequency") + "\n");
            }
            result.close();
            stmt.close();
            
          } catch (Exception st_press) {
            System.out.println(st_press.toString());
            JOptionPane.showMessageDialog(null,"Error calling sales together.");
          }
            sales_together_frame.setVisible(true);
        }
      });
      sales_together_panel.add(start_date);
      sales_together_panel.add(end_date);

      sales_together_panel.add(sales_together_go);
      sales_together_panel.add(scrollPane);

      sales_together_frame.add(sales_together_panel);

      return sales_together_frame;
    }

    // reports window
/*
    Displays Reports window with buttons to choose report to view
    @param conn A connection to the database
    @return reports_frame a JFrame for the reports buttons
    */
    public static JFrame reportsWindow(Connection conn) throws IOException {
      // create frame for report
      JFrame reports_frame = new JFrame("Reports Window");
      reports_frame.setSize(400, 400);

      // panel
      JPanel reports_panel = new JPanel();

      // create buttons for sales, excess, restock
      JButton sales = new JButton("Sales");
      JButton excess = new JButton("Excess");
      JButton restock = new JButton("Restock");
      JButton popularity = new JButton("Popularity");
      JButton sales_together = new JButton("What Sales Together");

      // check if clicked
      sales.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          // call sales function
          JFrame sales_frame = new JFrame();
          try {
            sales_frame = salesWindow(conn);
          } catch (Exception f){
            JOptionPane.showMessageDialog(null,"Error sales window.");
          }
          sales_frame.setVisible(true);
        }
      });
      excess.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          // call excess function
          JFrame excess_frame = new JFrame();
          try {
            excess_frame = excessWindow(conn);
          } catch (Exception f){
            JOptionPane.showMessageDialog(null,"Error excess window.");
          }
          excess_frame.setVisible(true);
        }
      });
      restock.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          // call restock function
          JFrame restock_frame = new JFrame();
          try {
            restock_frame = restockWindow(conn);
          } catch (Exception f){
            JOptionPane.showMessageDialog(null,"Error restock window.");
          }
          restock_frame.setVisible(true);
        }
      });
      popularity.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          // call popularity function
          JFrame popularity_frame = new JFrame();
          try {
            popularity_frame = popularityWindow(conn);
          } catch (Exception f){
            JOptionPane.showMessageDialog(null,"Error popularity window.");
          }
          popularity_frame.setVisible(true);
        }
      });
      sales_together.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          // call sales together function
          JFrame sales_together_frame = new JFrame();
          try {
            sales_together_frame = sales_together_window(conn);
          } catch (Exception f){
            JOptionPane.showMessageDialog(null,"Error sales together window.");
          }
          sales_together_frame.setVisible(true);
        }
      });

      reports_panel.add(sales);
      reports_panel.add(excess);
      reports_panel.add(restock);
      reports_panel.add(popularity);
      reports_panel.add(sales_together);

      reports_frame.add(reports_panel);

      return reports_frame;
    }

    /*
    Creates a window of customizations available, click to select, click again to deselect
    @param Connection conn to database
    @return JFrame customizations_frame, frame of customization window
    */
    public static JFrame customizationWindow(Connection conn) throws IOException {
      // Create a new frame for Customization options
      JFrame customizations_frame = new JFrame("Customization Options");
      customizations_frame.setSize(800, 800);
      JPanel customization_sub_menu = new JPanel(new GridLayout(4, 4));
  
      ArrayList<String> customization_names = null;

      ArrayList<String> current_customizations = new ArrayList<>();

      try {
          customization_names = getCustomizationNamesTable(conn);
      } catch (IOException error1) {
          error1.printStackTrace();
      }
  
      for (int i = 1; i < customization_names.size(); i++) {
          String customization = customization_names.get(i);
          JButton custom = new JButton(customization);
          customization_sub_menu.add(custom);


          // check if clicked
          custom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              // Extract the text from the clicked button
              String selected_item = custom.getText();

              if (current_customizations.contains(selected_item)) {
                // if currently selected, deselect
                current_customizations.remove(selected_item);
                custom.setBackground(null);
              }
              else {
                current_customizations.add(selected_item);
                custom.setBackground(Color.BLUE);
                custom.setOpaque(true);
                custom.setBorderPainted(false);
              }

            }
        });
      }

      // continue button 
      JButton continue_button = new JButton("Continue");
      continue_button.setBackground(Color.GREEN);
      continue_button.setOpaque(true);
      continue_button.setBorderPainted(false);
      customization_sub_menu.add(continue_button);

      continue_button.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          String s = e.getActionCommand();
          if (s == "Continue") {
            // once continue is clicked add all current customizations to order
            for (String custom : current_customizations) {
              selected_items.add(custom);
              order_customizations.add(custom);
              // update total cost
              try {
                total_cost += getCustomizationCostTable(conn, custom);
              }
              catch (IOException error1) {
                error1.printStackTrace();
              }
            }
            // and close frame
            customizations_frame.dispose();
          }
        }
      });
        
      // Add the submenu panel to the customizations_frame
      customizations_frame.add(customization_sub_menu);


      return customizations_frame;
    }

    /*
    Creates and shows a window of drink types and calls customization window
    @param Connection conn to database
    @param String drink_type, type of drink selected
    @param int size_x, number of x divisions of grid
    @param int size_y, number of y divisions of grid
    @return None, void function
    */
    public static void typeWindow(Connection conn, String drink_type, int size_x, int size_y) throws IOException {
      // Create a new frame for type options
      JFrame outside_frame = new JFrame(drink_type + " Options");
      outside_frame.setSize(800, 800);
      JPanel sub_menu = new JPanel(new GridLayout(size_x, size_y));

      for (String drink : drink_names) {
        // if the right type
        if (drink.length() >= drink_type.length() && drink.substring(0, drink_type.length()).equals(drink_type)) {
          JButton mt = new JButton(drink);
          mt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              // Extract the text from the clicked button
              String selected_item = mt.getText();
              // Add it to the ArrayList
              selected_items.add(selected_item);

              // add to number of drinks and total cost
              num_drinks += 1;
              try {
                  total_cost += getDrinkCostTable(conn, drink);
              }
              catch (IOException error1) {
                error1.printStackTrace();
              }
              try {
                order_drinks.add(getDrinkIDTable(conn, drink));
              }
              catch (IOException error1) {
                error1.printStackTrace();
              }
              

              // Close the outside_frame
              outside_frame.dispose();

              // Open the new frame here (e.g., a new options frame)
              JFrame customs_frame = new JFrame("Customizations");
              try {
                customs_frame = customizationWindow(conn);
              } catch (IOException error1) {
                  error1.printStackTrace();
              }

              customs_frame.setSize(800, 800);
              customs_frame.setVisible(true);
            }
          });
          sub_menu.add(mt);
        }
      }

      // add sub_menu to frame and make visible
      outside_frame.add(sub_menu);
      outside_frame.setVisible(true);
    }

    /*
    Creates and shows a window informing that a payment has been processed, allows employee to exit or make another order
    @param None
    @return None, void function
    */
    public static void payWindow() {
      JFrame outside_frame = new JFrame("Payment Processed");
      outside_frame.setSize(400, 400);
      JPanel button_sub_menu = new JPanel(new GridLayout(0,2));

      JButton employee_exit = new JButton("Employee Exit");
      JButton another_order = new JButton("Another Order");

      employee_exit.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          // Close the outside_frame and employee frame
          outside_frame.dispose();
          employee_frame.dispose();
        }
      });

      another_order.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          // Close the outside_frame
          outside_frame.dispose();
        }
      });

      button_sub_menu.add(employee_exit);
      button_sub_menu.add(another_order);

      outside_frame.add(button_sub_menu);
      outside_frame.setVisible(true);
    }

    /*
    Creates and shows a window informing that an order has been cancelled
    @param None
    @return None, void function
    */
    public static void cancelWindow() {
      JFrame outside_frame = new JFrame("Cancelled Order");
      outside_frame.setSize(400, 400);
      JPanel cancel_sub_menu = new JPanel(new BorderLayout());

      JTextArea cancel_text = new JTextArea("Order has been cancelled.");
      cancel_text.setEditable(false);

      JButton exit_button = new JButton("Exit");

      exit_button.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          // Close the outside_frame and employee frame
          outside_frame.dispose();
        }
      });

      cancel_sub_menu.add(cancel_text);
      cancel_sub_menu.add(exit_button, BorderLayout.PAGE_END);

      outside_frame.add(cancel_sub_menu);

      outside_frame.setVisible(true);
    }

    /*
    Contains commands to fulfill based on which button is pressed
    @param ActionEvent e, action that was performed (button pressed)
    @return None, void function
    */
    public void actionPerformed(ActionEvent e) {   
        
        String s = e.getActionCommand();
        Connection conn = null;
        try {
              conn = DriverManager.getConnection(
                "jdbc:postgresql://csce-315-db.engr.tamu.edu/csce315331_08r_db",
                "csce315_971_navya_0215",
                "password");
            } catch (Exception k) {
              System.exit(0);
            }

        if (s.equals("Close")) {
            f.dispose();
        }
        else if (s.equals("Manager")) {
          manager_frame.setVisible(true);
        }
        else if (s.equals("Employee")) {
          employee_frame.setVisible(true);
        }

        if (s.equals("Inventory")) {
          inventory_frame.setVisible(true);
        }
        if (s.equals("Menu")) {
          drinks_frame.setVisible(true);
        }
        if (s.equals("Reports")) {
          reports_frame.setVisible(true);
        }
        if (s.equals("Add Menu Item")){ 
          // set the text of the label to the text of the field
          add_menu.setVisible(true);
        }
        if (s.equals("Add Inventory Item")){
          add_inventory.setVisible(true);
        }

        if(s.equals("Save Menu Item")){
          menu_check = true;
          dataFeature(text_input, text_output, ingredients, ingredient_out, conn, true, true);
          menu_check = false;
        }

        if (s.equals("Save Inventory Item")){
          inventory_check = true;
          dataFeature(text_input_inventory, text_output_inventory, ingredients, ingredient_out, conn, true, false);
          inventory_check = false;
        }
        //update menu 
        if (s.equals("Update Menu")){
          update_menu.setVisible(true);
        }
        if(s.equals("Save Updates for Menu Item")) {
          menu_check = true;
          dataFeature(update_text_input, update_text_output,ingredients, ingredient_out, conn, false, true);
          menu_check = false;
        }
        if (s.equals("Update Inventory")){
          update_inventory.setVisible(true);
        }
        if(s.equals("Save Updates for Inventory Item")){
          inventory_check = true;
          dataFeature(update_input_inventory, update_output_inventory, ingredients, ingredient_out, conn, false, false);
          inventory_check = false;
        }

        if (s.equals("View Order")) {
          JFrame order_frame = new JFrame("Viewing Order");
          order_frame.setSize(400,400);
          
          JPanel total_sub_menu = new JPanel(new BorderLayout());
          JPanel button_sub_menu = new JPanel(new GridLayout(0,3));
          JPanel order_sub_menu = new JPanel(new GridLayout(0, 2));

          
          JTextArea order_text = new JTextArea();
          order_text.setEditable(false);

          JTextArea prices_text = new JTextArea();
          prices_text.setEditable(false);

          int index = 0;
          for (String selected_item : selected_items) {
            if (index == 0) {
              order_text.append(selected_item);
              try {
                prices_text.append(String.valueOf(getDrinkCostTable(conn, selected_item)));
              }
              catch (IOException error1) {
                error1.printStackTrace();
              }
            }
            else {
              if (drink_names.contains(selected_item)) {
                order_text.append("\n\n");
                prices_text.append("\n\n");
                try {
                  prices_text.append(String.valueOf(getDrinkCostTable(conn, selected_item)));
                }
                catch (IOException error1) {
                  error1.printStackTrace();
                }
              }
              else {
                order_text.append("\n");
                prices_text.append("\n");
                try {
                  prices_text.append(String.valueOf(getCustomizationCostTable(conn, selected_item)));
                }
                catch (IOException error1) {
                  error1.printStackTrace();
                }
              }
              order_text.append(selected_item);
              
            }
            index++;
          }

          order_text.append("\n\n\n\tTotal Price");
          prices_text.append("\n\n\n" + String.valueOf(total_cost));

          // add buttons
          JButton more_drinks = new JButton("Add More Drinks");
          JButton cancel_order = new JButton("Cancel Order");
          JButton finish_and_pay = new JButton("Finish and Pay");

          // check if clicked
          more_drinks.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              String s = e.getActionCommand();
              if (s == "Add More Drinks") {
                order_frame.dispose();
              }
            }
          }); 
          cancel_order.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              String s = e.getActionCommand();
              if (s == "Cancel Order") {
                // reset values
                num_drinks = 0;
                total_cost = 0.0;
                order_drinks.clear();
                selected_items.clear();
                order_customizations.clear();

                order_frame.dispose();
                // output window acknowledging cancelled order
                cancelWindow();
              }
            }
          });
          finish_and_pay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              String s = e.getActionCommand();
              if (s == "Finish and Pay") {
                // close order frame
                paid = true;
                order_frame.dispose();

                payWindow();
              }
            }
          }); 
          
          order_sub_menu.add(order_text);
          order_sub_menu.add(prices_text);

          JScrollPane scrollable_order = new JScrollPane(order_sub_menu);  
          scrollable_order.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);  
  
          button_sub_menu.add(more_drinks);
          button_sub_menu.add(cancel_order);
          button_sub_menu.add(finish_and_pay);

          total_sub_menu.add(scrollable_order);
          total_sub_menu.add(button_sub_menu, BorderLayout.PAGE_END);

          order_frame.add(total_sub_menu);

          order_frame.setVisible(true);
        }
        if (s.equals("Milk Tea")) {
          // Create a new frame for Milk Tea options
          try {
            typeWindow(conn, "Milk Tea", 4, 4);
          }
          catch (IOException error1) {
            error1.printStackTrace();
          }
        }
        if(s.equals("Seasonal")){
          try{
            typeWindow(conn, "Seasonal", 4, 4);
          }
          catch (IOException error1){
            error1.printStackTrace();
          }
        }
        if (s.equals("Brewed Tea")) {
          // Create a new frame for Brewed Tea options
          try {
            typeWindow(conn, "Brewed Tea", 2, 4);
          }
          catch (IOException error1) {
            error1.printStackTrace();
          }
        }
        if (s.equals("Fruit Tea")) {
          // Create a new frame for Fruit Tea options
          try {
            typeWindow(conn, "Fruit Tea", 3, 4);
          }
          catch (IOException error1) {
            error1.printStackTrace();
          }
        }
        if (s.equals("Fresh Milk")) {
          // Create a new frame for Fresh Milk options
          try {
            typeWindow(conn, "Fresh Milk", 3, 3);
          }
          catch (IOException error1) {
            error1.printStackTrace();
          }
        }
        if (s.equals("Ice Blended")) {
          // Create a new frame for Ice Blended options
          try {
            typeWindow(conn, "Ice Blended", 3, 3);
          }
          catch (IOException error1) {
            error1.printStackTrace();
          }
        }
        if (s.equals("Tea Mojito")) {
          // Create a new frame for Mojito options
          try {
            typeWindow(conn, "Mojito", 2, 2);
          }
          catch (IOException error1) {
            error1.printStackTrace();
          }
        }
        if (s.equals("Creama")) {
          // Create a new frame for Creama options
          try {
            typeWindow(conn, "Creama", 2, 4);
          }
          catch (IOException error1) {
            error1.printStackTrace();
          }
        }

        if (s.equals("Customizations")) {
          // Create a new frame for Customization options
          JFrame customizations_frame = new JFrame("Customization Options");
          customizations_frame.setSize(800, 800);
          JPanel customization_sub_menu = new JPanel(new GridLayout(4, 4));
      
          ArrayList<String> customization_names = null;
          try {
              customization_names = getCustomizationNamesTable(conn);
          } catch (IOException error1) {
              error1.printStackTrace();
          }
      
          for (int i = 1; i < customization_names.size(); i++) {
              String customization = customization_names.get(i);
              JButton custom = new JButton(customization);
              customization_sub_menu.add(custom);
          }
      
          // Add the submenu panel to the customizations_frame
          customizations_frame.add(customization_sub_menu);
      
          // Make the new frame visible
          customizations_frame.setVisible(true);
        }
        if (s.equals("Employee Exit")) {
          // reset values
          num_drinks = 0;
          total_cost = 0.0;
          order_drinks.clear();
          selected_items.clear();
          order_customizations.clear();
          // cancel order
          cancelWindow();
          // exit employee
          employee_frame.dispose();
        }
        if (s.equals("Cancel Order")) {
          // reset values
          num_drinks = 0;
          total_cost = 0.0;
          order_drinks.clear();
          selected_items.clear();
          order_customizations.clear();

          // output window acknowledging cancelled order
          cancelWindow();
        }
    }

    /*
    Fill rest of order_drinks array with code 0000, corresponding to no drink
    @param int max_drinks max number of drinks
    @return None, void function
    */
    public static void fillIDList(int max_drinks) {
      while (order_drinks.size() < max_drinks) {
        order_drinks.add("0000");
      }
    }

    /*
    Get all names of drinks using data from file
    @param String file_path to database
    @return ArrayList<String> drink_names of all customizations
    */
    public static ArrayList<String> getDrinkNames(String file_path) throws IOException {
      ArrayList<String> drink_names = new ArrayList<>();
      File file = new File(file_path);

      Scanner scanner = new Scanner(file);

      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String[] parts = line.split(",");
        if (parts.length >= 2) {
          String drink_name = parts[1].trim();
          drink_names.add(drink_name);
        }
      }

      scanner.close(); // Close the scanner explicitly.

      return drink_names;
    }

    /*
    Get all names of customizations using tables from database
    @param Connection conn to database
    @return ArrayList<String> drink_names of all customizations
    */
    public static ArrayList<String> getDrinkNamesTable(Connection conn) throws IOException {
      ArrayList<String> drink_names = new ArrayList<>();

      //create a statement object
      try {
        Statement stmt = conn.createStatement();
        //create a SQL statement
        String sql_statement = "SELECT * FROM drink_dictionary ORDER BY drink_id asc;";
        //send statement to DBMS

        ResultSet result = stmt.executeQuery(sql_statement);
        while (result.next()) {
          drink_names.add(result.getString("name"));
        }
      } catch (Exception e){
        System.out.println(e.toString());
        JOptionPane.showMessageDialog(null,"Error accessing Database.");
      }

      return drink_names;
    }

    /*
    Get cost of drink using data from file
    @param String file_path to file, String drink_name
    @return double drink_cost of drink_name
    */
    public static double getDrinkCost(String file_path, String drink_name) throws IOException {
      double drink_cost = 0;
      File file = new File(file_path);

      Scanner scanner = new Scanner(file);

      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String[] parts = line.split(",");
        if (parts.length >= 3) {
          String current_drink = parts[1].trim();
          String current_cost = parts[2].trim();

          if (current_drink.equals(drink_name)) {
            drink_cost = Double.valueOf(current_cost);
          }
        }
      }

      scanner.close(); // Close the scanner explicitly.

      return drink_cost;
    }

    /*
    Get cost of drink using data from file
    @param Connection conn to database, String drink_name
    @return double drink_cost of drink_name
    */
    public static double getDrinkCostTable(Connection conn, String drink_name) throws IOException {
      double drink_cost = 0;

      //create a statement object
      try {
        Statement stmt = conn.createStatement();
        //create a SQL statement
        String sqlStatement = "SELECT * FROM drink_dictionary ORDER BY drink_id asc;";
        //send statement to DBMS

        ResultSet result = stmt.executeQuery(sqlStatement);
        while (result.next()) {
          if (result.getString("name").equals(drink_name)) {
            drink_cost = Double.valueOf(result.getString("price"));
          }
        }
      } catch (Exception e){
        System.out.println(e.toString());
        JOptionPane.showMessageDialog(null,"Error accessing Database.");
      }

      return drink_cost;
    }

    /*
    Get id of drink using data from file
    @param String file_path to file, String drink_name
    @return String drink_id of drink_name
    */
    public static String getDrinkID(String file_path, String drink_name) throws IOException {
      String drink_ID = "0000";
      File file = new File(file_path);

      Scanner scanner = new Scanner(file);

      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String[] parts = line.split(",");
        if (parts.length >= 2) {
          String current_drink = parts[1].trim();
          String current_ID = parts[0].trim();

          if (current_drink.equals(drink_name)) {
            drink_ID = current_ID;
          }
        }
      }

      scanner.close(); // Close the scanner explicitly.

      return drink_ID;
    }

    /*
    Get id of drink using tables from database
    @param Connection conn to database, String drink_name
    @return String drink_id of drink_name
    */
    public static String getDrinkIDTable(Connection conn, String drink_name) throws IOException {
      String drink_ID = "0000";

      //create a statement object
      try {
        Statement stmt = conn.createStatement();
        //create a SQL statement
        String sqlStatement = "SELECT * FROM drink_dictionary ORDER BY drink_id asc;";
        //send statement to DBMS

        ResultSet result = stmt.executeQuery(sqlStatement);
        while (result.next()) {
          if (result.getString("name").equals(drink_name)) {
            drink_ID = result.getString("drink_id");
          }
        }
      } catch (Exception e){
        System.out.println(e.toString());
        JOptionPane.showMessageDialog(null,"Error accessing Database.");
      }

      return drink_ID;
    }

    /*
    Get all names of customizations using data from file
    @param String file_path to database
    @return ArrayList<String> of all customizations
    */
    public static ArrayList<String> getCustomizationNames(String file_path) throws IOException {
      ArrayList<String> customization_names = new ArrayList<>();
      File file = new File(file_path);
  
      Scanner scanner = new Scanner(file);
  
      while (scanner.hasNextLine()) {
          String line = scanner.nextLine();
          String[] parts = line.split(",");
          if (parts.length >= 3) {
              String customization_name = parts[2].trim();
              customization_names.add(customization_name);
          }
      }
  
      scanner.close(); // Close the scanner explicitly.
  
      return customization_names;
    }

    /*
    Get all names of customizations using tables from database
    @param Connection conn to database
    @return ArrayList<String> customization_names of all customizations
    */
    public static ArrayList<String> getCustomizationNamesTable(Connection conn) throws IOException {
      ArrayList<String> customization_names = new ArrayList<>();

      //create a statement object
      try {
        Statement stmt = conn.createStatement();
        //create a SQL statement
        String sqlStatement = "SELECT * FROM customizations ORDER BY id asc;";
        //send statement to DBMS

        ResultSet result = stmt.executeQuery(sqlStatement);
        while (result.next()) {
          customization_names.add(result.getString("customization"));
        }
      } catch (Exception e){
        System.out.println(e.toString());
        JOptionPane.showMessageDialog(null,"Error accessing Database.");
      }

      return customization_names;
    }

    /*
    Get value of customization cost table with drinks and customizations using data from file
    @param String file_path to database, String customName name of customization
    @return double custom_cost with price of customization
    */
    public static double getCustomizationCost(String file_path, String custom_name) throws IOException {
      double custom_cost = 0;
      File file = new File(file_path);
  
      Scanner scanner = new Scanner(file);
  
      while (scanner.hasNextLine()) {
          String line = scanner.nextLine();
          String[] parts = line.split(",");
          if (parts.length >= 4) {
              String current_custom = parts[2].trim();
              String current_cost = parts[3].trim();

              if (current_custom.equals(custom_name)) {
                custom_cost = Double.valueOf(current_cost);
              }
          }
      }
  
      scanner.close(); // Close the scanner explicitly.
  
      return custom_cost;
    }

    /*
    Get value of customization cost table with drinks and customizations using tables from database
    @param Connection conn to database, String customName name of customization
    @return double custom_cost with price of customization
    */
    public static double getCustomizationCostTable(Connection conn, String custom_name) throws IOException {
      double custom_cost = 0;

      //create a statement object
      try {
        Statement stmt = conn.createStatement();
        //create a SQL statement
        String sqlStatement = "SELECT * FROM customizations ORDER BY id asc;";
        //send statement to DBMS

        ResultSet result = stmt.executeQuery(sqlStatement);
        while (result.next()) {
          if (result.getString("customization").equals(custom_name)) {
            custom_cost = Double.valueOf(result.getString("price"));
          }
        }
      } catch (Exception e){
        System.out.println(e.toString());
        JOptionPane.showMessageDialog(null,"Error accessing Database.");
      }
    
      return custom_cost;
    }

    /*
    Update database inventory table with drinks and customizations using values hard-coded in
    @param Connection conn to database
    @return None, void function
    */
    public static void updateInventory(Connection conn) {
      // for each item in inventory find current amount
      ArrayList<ArrayList<String>> inventory_list = new ArrayList<ArrayList<String>>();

      //create a statement object
      try {
        Statement stmt = conn.createStatement();
        //create a SQL statement
        String sql_statement = "SELECT * FROM inventory ORDER BY product_id asc;";
        //send statement to DBMS
        ResultSet result = stmt.executeQuery(sql_statement);
        while (result.next()) {
          ArrayList<String> single_item = new ArrayList<String>();

          single_item.add(result.getString("product_id"));
          single_item.add(result.getString("total_amount"));
          single_item.add(result.getString("current_amount"));

          // make list of lists with all id, total, and current amount included for each item
          inventory_list.add(single_item);
        }
      } catch (Exception e){
        JOptionPane.showMessageDialog(null,"Error accessing Database.");
      }

      // update values in inventory list
      // 500001-500021 are drink types
      // 500022-500031 are add-ins
      // 600001-600008 are misc, often used in every drink

      for (String id : order_drinks) {
        if (id.equals("0000")) {
          // no update, null value
          continue;
        }
        if (id.equals("0001")) {
          // milk tea classic black
          for (ArrayList<String> item : inventory_list) {
            // milk
            if (item.contains("600008")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // black tea
            if (item.contains("500001")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0002")) {
          // milk tea classic green
          for (ArrayList<String> item : inventory_list) {
            // milk
            if (item.contains("600008")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // green tea
            if (item.contains("500002")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0003")) {
          // milk tea classic oolong
          for (ArrayList<String> item : inventory_list) {
            // milk
            if (item.contains("600008")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // oolong tea
            if (item.contains("500003")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0004")) {
          // milk tea honey black
          for (ArrayList<String> item : inventory_list) {
            // milk
            if (item.contains("600008")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // black tea
            if (item.contains("500001")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // honey
            if (item.contains("600010")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0005")) {
          // milk tea honey green
          for (ArrayList<String> item : inventory_list) {
            // milk
            if (item.contains("600008")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // green tea
            if (item.contains("500002")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // honey
            if (item.contains("600010")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0006")) {
          // milk tea honey oolong
          for (ArrayList<String> item : inventory_list) {
            // milk
            if (item.contains("600008")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // oolong tea
            if (item.contains("500003")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // honey
            if (item.contains("600010")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0007")) {
          // milk tea classic coffee
          for (ArrayList<String> item : inventory_list) {
            // milk
            if (item.contains("600008")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // coffee
            if (item.contains("500004")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0008")) {
          // milk tea ginger
          for (ArrayList<String> item : inventory_list) {
            // milk
            if (item.contains("600008")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // ginger
            if (item.contains("500005")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0009")) {
          // milk tea hokkaido
          for (ArrayList<String> item : inventory_list) {
            // milk
            if (item.contains("600008")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // hokkaido tea
            if (item.contains("500006")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0010")) {
          // milk tea okinawa
          for (ArrayList<String> item : inventory_list) {
            // milk
            if (item.contains("600008")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // okinawa tea
            if (item.contains("500007")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0011")) {
          // milk tea thai
          for (ArrayList<String> item : inventory_list) {
            // milk
            if (item.contains("600008")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // thai tea
            if (item.contains("500008")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0012")) {
          // milk tea taro
          for (ArrayList<String> item : inventory_list) {
            // milk
            if (item.contains("600008")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // taro
            if (item.contains("500009")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0013")) {
          // milk tea mango green
          for (ArrayList<String> item : inventory_list) {
            // milk
            if (item.contains("600008")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // green tea
            if (item.contains("500002")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // mango
            if (item.contains("500010")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0014")) {
          // milk tea QQ Happy Family
          for (ArrayList<String> item : inventory_list) {
            // milk
            if (item.contains("600008")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // QQ Happy Family
            if (item.contains("500011")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0015")) {
          // milk tea matcha red bean
          for (ArrayList<String> item : inventory_list) {
            // milk
            if (item.contains("600008")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // matcha
            if (item.contains("500012")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // red bean
            if (item.contains("500026")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0016")) {
          // brewed tea classic black
          for (ArrayList<String> item : inventory_list) {
            // black tea
            if (item.contains("500001")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0017")) {
          // brewed tea classic green
          for (ArrayList<String> item : inventory_list) {
            // green tea
            if (item.contains("500002")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0018")) {
          // brewed tea classic oolong
          for (ArrayList<String> item : inventory_list) {
            // oolong tea
            if (item.contains("500003")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0019")) {
          // brewed tea wintermelon
          for (ArrayList<String> item : inventory_list) {
            // wintermelon
            if (item.contains("500013")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0020")) {
          // brewed tea honey black
          for (ArrayList<String> item : inventory_list) {
            // black tea
            if (item.contains("500001")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // honey
            if (item.contains("600010")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0021")) {
          // brewed tea honey green
          for (ArrayList<String> item : inventory_list) {
            // green tea
            if (item.contains("500002")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // honey
            if (item.contains("600010")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0022")) {
          // brewed tea honey oolong
          for (ArrayList<String> item : inventory_list) {
            // oolong tea
            if (item.contains("500003")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // honey
            if (item.contains("600010")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0023")) {
          // brewed tea ginger
          for (ArrayList<String> item : inventory_list) {
            // ginger
            if (item.contains("500005")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0024")) {
          // fruit tea mango green
          for (ArrayList<String> item : inventory_list) {
            // green tea
            if (item.contains("500002")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // mango
            if (item.contains("500010")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0025")) {
          // fruit tea wintermelon lemonade
          for (ArrayList<String> item : inventory_list) {
            // wintermelon
            if (item.contains("500013")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // lemonade
            if (item.contains("600011")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }            
        if (id.equals("0026")) {
          // fruit tea strawberry
          for (ArrayList<String> item : inventory_list) {
            // strawberry
            if (item.contains("500015")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }      
        if (id.equals("0027")) {
          // fruit tea peach
          for (ArrayList<String> item : inventory_list) {
            // peach
            if (item.contains("500016")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }    
        if (id.equals("0028")) {
          // fruit tea peach kiwi
          for (ArrayList<String> item : inventory_list) {
            // peach
            if (item.contains("500016")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // kiwi
            if (item.contains("500017")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0029")) {
          // fruit tea kiwi
          for (ArrayList<String> item : inventory_list) {
            // kiwi
            if (item.contains("500017")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }  
        if (id.equals("0030")) {
          // fruit tea mango and passionfruit
          for (ArrayList<String> item : inventory_list) {
            // mango
            if (item.contains("500010")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // passionfruit
            if (item.contains("500018")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }  
        if (id.equals("0031")) {
          // fruit tea tropical fruit
          for (ArrayList<String> item : inventory_list) {
            // tropical fruit
            if (item.contains("500019")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }     
        if (id.equals("0032")) {
          // fruit tea hawaii fruit
          for (ArrayList<String> item : inventory_list) {
            // hawaii fruit
            if (item.contains("500020")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }   
        if (id.equals("0033")) {
          // fruit tea passionfruit orange and grapefruit
          for (ArrayList<String> item : inventory_list) {
            // passionfruit
            if (item.contains("500018")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // orange and grapefruit
            if (item.contains("500021")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }   
        if (id.equals("0034")) {
          // fresh milk
          for (ArrayList<String> item : inventory_list) {
            // fresh milk
            if (item.contains("600009")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 2));
            }
          }
        }
        if (id.equals("0035")) {
          // fresh milk classic black
          for (ArrayList<String> item : inventory_list) {
            // fresh milk
            if (item.contains("600009")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // black tea
            if (item.contains("500001")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0036")) {
          // fresh milk classic green
          for (ArrayList<String> item : inventory_list) {
            // fresh milk
            if (item.contains("600009")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // green tea
            if (item.contains("500002")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0037")) {
          // fresh milk classic oolong
          for (ArrayList<String> item : inventory_list) {
            // fresh milk
            if (item.contains("600009")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // oolong tea
            if (item.contains("500003")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0038")) {
          // fresh milk tea wintermelon
          for (ArrayList<String> item : inventory_list) {
            // fresh milk
            if (item.contains("600009")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // wintermelon
            if (item.contains("500013")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0039")) {
          // fresh milk cocoa lover
          for (ArrayList<String> item : inventory_list) {
            // cocoa
            if (item.contains("500032")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // fresh milk
            if (item.contains("600009")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0040")) {
          // fresh milk QQ Happy Family
          for (ArrayList<String> item : inventory_list) {
            // fresh milk
            if (item.contains("600009")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // QQ Happy Family
            if (item.contains("500011")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0041")) {
          // fresh milk milk tea matcha
          for (ArrayList<String> item : inventory_list) {
            // fresh milk
            if (item.contains("600009")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // matcha
            if (item.contains("500012")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (id.equals("0042")) {
          // fresh milk taro
          for (ArrayList<String> item : inventory_list) {
            // fresh milk
            if (item.contains("600009")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
            // taro
            if (item.contains("500009")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
      }

      // found in every drink (cups, straws, napkins)
      for (ArrayList<String> item : inventory_list) {
        // cups
        if (item.contains("600001")) {
          item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - num_drinks));
        }
        // straws
        if (item.contains("600003")) {
          item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - num_drinks));
        }
        // napkins
        if (item.contains("600005")) {
          item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - num_drinks));
        }
      }

      // all customizations
      for (String customization : order_customizations) {
        // customizations array is off in ids
        if (customization.equals("pearl")) {
          // pearl
          for (ArrayList<String> item : inventory_list) {
            if (item.contains("500022")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (customization.equals("mini pearl")) {
          // mini pearl
          for (ArrayList<String> item : inventory_list) {
            if (item.contains("500025")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (customization.equals("ice cream")) {
          // ice cream
          for (ArrayList<String> item : inventory_list) {
            if (item.contains("500028")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (customization.equals("pudding")) {
          // pudding
          for (ArrayList<String> item : inventory_list) {
            if (item.contains("500031")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (customization.equals("aloe vera")) {
          // aloe vera
          for (ArrayList<String> item : inventory_list) {
            if (item.contains("500023")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (customization.equals("red bean")) {
          // red bean
          for (ArrayList<String> item : inventory_list) {
            if (item.contains("500026")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (customization.equals("herb jelly")) {
          // herb jelly
          for (ArrayList<String> item : inventory_list) {
            if (item.contains("500029")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (customization.equals("aiyu jelly")) {
          // aiyu jelly
          for (ArrayList<String> item : inventory_list) {
            if (item.contains("500030")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (customization.equals("lychee jelly")) {
          // lychee jelly
          for (ArrayList<String> item : inventory_list) {
            if (item.contains("500024")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (customization.equals("creama")) {
          // creama
          for (ArrayList<String> item : inventory_list) {
            if (item.contains("500027")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (customization.equals("regular ice")) {
          // regular ice
          for (ArrayList<String> item : inventory_list) {
            if (item.contains("600006")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 2));
            }
          }
        }
        if (customization.equals("less ice")) {
          // less ice
          for (ArrayList<String> item : inventory_list) {
            if (item.contains("600006")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
        if (customization.equals("normal sweet")) {
          // normal sweet
          for (ArrayList<String> item : inventory_list) {
            if (item.contains("600007")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 4));
            }
          }
        }
        if (customization.equals("less sweet")) {
          // less sweet
          for (ArrayList<String> item : inventory_list) {
            if (item.contains("600007")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 3));
            }
          }
        }
        if (customization.equals("half sweet")) {
          // half sweet
          for (ArrayList<String> item : inventory_list) {
            if (item.contains("600007")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 2));
            }
          }
        }
        if (customization.equals("light sweet")) {
          // light sweet
          for (ArrayList<String> item : inventory_list) {
            if (item.contains("600007")) {
              item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
            }
          }
        }
      }

      // update values
      for (ArrayList<String> item : inventory_list) {
        //create a SQL statement
        String sql_statement = "UPDATE inventory";
        sql_statement += " SET current_amount = ";
        sql_statement += item.get(2);
        sql_statement += " WHERE product_id = ";
        sql_statement += item.get(0);
        sql_statement += ";";

        try{
          //create a statement object
          Statement stmt = conn.createStatement();
          //send statement to DBMS
          stmt.execute(sql_statement);
        } catch (Exception e){
          JOptionPane.showMessageDialog(null,"Error accessing Database.");
        }
      }


      // check inventory values
      checkInventoryLevels(conn);
    }

    /*
    Update database inventory table with drinks and customizations using tables from database
    @param Connection conn to database
    @return None, void function
    */
    public static void updateInventoryTable(Connection conn) {
      // for each item in inventory find current amount
      ArrayList<ArrayList<String>> inventory_list = new ArrayList<ArrayList<String>>();
      //create a statement object
      try {
        Statement stmt = conn.createStatement();
        //create a SQL statement
        String sql_statement = "SELECT * FROM inventory ORDER BY product_id asc;";
        //send statement to DBMS
        ResultSet result = stmt.executeQuery(sql_statement);
        while (result.next()) {
          ArrayList<String> single_item = new ArrayList<String>();

          single_item.add(result.getString("product_id"));
          single_item.add(result.getString("total_amount"));
          single_item.add(result.getString("current_amount"));

          // make list of lists with all id, total, and current amount included for each item
          inventory_list.add(single_item);
        }
      } catch (Exception e){
        JOptionPane.showMessageDialog(null,"Error accessing Inventory.");
      }

      // for each item in drink_dictionary find ingredients
      ArrayList<ArrayList<String>> drink_dictionary_list = new ArrayList<ArrayList<String>>();
      //create a statement object
      try {
        Statement stmt = conn.createStatement();
        //create a SQL statement
        String sql_statement = "SELECT * FROM drink_dictionary ORDER BY drink_id asc;";
        //send statement to DBMS
        ResultSet result = stmt.executeQuery(sql_statement);
        while (result.next()) {
          ArrayList<String> single_item = new ArrayList<String>();

          single_item.add(result.getString("drink_id"));
          single_item.add(result.getString("ingredients"));

          // make list of lists with ids and ingredients included for each item
          drink_dictionary_list.add(single_item);
        }
      } catch (Exception e){
        JOptionPane.showMessageDialog(null,"Error accessing Drink Dictionary.");
      }

      // for each item in customizations get id
      ArrayList<ArrayList<String>> customizations_dictionary_list = new ArrayList<ArrayList<String>>();
      //create a statement object
      try {
        Statement stmt = conn.createStatement();
        //create a SQL statement
        String sql_statement = "SELECT * FROM customizations ORDER BY id asc;";
        //send statement to DBMS
        ResultSet result = stmt.executeQuery(sql_statement);
        while (result.next()) {
          ArrayList<String> single_item = new ArrayList<String>();

          single_item.add(result.getString("id"));
          single_item.add(result.getString("customization"));

          // make list of lists with ids and ingredients included for each item
          customizations_dictionary_list.add(single_item);
        }
      } catch (Exception e){
        JOptionPane.showMessageDialog(null,"Error accessing Customizations.");
      }

      // update values in inventory list
      // 500001-500021 are drink types
      // 500022-500031 are add-ins
      // 600001-600008 are misc, often used in every drink

      // for each drink in the order
      for (String id : order_drinks) {
        if (id.equals("0000")) {
          // no update, null value
          continue;
        }

        // find spot in drink_dictionary
        for (ArrayList<String> drink : drink_dictionary_list) {
          String ingredients = "";
          ArrayList<String> ingredients_list = new ArrayList<String>();

          // if at right drink
          if (drink.get(0).equals(id)) {
            // split ingredients into list
            ingredients = drink.get(1);
            String[] temp_str_array = ingredients.split(",");
            ingredients_list = new ArrayList<String>(Arrays.asList(temp_str_array));

            // decrease inventory value for each ingredient
            for (String ingredient : ingredients_list) {
              for (ArrayList<String> item : inventory_list) {
                if (item.contains(ingredient)) {
                  item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
                }
              }

              // append ingredient to order ingredients list
              order_ingredients.add(ingredient);
            }
          }
        }
      }


      // found in every drink (cups, straws, napkins)
      for (ArrayList<String> item : inventory_list) {
        // cups
        if (item.contains("600001")) {
          item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - num_drinks));
          order_ingredients.add("600001");
        }
        // straws
        if (item.contains("600003")) {
          item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - num_drinks));
          order_ingredients.add("600003");
        }
        // napkins
        if (item.contains("600005")) {
          item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - num_drinks));
          order_ingredients.add("600005");
        }        
      }

      // all customizations
      for (String customization : order_customizations) {
        String custom_id = "";

        // find spot in customizations_dictionary_list
        for (ArrayList<String> custom_dictionary : customizations_dictionary_list) {
          if (custom_dictionary.get(1).equals(customization)) {
            custom_id = custom_dictionary.get(0);

            order_ingredients.add(custom_id);
          }
        }

        // update inventory
        for (ArrayList<String> item : inventory_list) {
          if (item.contains(custom_id)) {
            item.set(2, String.valueOf(Integer.valueOf(item.get(2)) - 1));
          }
        }
      }

      // update values
      for (ArrayList<String> item : inventory_list) {
        //create a SQL statement
        String sql_statement = "UPDATE inventory";
        sql_statement += " SET current_amount = ";
        sql_statement += item.get(2);
        sql_statement += " WHERE product_id = ";
        sql_statement += item.get(0);
        sql_statement += ";";

        try{
          //create a statement object
          Statement stmt = conn.createStatement();
          //send statement to DBMS
          stmt.execute(sql_statement);
        } catch (Exception e){
          JOptionPane.showMessageDialog(null,"Error updating Inventory.");
        }
      }


      // check inventory values
      checkInventoryLevels(conn);
    }
 
}
