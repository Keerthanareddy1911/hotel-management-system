package hotel;
import java.util.*;
import hotel.Models.*;
public interface Repository {
 List<Room> rooms() throws Exception;
 List<Booking> bookings() throws Exception;
 Booking create(Booking candidate) throws Exception;
 Booking transition(long id, Status target, java.math.BigDecimal extras) throws Exception;
}
