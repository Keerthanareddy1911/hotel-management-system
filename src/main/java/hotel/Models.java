package hotel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
public final class Models {
 public record Room(int id, String number, String type, int capacity, BigDecimal rate) {}
 public enum Status { RESERVED, CHECKED_IN, CHECKED_OUT, CANCELLED }
 public record Booking(long id, int roomId, String guest, String email, int guests, LocalDate arrival, LocalDate departure, Status status, BigDecimal nightlyRate, BigDecimal extras, BigDecimal taxPercent) {
  public long nights() { return ChronoUnit.DAYS.between(arrival, departure); }
  public BigDecimal subtotal() { return nightlyRate.multiply(BigDecimal.valueOf(nights())).add(extras).setScale(2); }
  public BigDecimal tax() { return subtotal().multiply(taxPercent).divide(new BigDecimal("100"),2,java.math.RoundingMode.HALF_UP); }
  public BigDecimal total() { return subtotal().add(tax()); }
  public boolean active() { return status==Status.RESERVED || status==Status.CHECKED_IN; }
  public boolean overlaps(LocalDate a,LocalDate d) { return active() && arrival.isBefore(d) && a.isBefore(departure); }
 }
 public static List<Room> initialRooms() { return List.of(new Room(1,"101","Standard",2,new BigDecimal("1800.00")),new Room(2,"102","Standard",2,new BigDecimal("1800.00")),new Room(3,"201","Deluxe",3,new BigDecimal("3200.00")),new Room(4,"202","Deluxe",3,new BigDecimal("3200.00")),new Room(5,"301","Suite",4,new BigDecimal("5500.00")),new Room(6,"302","Suite",4,new BigDecimal("5500.00"))); }
}
