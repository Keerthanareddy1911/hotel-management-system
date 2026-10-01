import javax.tools.ToolProvider;
import java.nio.file.*;
import java.util.*;
public class Build {
 public static void main(String[] args)throws Exception {
  Files.createDirectories(Path.of("target/classes"));List<String> options=new ArrayList<>(List.of("--release","17","-d","target/classes"));
  for(String root:List.of("src/main/java","src/test/java")){try(var files=Files.walk(Path.of(root))){files.filter(p->p.toString().endsWith(".java")).forEach(p->options.add(p.toString()));}}
  var compiler=ToolProvider.getSystemJavaCompiler();if(compiler==null)throw new IllegalStateException("Install JDK 17+, not just JRE.");if(compiler.run(null,null,null,options.toArray(String[]::new))!=0)System.exit(1);
  try(var files=Files.walk(Path.of("src/main/resources"))){for(Path f:files.filter(Files::isRegularFile).toList()){Path out=Path.of("target/classes").resolve(Path.of("src/main/resources").relativize(f));Files.createDirectories(out.getParent());Files.copy(f,out,StandardCopyOption.REPLACE_EXISTING);}}
  System.out.println("Compiled successfully. Run java -cp target/classes hotel.Tests then java -cp target/classes hotel.Main --demo");
 }
}
