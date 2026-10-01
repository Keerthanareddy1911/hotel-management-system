package hotel;
import java.sql.*;
import java.util.*;
import java.math.BigDecimal;
import hotel.Models.*;
public final class JdbcRepository implements Repository {
 private final String url,user,password;
 public JdbcRepository(String url,String user,String password){this.url=url;this.user=user;this.password=password;}
 private Connection open() throws SQLException {return DriverManager.getConnection(url,user,password);}
 public List<Room> rooms() throws SQLException {
  try(Connection c=open();PreparedStatement s=c.prepareStatement("SELECT * FROM rooms ORDER BY number");ResultSet r=s.executeQuery()){
   List<Room> out=new ArrayList<>();while(r.next())out.add(new Room(r.getInt("id"),r.getString("number"),r.getString("type"),r.getInt("capacity"),r.getBigDecimal("rate")));return out;
  }
 }
 private Booking read(ResultSet r)throws SQLException{return new Booking(r.getLong("id"),r.getInt("room_id"),r.getString("guest"),r.getString("email"),r.getInt("guests"),r.getDate("arrival").toLocalDate(),r.getDate("departure").toLocalDate(),Status.valueOf(r.getString("status")),r.getBigDecimal("nightly_rate"),r.getBigDecimal("extras"),r.getBigDecimal("tax_percent"));}
 public List<Booking> bookings()throws SQLException{
  try(Connection c=open();PreparedStatement s=c.prepareStatement("SELECT * FROM bookings ORDER BY id DESC");ResultSet r=s.executeQuery()){List<Booking> out=new ArrayList<>();while(r.next())out.add(read(r));return out;}
 }
 private void lockRoom(Connection c,int roomId)throws SQLException{
  try(PreparedStatement s=c.prepareStatement("SELECT id FROM rooms WHERE id=? FOR UPDATE")){s.setInt(1,roomId);try(ResultSet r=s.executeQuery()){if(!r.next())throw new IllegalArgumentException("Unknown room.");}}
 }
 public Booking create(Booking b)throws SQLException{
  try(Connection c=open()) {c.setAutoCommit(false);try {
   lockRoom(c,b.roomId());
   try(PreparedStatement s=c.prepareStatement("SELECT id FROM bookings WHERE room_id=? AND status IN ('RESERVED','CHECKED_IN') AND arrival < ? AND departure > ? FOR UPDATE")){
    s.setInt(1,b.roomId());s.setDate(2,java.sql.Date.valueOf(b.departure()));s.setDate(3,java.sql.Date.valueOf(b.arrival()));try(ResultSet r=s.executeQuery()){if(r.next())throw new IllegalArgumentException("Room is already booked for these dates.");}
   }
   long id;try(PreparedStatement s=c.prepareStatement("INSERT INTO bookings(room_id,guest,email,guests,arrival,departure,status,nightly_rate,extras,tax_percent) VALUES(?,?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
    s.setInt(1,b.roomId());s.setString(2,b.guest());s.setString(3,b.email());s.setInt(4,b.guests());s.setDate(5,java.sql.Date.valueOf(b.arrival()));s.setDate(6,java.sql.Date.valueOf(b.departure()));s.setString(7,b.status().name());s.setBigDecimal(8,b.nightlyRate());s.setBigDecimal(9,b.extras());s.setBigDecimal(10,b.taxPercent());s.executeUpdate();try(ResultSet r=s.getGeneratedKeys()){if(!r.next())throw new SQLException("No generated ID");id=r.getLong(1);}
   }
   c.commit();return new Booking(id,b.roomId(),b.guest(),b.email(),b.guests(),b.arrival(),b.departure(),b.status(),b.nightlyRate(),b.extras(),b.taxPercent());
  }catch(SQLException|RuntimeException e){c.rollback();throw e;}}
 }
 public Booking transition(long id,Status target,BigDecimal extras)throws SQLException{
  try(Connection c=open()){c.setAutoCommit(false);try{
   int roomId;try(PreparedStatement s=c.prepareStatement("SELECT room_id FROM bookings WHERE id=?")){s.setLong(1,id);try(ResultSet r=s.executeQuery()){if(!r.next())throw new IllegalArgumentException("Booking not found.");roomId=r.getInt(1);}}
   lockRoom(c,roomId);
   Booking b;try(PreparedStatement s=c.prepareStatement("SELECT * FROM bookings WHERE id=? FOR UPDATE")){s.setLong(1,id);try(ResultSet r=s.executeQuery()){if(!r.next())throw new IllegalArgumentException("Booking not found.");b=read(r);}}
   Service.validateTransition(b.status(),target);
   try(PreparedStatement s=c.prepareStatement("UPDATE bookings SET status=?,extras=? WHERE id=?")){s.setString(1,target.name());s.setBigDecimal(2,extras);s.setLong(3,id);s.executeUpdate();}
   c.commit();return new Booking(id,b.roomId(),b.guest(),b.email(),b.guests(),b.arrival(),b.departure(),target,b.nightlyRate(),extras,b.taxPercent());
  }catch(SQLException|RuntimeException e){c.rollback();throw e;}}
 }
}
