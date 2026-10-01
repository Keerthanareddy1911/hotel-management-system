package hotel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import hotel.Models.*;
public final class Service {
 private final Repository repo;
 private final Map<Integer,Room> roomIndex=new HashMap<>();
 private final BigDecimal tax;
 public Service(Repository repo,BigDecimal tax) throws Exception {this.repo=repo;this.tax=tax;if(tax.signum()<0||tax.compareTo(new BigDecimal("100"))>0)throw new IllegalArgumentException("Tax must be between 0 and 100."); for(Room r:repo.rooms())roomIndex.put(r.id(),r);}
 public List<Room> rooms(){return roomIndex.values().stream().sorted(Comparator.comparing(Room::number)).toList();}
 public List<Booking> bookings() throws Exception{return repo.bookings().stream().sorted(Comparator.comparingLong(Booking::id).reversed()).toList();}
 public Booking reserve(Map<String,String> f) throws Exception {
  Room r=roomIndex.get(Integer.parseInt(required(f,"roomId")));if(r==null)throw new IllegalArgumentException("Unknown room.");
  String guest=required(f,"guest"),email=required(f,"email");
  if(guest.length()>100||email.length()>150||!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))throw new IllegalArgumentException("Enter a valid guest name and email.");
  int guests=Integer.parseInt(required(f,"guests"));if(guests<1||guests>r.capacity())throw new IllegalArgumentException("Guest count exceeds room capacity.");
  LocalDate a=LocalDate.parse(required(f,"arrival")),d=LocalDate.parse(required(f,"departure"));
  if(a.isBefore(LocalDate.now())||!d.isAfter(a)||java.time.temporal.ChronoUnit.DAYS.between(a,d)>365)throw new IllegalArgumentException("Choose future dates and a stay of 1–365 nights.");
  return repo.create(new Booking(0,r.id(),guest,email,guests,a,d,Status.RESERVED,r.rate(),new BigDecimal("0.00"),tax));
 }
 public Booking action(Map<String,String> f) throws Exception {
  long id=Long.parseLong(required(f,"id"));Status target=Status.valueOf(required(f,"status"));
  BigDecimal extras=new BigDecimal(f.getOrDefault("extras","0")).setScale(2,java.math.RoundingMode.UNNECESSARY);
  if(extras.signum()<0||extras.compareTo(new BigDecimal("1000000"))>0)throw new IllegalArgumentException("Extra charges must be between 0 and 1000000, with at most two decimal places.");
  return repo.transition(id,target,extras);
 }
 static String required(Map<String,String> f,String key){String v=f.getOrDefault(key,"").trim();if(v.isEmpty())throw new IllegalArgumentException("Missing field: "+key);return v;}
 static void validateTransition(Status from,Status to){
  if(!((from==Status.RESERVED&&(to==Status.CHECKED_IN||to==Status.CANCELLED))||(from==Status.CHECKED_IN&&to==Status.CHECKED_OUT)))throw new IllegalArgumentException("Invalid booking status change.");
 }
}
