package hotel;
import java.util.*;
import java.math.BigDecimal;
import hotel.Models.*;
public final class MemoryRepository implements Repository {
 private final Map<Long,Booking> index=new LinkedHashMap<>(); // expected O(1) booking lookup
 private long sequence=1;
 public List<Room> rooms(){return Models.initialRooms();}
 public synchronized List<Booking> bookings(){return new ArrayList<>(index.values());}
 public synchronized Booking create(Booking b){
  if(index.values().stream().anyMatch(x->x.roomId()==b.roomId() && x.overlaps(b.arrival(),b.departure()))) throw new IllegalArgumentException("Room is already booked for these dates.");
  Booking saved=new Booking(sequence++,b.roomId(),b.guest(),b.email(),b.guests(),b.arrival(),b.departure(),b.status(),b.nightlyRate(),b.extras(),b.taxPercent()); index.put(saved.id(),saved);return saved;
 }
 public synchronized Booking transition(long id,Status target,BigDecimal extras){
  Booking b=index.get(id);if(b==null)throw new IllegalArgumentException("Booking not found.");
  Service.validateTransition(b.status(),target);
  Booking saved=new Booking(b.id(),b.roomId(),b.guest(),b.email(),b.guests(),b.arrival(),b.departure(),target,b.nightlyRate(),extras,b.taxPercent());index.put(id,saved);return saved;
 }
}
