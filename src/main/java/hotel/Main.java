package hotel;
import com.sun.net.httpserver.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import hotel.Models.*;
public final class Main {
 public static void main(String[] args)throws Exception {
  boolean demo=Arrays.asList(args).contains("--demo");
  Repository repo=demo?new MemoryRepository():new JdbcRepository(env("DB_URL","jdbc:mysql://localhost:3306/hotel_management"),env("DB_USER","hotel_app"),env("DB_PASSWORD",""));
  Service service=new Service(repo,new BigDecimal(env("TAX_PERCENT","0")));
  HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",Integer.parseInt(env("PORT","8080"))),0);
  server.createContext("/",ex->{try{
   String path=ex.getRequestURI().getPath();
   if(path.startsWith("/api/")){
    ex.getResponseHeaders().set("Cache-Control","no-store");
    if(path.equals("/api/state")&&ex.getRequestMethod().equals("GET")){
     List<Map<String,Object>> rooms=new ArrayList<>(),bookings=new ArrayList<>();
     for(Room r:service.rooms())rooms.add(Map.of("id",r.id(),"number",r.number(),"type",r.type(),"capacity",r.capacity(),"rate",r.rate()));
     for(Booking b:service.bookings()){Map<String,Object> m=new LinkedHashMap<>();m.put("id",b.id());m.put("roomId",b.roomId());m.put("guest",b.guest());m.put("email",b.email());m.put("guests",b.guests());m.put("arrival",b.arrival());m.put("departure",b.departure());m.put("status",b.status());m.put("nightlyRate",b.nightlyRate());m.put("nights",b.nights());m.put("extras",b.extras());m.put("subtotal",b.subtotal());m.put("taxPercent",b.taxPercent());m.put("tax",b.tax());m.put("total",b.total());bookings.add(m);}
     reply(ex,200,Json.encode(Map.of("rooms",rooms,"bookings",bookings,"demo",demo,"taxPercent",env("TAX_PERCENT","0"))),"application/json");
    }else if((path.equals("/api/book")||path.equals("/api/action"))&&ex.getRequestMethod().equals("POST")){
     String origin=ex.getRequestHeaders().getFirst("Origin");String expected="http://"+ex.getRequestHeaders().getFirst("Host");if(origin!=null&&!origin.equals(expected)){reply(ex,403,"{\"error\":\"Invalid request origin\"}","application/json");return;}
     if(!Optional.ofNullable(ex.getRequestHeaders().getFirst("Content-Type")).orElse("").startsWith("application/x-www-form-urlencoded"))throw new IllegalArgumentException("Use form-encoded requests.");
     byte[] body=ex.getRequestBody().readNBytes(8193);if(body.length>8192)throw new IllegalArgumentException("Request too large.");Map<String,String> f=new HashMap<>();for(String pair:new String(body,StandardCharsets.UTF_8).split("&")){String[] kv=pair.split("=",2);if(kv.length==2)f.put(URLDecoder.decode(kv[0],StandardCharsets.UTF_8),URLDecoder.decode(kv[1],StandardCharsets.UTF_8));}
     Booking b=path.equals("/api/book")?service.reserve(f):service.action(f);reply(ex,200,Json.encode(Map.of("id",b.id())),"application/json");
    }else reply(ex,405,"{\"error\":\"Unknown API route or method\"}","application/json");
   }else {
    String resource=switch(path){case "/","/index.html"->"index.html";case "/app.js"->"app.js";case "/style.css"->"style.css";default->null;};
    if(resource==null){reply(ex,404,"Not found","text/plain");return;}
    try(var in=Main.class.getResourceAsStream("/public/"+resource)){if(in==null)throw new IllegalStateException("Missing resource");String type=resource.endsWith("css")?"text/css":resource.endsWith("js")?"text/javascript":"text/html";reply(ex,200,new String(in.readAllBytes(),StandardCharsets.UTF_8),type);}
   }
  }catch(IllegalArgumentException|java.time.DateTimeException|ArithmeticException e){reply(ex,400,Json.encode(Map.of("error",e.getMessage()==null?"Invalid input":e.getMessage())),"application/json");}
   catch(Exception e){e.printStackTrace();reply(ex,500,"{\"error\":\"Server error. Check server logs.\"}","application/json");}finally{ex.close();}});
  server.setExecutor(Executors.newFixedThreadPool(8));server.start();System.out.println("Hotel portal: http://127.0.0.1:"+server.getAddress().getPort()+" ("+(demo?"DEMO: resets on restart":"MySQL")+")");
 }
 static String env(String key,String fallback){return System.getenv().getOrDefault(key,fallback);}
 static void reply(HttpExchange ex,int code,String text,String type)throws java.io.IOException{byte[] b=text.getBytes(StandardCharsets.UTF_8);ex.getResponseHeaders().set("Content-Type",type+"; charset=utf-8");ex.getResponseHeaders().set("X-Content-Type-Options","nosniff");ex.getResponseHeaders().set("Content-Security-Policy","default-src 'self'; style-src 'self'; script-src 'self'; frame-ancestors 'none'");ex.sendResponseHeaders(code,b.length);ex.getResponseBody().write(b);}
}
