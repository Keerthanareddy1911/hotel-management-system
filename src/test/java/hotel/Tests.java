package hotel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import hotel.Models.*;
public class Tests {
 static int checks=0;
 static void check(boolean ok,String note){if(!ok)throw new AssertionError(note);checks++;}
 static Map<String,String> form(int room,int start,int end){return new HashMap<>(Map.of("roomId",""+room,"guest","Test Guest","email","guest@example.com","guests","2","arrival",LocalDate.now().plusDays(start).toString(),"departure",LocalDate.now().plusDays(end).toString()));}
 interface Task {void run()throws Exception;}
 static void reject(Task f,String note)throws Exception{try{f.run();throw new AssertionError(note);}catch(IllegalArgumentException|java.time.DateTimeException|ArithmeticException expected){checks++;}}
 public static void main(String[] args)throws Exception {
  Service s=new Service(new MemoryRepository(),new BigDecimal("12"));
  Booking b=s.reserve(form(1,1,3));check(b.nights()==2,"night count");check(b.total().compareTo(new BigDecimal("4032.00"))==0,"exact decimal invoice");
  reject(()->s.reserve(form(1,2,4)),"overlap blocked");s.reserve(form(1,3,4));check(s.bookings().size()==2,"adjacent stay allowed");
  var capacity=form(2,1,2);capacity.put("guests","5");reject(()->s.reserve(capacity),"capacity");reject(()->s.reserve(form(2,2,2)),"zero nights");reject(()->s.reserve(form(2,-1,1)),"past arrival");
  var email=form(2,1,2);email.put("email","invalid");reject(()->s.reserve(email),"email");
  reject(()->s.action(Map.of("id",""+b.id(),"status","CHECKED_OUT")),"cannot skip check-in");
  s.action(Map.of("id",""+b.id(),"status","CHECKED_IN"));Booking invoice=s.action(Map.of("id",""+b.id(),"status","CHECKED_OUT","extras","500.25"));check(invoice.total().compareTo(new BigDecimal("4592.28"))==0,"extras and tax rounding");
  reject(()->s.action(Map.of("id",""+b.id(),"status","CANCELLED")),"terminal state");
  Booking cancelled=s.reserve(form(2,1,2));s.action(Map.of("id",""+cancelled.id(),"status","CANCELLED"));s.reserve(form(2,1,2));check(true,"cancelled room reusable");
  Service race=new Service(new MemoryRepository(),BigDecimal.ZERO);ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch gate=new CountDownLatch(1);List<Future<Boolean>> results=new ArrayList<>();
  for(int i=0;i<2;i++)results.add(pool.submit(()->{gate.await();try{race.reserve(form(3,1,2));return true;}catch(IllegalArgumentException e){return false;}}));gate.countDown();int wins=0;for(var f:results)if(f.get())wins++;pool.shutdown();check(wins==1,"concurrent double booking blocked");
  check(Json.encode("quote\"\n").equals("\"quote\\\"\\n\""),"JSON escaping");
  System.out.println("PASS: "+checks+" business-rule, billing, concurrency, and serialization checks.");
 }
}
