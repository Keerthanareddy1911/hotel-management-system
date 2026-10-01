package hotel;
import java.util.*;
public final class Json {
 public static String encode(Object o){
  if(o==null)return "null";
  if(o instanceof Number||o instanceof Boolean)return o.toString();
  if(o instanceof Map<?,?> m)return "{"+m.entrySet().stream().map(e->encode(e.getKey().toString())+":"+encode(e.getValue())).collect(java.util.stream.Collectors.joining(","))+"}";
  if(o instanceof Iterable<?> it){List<String>a=new ArrayList<>();for(Object x:it)a.add(encode(x));return "["+String.join(",",a)+"]";}
  StringBuilder b=new StringBuilder("\"");for(char c:o.toString().toCharArray()){switch(c){case '"'->b.append("\\\"");case '\\'->b.append("\\\\");case '\n'->b.append("\\n");case '\r'->b.append("\\r");case '\t'->b.append("\\t");default->{if(c<32)b.append(String.format("\\u%04x",(int)c));else b.append(c);}}}return b.append('"').toString();
 }
}
