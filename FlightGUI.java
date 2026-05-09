import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.Box;
import java.util.ArrayList;
import java.util.List;

class DBConnection {
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/flightdb", "root", "shivkirtan");
            return c;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

class Flight {
    int flightNo;
    String from, to;
    int seats;
    double price;

    public Flight(int flightNo, String from, String to, int seats, double price) {
        this.flightNo = flightNo;
        this.from = from;
        this.to = to;
        this.seats = seats;
        this.price = price;
    }

    public int getFlightNo() { return flightNo; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public int getSeats() { return seats; }
    public double getPrice() { return price; }
    public void setSeats(int seats) { this.seats = seats; }
}

class Admin {
    private String name;

    public Admin(String name) {
        this.name = name;
    }

    public static boolean login(String user, String pass) {
        if(user.equals("admin") && pass.equals("1234"))
            return true;
        return false;
    }

    public void addFlight(FlightService service, Flight f) {
        service.addFlight(f);
    }
}

class Customer {
    private String name;
    private FlightService service;

    public Customer(String name, FlightService service) {
        this.name = name;
        this.service = service;
    }

    public String getName() { return name; }

    public boolean book(int flightNo, int seats, String passengerName) {
        return service.bookFlight(flightNo, seats, passengerName);
    }
}

class FlightService {

    public void addFlight(Flight f) {
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO flights (flightNo, fromCity, toCity, seats, price) VALUES (?,?,?,?,?)");
            ps.setInt(1, f.getFlightNo());
            ps.setString(2, f.getFrom());
            ps.setString(3, f.getTo());
            ps.setInt(4, f.getSeats());
            ps.setDouble(5, f.getPrice());
            ps.executeUpdate();
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding flight: " + e.getMessage());
        }
    }

    public List<Flight> getAll() {
        List<Flight> list = new ArrayList<>();
        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM flights ORDER BY flightNo");
            while (rs.next()) {
                Flight f = new Flight(
                    rs.getInt("flightNo"),
                    rs.getString("fromCity"),
                    rs.getString("toCity"),
                    rs.getInt("seats"),
                    rs.getDouble("price")
                );
                list.add(f);
            }
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching flights: " + e.getMessage());
        }
        return list;
    }

    public boolean bookFlight(int flightNo, int seats, String passengerName) {
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM flights WHERE flightNo=?");
            ps.setInt(1, flightNo);
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                throw new RuntimeException("Flight not found");
            int available = rs.getInt("seats");
            if (seats > available)
                throw new RuntimeException("Only " + available + " seats available");
            PreparedStatement upd = con.prepareStatement("UPDATE flights SET seats=seats-? WHERE flightNo=?");
            upd.setInt(1, seats);
            upd.setInt(2, flightNo);
            upd.executeUpdate();
            con.close();
            return true;
        } catch (RuntimeException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Booking error: " + e.getMessage());
        }
    }

    public List<Flight> search(String city) {
        List<Flight> list = new ArrayList<>();
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM flights WHERE fromCity LIKE ? OR toCity LIKE ?");
            ps.setString(1, "%" + city + "%");
            ps.setString(2, "%" + city + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Flight(
                    rs.getInt("flightNo"),
                    rs.getString("fromCity"),
                    rs.getString("toCity"),
                    rs.getInt("seats"),
                    rs.getDouble("price")
                ));
            }
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException("Search error: " + e.getMessage());
        }
        return list;
    }

    public boolean exists(int flightNo) {
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT flightNo FROM flights WHERE flightNo=?");
            ps.setInt(1, flightNo);
            boolean found = ps.executeQuery().next();
            con.close();
            return found;
        } catch (SQLException e) {
            return false;
        }
    }

    public Flight getFlight(int flightNo) {
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM flights WHERE flightNo=?");
            ps.setInt(1, flightNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Flight f = new Flight(
                    rs.getInt("flightNo"),
                    rs.getString("fromCity"),
                    rs.getString("toCity"),
                    rs.getInt("seats"),
                    rs.getDouble("price")
                );
                con.close();
                return f;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

public class FlightGUI extends JFrame {

    static final Color BG   = new Color(15, 20, 35);
    static final Color CARD = new Color(25, 32, 52);
    static final Color ACC  = new Color(99, 179, 237);
    static final Color TXT  = new Color(220, 228, 245);
    static final Color SUB  = new Color(110, 125, 155);
    static final Color GRN  = new Color(72, 199, 142);
    static final Color ORG  = new Color(246, 135, 90);

    JLabel status = new JLabel("  Ready");
    FlightService service = new FlightService();
    Admin admin = new Admin("Admin");

    JTextField tf(String ph) {
        JTextField f = new JTextField(12);
        f.setBackground(new Color(12, 16, 28));
        f.setForeground(SUB);
        f.setCaretColor(ACC);
        f.setToolTipText(ph);
        f.setText(ph);
        f.setFont(new Font("Monospaced", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(40, 55, 80)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(ph)) {
                    f.setText("");
                    f.setForeground(TXT);
                }
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) {
                    f.setText(ph);
                    f.setForeground(SUB);
                }
            }
        });
        return f;
    }

    // get actual text from field (ignore placeholder)
    String val(JTextField f) {
        String t = f.getText().trim();
        if(t.equals(f.getToolTipText()))
            return "";
        return t;
    }

    JButton btn(String t, Color c) {
        JButton b = new JButton(t);
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(SUB);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return l;
    }

    DefaultTableModel toModel(List<Flight> flights) {
        String[] cols = {"Flight No", "From", "To", "Seats", "Price (Rs)"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for(int i = 0; i < flights.size(); i++) {
            Flight f = flights.get(i);
            m.addRow(new Object[]{
                f.getFlightNo(), f.getFrom(), f.getTo(),
                f.getSeats(), String.format("%.2f", f.getPrice())
            });
        }
        return m;
    }

    JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setBackground(new Color(12, 16, 28));
        t.setForeground(TXT);
        t.setGridColor(new Color(40, 55, 80));
        t.setRowHeight(26);
        t.setFont(new Font("Monospaced", Font.PLAIN, 13));
        t.setSelectionBackground(new Color(99, 179, 237, 60));
        t.setSelectionForeground(TXT);
        JTableHeader h = t.getTableHeader();
        h.setBackground(CARD);
        h.setForeground(ACC);
        h.setFont(new Font("SansSerif", Font.BOLD, 12));
        return t;
    }

    JScrollPane tablePane(List<Flight> flights) {
        JTable tbl = styledTable(toModel(flights));
        JScrollPane sp = new JScrollPane(tbl);
        sp.getViewport().setBackground(new Color(12, 16, 28));
        sp.setBorder(BorderFactory.createLineBorder(new Color(40, 55, 80)));
        return sp;
    }

    void setCenter(JPanel p, JComponent c) {
        BorderLayout bl = (BorderLayout) p.getLayout();
        Component old = bl.getLayoutComponent(BorderLayout.CENTER);
        if (old != null)
            p.remove(old);
        p.add(c, BorderLayout.CENTER);
        p.revalidate();
        p.repaint();
    }

    void setStatus(String msg) {
        status.setText("  " + msg);
    }

    JPanel section(String title) {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(40, 55, 80)),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JLabel l = new JLabel(title);
        l.setForeground(ACC);
        l.setFont(new Font("Georgia", Font.BOLD, 14));
        p.add(l, BorderLayout.NORTH);
        return p;
    }

    JPanel buildFlightsTab() {
        JPanel p = section("✈  All Flights");
        JButton refresh = btn("↻  Refresh", ACC);

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        topRow.setOpaque(false);
        topRow.add(refresh);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(topRow, BorderLayout.EAST);
        p.add(north, BorderLayout.NORTH);

        try {
            p.add(tablePane(service.getAll()), BorderLayout.CENTER);
        } catch (Exception e) {
            p.add(new JLabel("DB Error: " + e.getMessage()), BorderLayout.CENTER);
        }

        refresh.addActionListener(e -> {
            try {
                setCenter(p, tablePane(service.getAll()));
                setStatus("✓ Refreshed");
            } catch (Exception ex) {
                setStatus("Error: " + ex.getMessage());
            }
        });
        return p;
    }

    JPanel buildAddTab(JPanel flightsTab) {
        JPanel p = section("➕  Add Flight  —  Admin Only");

        JTextField uF = tf("Username");
        JPasswordField pwF = new JPasswordField(10);
        pwF.setBackground(new Color(12, 16, 28));
        pwF.setForeground(TXT);
        pwF.setCaretColor(ACC);
        pwF.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(40, 55, 80)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        pwF.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JTextField fNo  = tf("Flight No");
        JTextField fFr  = tf("From City");
        JTextField fTo  = tf("To City");
        JTextField fSt  = tf("Seats");
        JTextField fPr  = tf("Price");

        JButton addBtn   = btn("+ Add Flight", GRN);
        JButton loginBtn = btn("🔑 Login", ACC);
        addBtn.setEnabled(false);

        JLabel loginStatus = new JLabel("  Login to enable Add");
        loginStatus.setForeground(SUB);
        loginStatus.setFont(new Font("SansSerif", Font.ITALIC, 11));

        loginBtn.addActionListener(e -> {
            String u = val(uF);
            String pw = new String(pwF.getPassword()).trim();
            if (Admin.login(u, pw)) {
                addBtn.setEnabled(true);
                loginStatus.setText("  ✓ Admin logged in");
                loginStatus.setForeground(GRN);
                loginBtn.setEnabled(false);
                setStatus("Admin logged in");
            } else {
                loginStatus.setText("  ✗ Wrong credentials");
                loginStatus.setForeground(ORG);
                setStatus("Wrong admin credentials");
            }
        });

        addBtn.addActionListener(e -> {
            String no = val(fNo);
            String from = val(fFr);
            String to = val(fTo);
            String st = val(fSt);
            String pr = val(fPr);

            if (no.isEmpty() || from.isEmpty() || to.isEmpty() || st.isEmpty() || pr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fill all fields!", "Missing", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int flightNo = Integer.parseInt(no);
                int seats    = Integer.parseInt(st);
                double price = Double.parseDouble(pr);

                if (service.exists(flightNo)) {
                    JOptionPane.showMessageDialog(this,
                        "Flight No " + flightNo + " already exists!", "Duplicate", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                admin.addFlight(service, new Flight(flightNo, from, to, seats, price));
                setStatus("✓ Flight " + flightNo + " added!");
                JOptionPane.showMessageDialog(this, "Flight added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                // reset fields back to placeholder
                fNo.setText(fNo.getToolTipText()); fNo.setForeground(SUB);
                fFr.setText(fFr.getToolTipText()); fFr.setForeground(SUB);
                fTo.setText(fTo.getToolTipText()); fTo.setForeground(SUB);
                fSt.setText(fSt.getToolTipText()); fSt.setForeground(SUB);
                fPr.setText(fPr.getToolTipText()); fPr.setForeground(SUB);

                setCenter(flightsTab, tablePane(service.getAll()));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Flight No, Seats, Price must be numbers.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                setStatus("Error: " + ex.getMessage());
            }
        });

        JPanel loginRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        loginRow.setOpaque(false);
        loginRow.add(lbl("Username:")); loginRow.add(uF);
        loginRow.add(lbl("Password:")); loginRow.add(pwF);
        loginRow.add(loginBtn);
        loginRow.add(loginStatus);

        JPanel fieldRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        fieldRow.setOpaque(false);
        fieldRow.add(lbl("No:"));    fieldRow.add(fNo);
        fieldRow.add(lbl("From:"));  fieldRow.add(fFr);
        fieldRow.add(lbl("To:"));    fieldRow.add(fTo);
        fieldRow.add(lbl("Seats:")); fieldRow.add(fSt);
        fieldRow.add(lbl("Price:")); fieldRow.add(fPr);
        fieldRow.add(addBtn);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(40, 55, 80));

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.add(loginRow);
        center.add(Box.createVerticalStrut(6));
        center.add(sep);
        center.add(Box.createVerticalStrut(6));
        center.add(fieldRow);

        p.add(center, BorderLayout.CENTER);
        return p;
    }

    JPanel buildBookTab(JPanel flightsTab) {
        JPanel p = section("🎫  Book Flight");

        JTextField bNo   = tf("Flight No");
        JTextField bName = tf("Passenger Name");
        JTextField bSeat = tf("Seats");

        JTextArea receipt = new JTextArea(8, 42);
        receipt.setBackground(new Color(12, 16, 28));
        receipt.setForeground(GRN);
        receipt.setEditable(false);
        receipt.setFont(new Font("Monospaced", Font.PLAIN, 13));
        receipt.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JButton bookBtn  = btn("✔ Book Now", ORG);
        JButton clearBtn = btn("✕ Clear", new Color(80, 85, 105));

        bookBtn.addActionListener(e -> {
            String no   = val(bNo);
            String name = val(bName);
            String seat = val(bSeat);

            if (no.isEmpty() || name.isEmpty() || seat.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fill all fields!", "Missing", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int flightNo    = Integer.parseInt(no);
                int seatsWanted = Integer.parseInt(seat);
                if (seatsWanted <= 0)
                    throw new NumberFormatException();

                Flight f = service.getFlight(flightNo);
                if (f == null) {
                    JOptionPane.showMessageDialog(this, "Flight not found!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                new Customer(name, service).book(flightNo, seatsWanted, name);
                double total = f.getPrice() * seatsWanted;

                receipt.setText(
                    "════════════════════════════════\n" +
                    "        BOOKING CONFIRMED ✓\n" +
                    "════════════════════════════════\n" +
                    "Passenger  : " + name + "\n" +
                    "Flight No  : " + flightNo + "\n" +
                    "Route      : " + f.getFrom() + " -> " + f.getTo() + "\n" +
                    "Seats      : " + seatsWanted + "\n" +
                    "Price/Seat : Rs " + String.format("%.2f", f.getPrice()) + "\n" +
                    "TOTAL BILL : Rs " + String.format("%.2f", total) + "\n" +
                    "════════════════════════════════"
                );
                setStatus("✓ Booking confirmed for " + name);
                setCenter(flightsTab, tablePane(service.getAll()));

                // clear the fields
                bNo.setText(bNo.getToolTipText());     bNo.setForeground(SUB);
                bName.setText(bName.getToolTipText()); bName.setForeground(SUB);
                bSeat.setText(bSeat.getToolTipText()); bSeat.setForeground(SUB);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Flight No and Seats must be valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Booking Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        clearBtn.addActionListener(e -> {
            receipt.setText("");
            bNo.setText(bNo.getToolTipText());     bNo.setForeground(SUB);
            bName.setText(bName.getToolTipText()); bName.setForeground(SUB);
            bSeat.setText(bSeat.getToolTipText()); bSeat.setForeground(SUB);
        });

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        row.setOpaque(false);
        row.add(lbl("Flight No:"));  row.add(bNo);
        row.add(lbl("Passenger:")); row.add(bName);
        row.add(lbl("Seats:"));     row.add(bSeat);
        row.add(bookBtn);
        row.add(clearBtn);

        JScrollPane sp = new JScrollPane(receipt);
        sp.getViewport().setBackground(new Color(12, 16, 28));
        p.add(row, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    JPanel buildSearchTab() {
        JPanel p = section("🔍  Search Flights");

        JTextField sCity  = tf("City name...");
        JButton searchBtn = btn("🔍 Search", ACC);
        JButton clearBtn  = btn("✕ Clear", new Color(80, 85, 105));

        ActionListener doSearch = e -> {
            String city = val(sCity);
            try {
                List<Flight> results;
                if(city.isEmpty())
                    results = service.getAll();
                else
                    results = service.search(city);
                setCenter(p, tablePane(results));
                if(city.isEmpty())
                    setStatus("Showing all flights");
                else
                    setStatus(results.size() + " result(s) for \"" + city + "\"");
            } catch (Exception ex) {
                setStatus("Error: " + ex.getMessage());
            }
        };

        searchBtn.addActionListener(doSearch);
        sCity.addActionListener(doSearch);

        clearBtn.addActionListener(e -> {
            sCity.setText(sCity.getToolTipText());
            sCity.setForeground(SUB);
            try {
                setCenter(p, tablePane(service.getAll()));
            } catch (Exception ignored) {}
            setStatus("Showing all flights");
        });

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        row.setOpaque(false);
        row.add(lbl("City:"));
        row.add(sCity);
        row.add(searchBtn);
        row.add(clearBtn);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        JLabel title = new JLabel("🔍  Search Flights");
        title.setForeground(ACC);
        title.setFont(new Font("Georgia", Font.BOLD, 14));
        north.add(title, BorderLayout.NORTH);
        north.add(row, BorderLayout.CENTER);

        p.removeAll();
        p.add(north, BorderLayout.NORTH);
        try {
            p.add(tablePane(service.getAll()), BorderLayout.CENTER);
        } catch (Exception e) {
            p.add(new JLabel("DB Error"), BorderLayout.CENTER);
        }
        return p;
    }

    FlightGUI() {
        super("✈  SkyBook — Flight Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950, 640);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);

        UIManager.put("TabbedPane.background", CARD);
        UIManager.put("TabbedPane.foreground", TXT);
        UIManager.put("TabbedPane.selected", ACC);
        UIManager.put("TabbedPane.contentAreaColor", BG);
        UIManager.put("TabbedPane.tabAreaBackground", CARD);
        UIManager.put("Panel.background", CARD);
        UIManager.put("Label.foreground", TXT);

        JPanel t1 = buildFlightsTab();
        JPanel t2 = buildAddTab(t1);
        JPanel t3 = buildBookTab(t1);
        JPanel t4 = buildSearchTab();

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(CARD);
        tabs.setForeground(TXT);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.addTab("✈ Flights", t1);
        tabs.addTab("➕ Add", t2);
        tabs.addTab("🎫 Book", t3);
        tabs.addTab("🔍 Search", t4);

        status.setForeground(ACC);
        status.setFont(new Font("SansSerif", Font.PLAIN, 12));
        status.setOpaque(true);
        status.setBackground(CARD);
        status.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 55, 80)),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));

        add(tabs, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(FlightGUI::new);
    }
}